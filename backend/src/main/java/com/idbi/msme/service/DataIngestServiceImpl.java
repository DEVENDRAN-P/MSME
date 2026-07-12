package com.idbi.msme.service;

import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.*;
import com.idbi.msme.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataIngestServiceImpl implements DataIngestService {

    private final BusinessRepository businessRepository;
    private final GstFilingRepository gstFilingRepository;
    private final UpiTransactionRepository upiTransactionRepository;
    private final AaBankTransactionRepository aaBankTransactionRepository;
    private final EpfoRecordRepository epfoRecordRepository;
    private final UtilityPaymentRepository utilityPaymentRepository;
    private final EcommerceSaleRepository ecommerceSaleRepository;
    private final Random random = new Random();

    public DataIngestServiceImpl(
            BusinessRepository businessRepository,
            GstFilingRepository gstFilingRepository,
            UpiTransactionRepository upiTransactionRepository,
            AaBankTransactionRepository aaBankTransactionRepository,
            EpfoRecordRepository epfoRecordRepository,
            UtilityPaymentRepository utilityPaymentRepository,
            EcommerceSaleRepository ecommerceSaleRepository) {
        this.businessRepository = businessRepository;
        this.gstFilingRepository = gstFilingRepository;
        this.upiTransactionRepository = upiTransactionRepository;
        this.aaBankTransactionRepository = aaBankTransactionRepository;
        this.epfoRecordRepository = epfoRecordRepository;
        this.utilityPaymentRepository = utilityPaymentRepository;
        this.ecommerceSaleRepository = ecommerceSaleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public IngestSummaryResponse getIngestSummary(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with ID: " + businessId));

        List<GstFiling> gstFilings = gstFilingRepository.findByBusinessIdOrderByFilingMonthAsc(businessId);
        List<UpiTransaction> upiTransactions = upiTransactionRepository.findByBusinessIdOrderByMonthAsc(businessId);
        List<AaBankTransaction> bankTransactions = aaBankTransactionRepository.findByBusinessIdOrderByMonthAsc(businessId);
        List<EpfoRecord> epfoRecords = epfoRecordRepository.findByBusinessIdOrderByMonthAsc(businessId);
        List<UtilityPayment> utilityPayments = utilityPaymentRepository.findByBusinessIdOrderByBillingMonthAsc(businessId);
        List<EcommerceSale> ecommerceSales = ecommerceSaleRepository.findByBusinessIdOrderByMonthAsc(businessId);

        return new IngestSummaryResponse(
                businessId,
                !gstFilings.isEmpty(),
                !upiTransactions.isEmpty(),
                !bankTransactions.isEmpty(),
                !epfoRecords.isEmpty(),
                !utilityPayments.isEmpty(),
                !ecommerceSales.isEmpty(),
                gstFilings.stream().map(f -> new IngestSummaryResponse.GstRecordDto(f.getFilingMonth(), f.getTurnover(), f.getTaxPaid(), f.getFilingStatus())).collect(Collectors.toList()),
                upiTransactions.stream().map(u -> new IngestSummaryResponse.UpiRecordDto(u.getMonth(), u.getTotalCreditVolume(), u.getTotalCreditCount(), u.getTotalDebitVolume(), u.getTotalDebitCount())).collect(Collectors.toList()),
                bankTransactions.stream().map(b -> new IngestSummaryResponse.BankRecordDto(b.getMonth(), b.getAvgBalance(), b.getInwardRemittances(), b.getOutwardRemittances())).collect(Collectors.toList()),
                epfoRecords.stream().map(e -> new IngestSummaryResponse.EpfoRecordDto(e.getMonth(), e.getEmployeeCount(), e.getContributionAmount())).collect(Collectors.toList()),
                utilityPayments.stream().map(ut -> new IngestSummaryResponse.UtilityRecordDto(ut.getUtilityType(), ut.getBillingMonth(), ut.getAmount(), ut.getPaymentStatus())).collect(Collectors.toList()),
                ecommerceSales.stream().map(ec -> new IngestSummaryResponse.EcommRecordDto(ec.getPlatform(), ec.getMonth(), ec.getSalesVolume(), ec.getOrderCount())).collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public IngestSummaryResponse syncAlternateData(UUID businessId, String streamType) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business profile not found with ID: " + businessId));

        // Purge existing records for deterministic re-sync
        if ("ALL".equalsIgnoreCase(streamType) || "GST".equalsIgnoreCase(streamType)) {
            gstFilingRepository.deleteByBusinessId(businessId);
        }
        if ("ALL".equalsIgnoreCase(streamType) || "UPI".equalsIgnoreCase(streamType)) {
            upiTransactionRepository.deleteByBusinessId(businessId);
        }
        if ("ALL".equalsIgnoreCase(streamType) || "AA".equalsIgnoreCase(streamType)) {
            aaBankTransactionRepository.deleteByBusinessId(businessId);
        }
        if ("ALL".equalsIgnoreCase(streamType) || "EPFO".equalsIgnoreCase(streamType)) {
            epfoRecordRepository.deleteByBusinessId(businessId);
        }
        if ("ALL".equalsIgnoreCase(streamType) || "UTILITY".equalsIgnoreCase(streamType)) {
            utilityPaymentRepository.deleteByBusinessId(businessId);
        }
        if ("ALL".equalsIgnoreCase(streamType) || "ECOMMERCE".equalsIgnoreCase(streamType)) {
            ecommerceSaleRepository.deleteByBusinessId(businessId);
        }

        // Establish average sector metrics (in INR)
        BigDecimal baseMonthlyTurnover;
        String sector = business.getIndustrySector().toLowerCase();
        if (sector.contains("manufacturing")) {
            baseMonthlyTurnover = BigDecimal.valueOf(1800000); // 18L
        } else if (sector.contains("service")) {
            baseMonthlyTurnover = BigDecimal.valueOf(800000);   // 8L
        } else if (sector.contains("retail") || sector.contains("wholesale") || sector.contains("trade")) {
            baseMonthlyTurnover = BigDecimal.valueOf(500000);   // 5L
        } else {
            baseMonthlyTurnover = BigDecimal.valueOf(1000000);  // 10L
        }

        // Generate 12 months of timelines up to the previous calendar month
        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 12; i >= 1; i--) {
            LocalDate targetDate = today.minusMonths(i);
            String monthKey = targetDate.format(monthFormatter);

            // Deviate monthly revenues to look realistic (e.g. holiday peaks, seasonality)
            double multiplier = 0.85 + (1.30 - 0.85) * random.nextDouble();
            BigDecimal monthlyTurnover = baseMonthlyTurnover.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxPaid = monthlyTurnover.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP); // 18% standard GST rate

            // 1. GST Filings
            if ("ALL".equalsIgnoreCase(streamType) || "GST".equalsIgnoreCase(streamType)) {
                String filingStatus = random.nextDouble() < 0.90 ? "FILED" : "DELAYED";
                GstFiling filing = new GstFiling(UUID.randomUUID(), business, monthKey, monthlyTurnover, taxPaid, filingStatus);
                gstFilingRepository.save(filing);
            }

            // 2. UPI Transaction Summaries
            BigDecimal upiCreditVolume = monthlyTurnover.multiply(BigDecimal.valueOf(0.40 + (0.65 - 0.40) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP); // 40-65% UPI collections
            int upiCreditCount = 180 + random.nextInt(400);
            BigDecimal upiDebitVolume = upiCreditVolume.multiply(BigDecimal.valueOf(0.85 + (0.95 - 0.85) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            int upiDebitCount = 100 + random.nextInt(200);

            if ("ALL".equalsIgnoreCase(streamType) || "UPI".equalsIgnoreCase(streamType)) {
                UpiTransaction upi = new UpiTransaction(UUID.randomUUID(), business, monthKey, upiCreditVolume, upiCreditCount, upiDebitVolume, upiDebitCount);
                upiTransactionRepository.save(upi);
            }

            // 3. AA Bank Account Summaries
            BigDecimal bankInflows = monthlyTurnover.multiply(BigDecimal.valueOf(0.95 + (1.10 - 0.95) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP); // captures cash/cheques + UPI
            BigDecimal bankOutflows = bankInflows.multiply(BigDecimal.valueOf(0.88 + (0.96 - 0.88) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal avgBalance = bankInflows.multiply(BigDecimal.valueOf(0.12 + (0.25 - 0.12) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);

            if ("ALL".equalsIgnoreCase(streamType) || "AA".equalsIgnoreCase(streamType)) {
                AaBankTransaction bank = new AaBankTransaction(UUID.randomUUID(), business, monthKey, avgBalance, bankInflows, bankOutflows);
                aaBankTransactionRepository.save(bank);
            }

            // 4. EPFO Records
            int employees = sector.contains("manufacturing") ? 18 + random.nextInt(15) : sector.contains("service") ? 8 + random.nextInt(8) : 3 + random.nextInt(5);
            BigDecimal contribution = BigDecimal.valueOf(employees).multiply(BigDecimal.valueOf(1800)).setScale(2, RoundingMode.HALF_UP);

            if ("ALL".equalsIgnoreCase(streamType) || "EPFO".equalsIgnoreCase(streamType)) {
                EpfoRecord epfo = new EpfoRecord(UUID.randomUUID(), business, monthKey, employees, contribution);
                epfoRecordRepository.save(epfo);
            }

            // 5. Utility Bills Payments
            if ("ALL".equalsIgnoreCase(streamType) || "UTILITY".equalsIgnoreCase(streamType)) {
                BigDecimal elecAmount = BigDecimal.valueOf(8000 + random.nextInt(12000));
                BigDecimal waterAmount = BigDecimal.valueOf(1200 + random.nextInt(1800));
                BigDecimal telecomAmount = BigDecimal.valueOf(2500 + random.nextInt(4000));

                String elecStatus = random.nextDouble() < 0.92 ? "PAID_ON_TIME" : "PAID_LATE";
                String waterStatus = random.nextDouble() < 0.96 ? "PAID_ON_TIME" : "PAID_LATE";
                String telecomStatus = random.nextDouble() < 0.95 ? "PAID_ON_TIME" : "PAID_LATE";

                utilityPaymentRepository.save(new UtilityPayment(UUID.randomUUID(), business, "ELECTRICITY", monthKey, elecAmount, elecStatus));
                utilityPaymentRepository.save(new UtilityPayment(UUID.randomUUID(), business, "WATER", monthKey, waterAmount, waterStatus));
                utilityPaymentRepository.save(new UtilityPayment(UUID.randomUUID(), business, "TELECOM", monthKey, telecomAmount, telecomStatus));
            }

            // 6. E-Commerce Trade Records
            if ("ALL".equalsIgnoreCase(streamType) || "ECOMMERCE".equalsIgnoreCase(streamType)) {
                String platform = sector.contains("retail") || sector.contains("trade") ? (random.nextBoolean() ? "AMAZON" : "ONDC") : "ONDC";
                BigDecimal ecommSales = monthlyTurnover.multiply(BigDecimal.valueOf(0.10 + (0.28 - 0.10) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP); // 10-28% online sales channels
                int orders = 50 + random.nextInt(150);

                ecommerceSaleRepository.save(new EcommerceSale(UUID.randomUUID(), business, platform, monthKey, ecommSales, orders));
            }
        }

        return getIngestSummary(businessId);
    }
}

package com.idbi.msme.service;

import com.idbi.msme.dto.IngestSummaryResponse;
import com.idbi.msme.exception.ResourceNotFoundException;
import com.idbi.msme.model.*;
import com.idbi.msme.repository.FirestoreDataAccess;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataIngestServiceImpl implements DataIngestService {

    private final FirestoreDataAccess db;
    private final Random random = new Random();

    public DataIngestServiceImpl(FirestoreDataAccess db) {
        this.db = db;
    }

    @Override
    public IngestSummaryResponse getIngestSummary(String businessId) {
        BusinessProfile business = db.findBusinessById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        var gstFilings = db.getGstFilings(businessId);
        var upiTransactions = db.getUpiTransactions(businessId);
        var bankTransactions = db.getAaBankTransactions(businessId);
        var epfoRecords = db.getEpfoRecords(businessId);
        var utilityPayments = db.getUtilityPayments(businessId);
        var ecommerceSales = db.getEcommerceSales(businessId);

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
    public IngestSummaryResponse syncAlternateData(String businessId, String streamType) {
        BusinessProfile business = db.findBusinessById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found with ID: " + businessId));

        if ("ALL".equalsIgnoreCase(streamType) || "GST".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "gstFilings");
        }
        if ("ALL".equalsIgnoreCase(streamType) || "UPI".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "upiTransactions");
        }
        if ("ALL".equalsIgnoreCase(streamType) || "AA".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "aaBankTransactions");
        }
        if ("ALL".equalsIgnoreCase(streamType) || "EPFO".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "epfoRecords");
        }
        if ("ALL".equalsIgnoreCase(streamType) || "UTILITY".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "utilityPayments");
        }
        if ("ALL".equalsIgnoreCase(streamType) || "ECOMMERCE".equalsIgnoreCase(streamType)) {
            db.deleteSubCollection(businessId, "ecommerceSales");
        }

        BigDecimal baseMonthlyTurnover;
        String sector = business.getIndustrySector().toLowerCase();
        if (sector.contains("manufacturing")) {
            baseMonthlyTurnover = BigDecimal.valueOf(1800000);
        } else if (sector.contains("service")) {
            baseMonthlyTurnover = BigDecimal.valueOf(800000);
        } else if (sector.contains("retail") || sector.contains("wholesale") || sector.contains("trade")) {
            baseMonthlyTurnover = BigDecimal.valueOf(500000);
        } else {
            baseMonthlyTurnover = BigDecimal.valueOf(1000000);
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 12; i >= 1; i--) {
            LocalDate targetDate = today.minusMonths(i);
            String monthKey = targetDate.format(monthFormatter);

            double multiplier = 0.85 + (1.30 - 0.85) * random.nextDouble();
            BigDecimal monthlyTurnover = baseMonthlyTurnover.multiply(BigDecimal.valueOf(multiplier)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxPaid = monthlyTurnover.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);

            if ("ALL".equalsIgnoreCase(streamType) || "GST".equalsIgnoreCase(streamType)) {
                String filingStatus = random.nextDouble() < 0.90 ? "FILED" : "DELAYED";
                GstFilingDocument filing = new GstFilingDocument();
                filing.setId(UUID.randomUUID().toString());
                filing.setBusinessId(businessId);
                filing.setFilingMonth(monthKey);
                filing.setTurnover(monthlyTurnover);
                filing.setTaxPaid(taxPaid);
                filing.setFilingStatus(filingStatus);
                filing.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveGstFiling(filing);
            }

            BigDecimal upiCreditVolume = monthlyTurnover.multiply(BigDecimal.valueOf(0.40 + (0.65 - 0.40) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            int upiCreditCount = 180 + random.nextInt(400);
            BigDecimal upiDebitVolume = upiCreditVolume.multiply(BigDecimal.valueOf(0.85 + (0.95 - 0.85) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            int upiDebitCount = 100 + random.nextInt(200);

            if ("ALL".equalsIgnoreCase(streamType) || "UPI".equalsIgnoreCase(streamType)) {
                UpiTransactionDocument upi = new UpiTransactionDocument();
                upi.setId(UUID.randomUUID().toString());
                upi.setBusinessId(businessId);
                upi.setMonth(monthKey);
                upi.setTotalCreditVolume(upiCreditVolume);
                upi.setTotalCreditCount(upiCreditCount);
                upi.setTotalDebitVolume(upiDebitVolume);
                upi.setTotalDebitCount(upiDebitCount);
                upi.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveUpiTransaction(upi);
            }

            BigDecimal bankInflows = monthlyTurnover.multiply(BigDecimal.valueOf(0.95 + (1.10 - 0.95) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal bankOutflows = bankInflows.multiply(BigDecimal.valueOf(0.88 + (0.96 - 0.88) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal avgBalance = bankInflows.multiply(BigDecimal.valueOf(0.12 + (0.25 - 0.12) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);

            if ("ALL".equalsIgnoreCase(streamType) || "AA".equalsIgnoreCase(streamType)) {
                AaBankTransactionDocument bank = new AaBankTransactionDocument();
                bank.setId(UUID.randomUUID().toString());
                bank.setBusinessId(businessId);
                bank.setMonth(monthKey);
                bank.setAvgBalance(avgBalance);
                bank.setInwardRemittances(bankInflows);
                bank.setOutwardRemittances(bankOutflows);
                bank.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveAaBankTransaction(bank);
            }

            int employees = sector.contains("manufacturing") ? 18 + random.nextInt(15) : sector.contains("service") ? 8 + random.nextInt(8) : 3 + random.nextInt(5);
            BigDecimal contribution = BigDecimal.valueOf(employees).multiply(BigDecimal.valueOf(1800)).setScale(2, RoundingMode.HALF_UP);

            if ("ALL".equalsIgnoreCase(streamType) || "EPFO".equalsIgnoreCase(streamType)) {
                EpfoRecordDocument epfo = new EpfoRecordDocument();
                epfo.setId(UUID.randomUUID().toString());
                epfo.setBusinessId(businessId);
                epfo.setMonth(monthKey);
                epfo.setEmployeeCount(employees);
                epfo.setContributionAmount(contribution);
                epfo.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveEpfoRecord(epfo);
            }

            if ("ALL".equalsIgnoreCase(streamType) || "UTILITY".equalsIgnoreCase(streamType)) {
                BigDecimal elecAmount = BigDecimal.valueOf(8000 + random.nextInt(12000));
                BigDecimal waterAmount = BigDecimal.valueOf(1200 + random.nextInt(1800));
                BigDecimal telecomAmount = BigDecimal.valueOf(2500 + random.nextInt(4000));
                String elecStatus = random.nextDouble() < 0.92 ? "PAID_ON_TIME" : "PAID_LATE";
                String waterStatus = random.nextDouble() < 0.96 ? "PAID_ON_TIME" : "PAID_LATE";
                String telecomStatus = random.nextDouble() < 0.95 ? "PAID_ON_TIME" : "PAID_LATE";

                UtilityPaymentDocument elec = new UtilityPaymentDocument();
                elec.setId(UUID.randomUUID().toString());
                elec.setBusinessId(businessId);
                elec.setUtilityType("ELECTRICITY");
                elec.setBillingMonth(monthKey);
                elec.setAmount(elecAmount);
                elec.setPaymentStatus(elecStatus);
                elec.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveUtilityPayment(elec);

                UtilityPaymentDocument water = new UtilityPaymentDocument();
                water.setId(UUID.randomUUID().toString());
                water.setBusinessId(businessId);
                water.setUtilityType("WATER");
                water.setBillingMonth(monthKey);
                water.setAmount(waterAmount);
                water.setPaymentStatus(waterStatus);
                water.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveUtilityPayment(water);

                UtilityPaymentDocument telecom = new UtilityPaymentDocument();
                telecom.setId(UUID.randomUUID().toString());
                telecom.setBusinessId(businessId);
                telecom.setUtilityType("TELECOM");
                telecom.setBillingMonth(monthKey);
                telecom.setAmount(telecomAmount);
                telecom.setPaymentStatus(telecomStatus);
                telecom.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveUtilityPayment(telecom);
            }

            if ("ALL".equalsIgnoreCase(streamType) || "ECOMMERCE".equalsIgnoreCase(streamType)) {
                String platform = sector.contains("retail") || sector.contains("trade") ? (random.nextBoolean() ? "AMAZON" : "ONDC") : "ONDC";
                BigDecimal ecommSales = monthlyTurnover.multiply(BigDecimal.valueOf(0.10 + (0.28 - 0.10) * random.nextDouble())).setScale(2, RoundingMode.HALF_UP);
                int orders = 50 + random.nextInt(150);

                EcommerceSaleDocument ecomm = new EcommerceSaleDocument();
                ecomm.setId(UUID.randomUUID().toString());
                ecomm.setBusinessId(businessId);
                ecomm.setPlatform(platform);
                ecomm.setMonth(monthKey);
                ecomm.setSalesVolume(ecommSales);
                ecomm.setOrderCount(orders);
                ecomm.setCreatedAt(java.time.LocalDateTime.now().toString());
                db.saveEcommerceSale(ecomm);
            }
        }

        return getIngestSummary(businessId);
    }
}

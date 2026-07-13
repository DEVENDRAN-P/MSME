package com.idbi.msme.dto;

import java.math.BigDecimal;
import java.util.List;

public class IngestSummaryResponse {

    private String businessId;
    private boolean gstSynced;
    private boolean upiSynced;
    private boolean aaSynced;
    private boolean epfoSynced;
    private boolean utilitySynced;
    private boolean ecommSynced;

    private List<GstRecordDto> gstRecords;
    private List<UpiRecordDto> upiRecords;
    private List<BankRecordDto> bankRecords;
    private List<EpfoRecordDto> epfoRecords;
    private List<UtilityRecordDto> utilityRecords;
    private List<EcommRecordDto> ecommRecords;

    public IngestSummaryResponse() {
    }

    public IngestSummaryResponse(String businessId, boolean gstSynced, boolean upiSynced, boolean aaSynced,
                                 boolean epfoSynced, boolean utilitySynced, boolean ecommSynced,
                                 List<GstRecordDto> gstRecords, List<UpiRecordDto> upiRecords,
                                 List<BankRecordDto> bankRecords, List<EpfoRecordDto> epfoRecords,
                                 List<UtilityRecordDto> utilityRecords, List<EcommRecordDto> ecommRecords) {
        this.businessId = businessId;
        this.gstSynced = gstSynced;
        this.upiSynced = upiSynced;
        this.aaSynced = aaSynced;
        this.epfoSynced = epfoSynced;
        this.utilitySynced = utilitySynced;
        this.ecommSynced = ecommSynced;
        this.gstRecords = gstRecords;
        this.upiRecords = upiRecords;
        this.bankRecords = bankRecords;
        this.epfoRecords = epfoRecords;
        this.utilityRecords = utilityRecords;
        this.ecommRecords = ecommRecords;
    }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public boolean isGstSynced() { return gstSynced; }
    public void setGstSynced(boolean gstSynced) { this.gstSynced = gstSynced; }
    public boolean isUpiSynced() { return upiSynced; }
    public void setUpiSynced(boolean upiSynced) { this.upiSynced = upiSynced; }
    public boolean isAaSynced() { return aaSynced; }
    public void setAaSynced(boolean aaSynced) { this.aaSynced = aaSynced; }
    public boolean isEpfoSynced() { return epfoSynced; }
    public void setEpfoSynced(boolean epfoSynced) { this.epfoSynced = epfoSynced; }
    public boolean isUtilitySynced() { return utilitySynced; }
    public void setUtilitySynced(boolean utilitySynced) { this.utilitySynced = utilitySynced; }
    public boolean isEcommSynced() { return ecommSynced; }
    public void setEcommSynced(boolean ecommSynced) { this.ecommSynced = ecommSynced; }
    public List<GstRecordDto> getGstRecords() { return gstRecords; }
    public void setGstRecords(List<GstRecordDto> gstRecords) { this.gstRecords = gstRecords; }
    public List<UpiRecordDto> getUpiRecords() { return upiRecords; }
    public void setUpiRecords(List<UpiRecordDto> upiRecords) { this.upiRecords = upiRecords; }
    public List<BankRecordDto> getBankRecords() { return bankRecords; }
    public void setBankRecords(List<BankRecordDto> bankRecords) { this.bankRecords = bankRecords; }
    public List<EpfoRecordDto> getEpfoRecords() { return epfoRecords; }
    public void setEpfoRecords(List<EpfoRecordDto> epfoRecords) { this.epfoRecords = epfoRecords; }
    public List<UtilityRecordDto> getUtilityRecords() { return utilityRecords; }
    public void setUtilityRecords(List<UtilityRecordDto> utilityRecords) { this.utilityRecords = utilityRecords; }
    public List<EcommRecordDto> getEcommRecords() { return ecommRecords; }
    public void setEcommRecords(List<EcommRecordDto> ecommRecords) { this.ecommRecords = ecommRecords; }

    public static class GstRecordDto {
        public String month;
        public BigDecimal turnover;
        public BigDecimal taxPaid;
        public String status;

        public GstRecordDto(String month, BigDecimal turnover, BigDecimal taxPaid, String status) {
            this.month = month;
            this.turnover = turnover;
            this.taxPaid = taxPaid;
            this.status = status;
        }
    }

    public static class UpiRecordDto {
        public String month;
        public BigDecimal creditVolume;
        public int creditCount;
        public BigDecimal debitVolume;
        public int debitCount;

        public UpiRecordDto(String month, BigDecimal creditVolume, int creditCount, BigDecimal debitVolume, int debitCount) {
            this.month = month;
            this.creditVolume = creditVolume;
            this.creditCount = creditCount;
            this.debitVolume = debitVolume;
            this.debitCount = debitCount;
        }
    }

    public static class BankRecordDto {
        public String month;
        public BigDecimal avgBalance;
        public BigDecimal inflows;
        public BigDecimal outflows;

        public BankRecordDto(String month, BigDecimal avgBalance, BigDecimal inflows, BigDecimal outflows) {
            this.month = month;
            this.avgBalance = avgBalance;
            this.inflows = inflows;
            this.outflows = outflows;
        }
    }

    public static class EpfoRecordDto {
        public String month;
        public int employeeCount;
        public BigDecimal contribution;

        public EpfoRecordDto(String month, int employeeCount, BigDecimal contribution) {
            this.month = month;
            this.employeeCount = employeeCount;
            this.contribution = contribution;
        }
    }

    public static class UtilityRecordDto {
        public String type;
        public String month;
        public BigDecimal amount;
        public String status;

        public UtilityRecordDto(String type, String month, BigDecimal amount, String status) {
            this.type = type;
            this.month = month;
            this.amount = amount;
            this.status = status;
        }
    }

    public static class EcommRecordDto {
        public String platform;
        public String month;
        public BigDecimal sales;
        public int orders;

        public EcommRecordDto(String platform, String month, BigDecimal sales, int orders) {
            this.platform = platform;
            this.month = month;
            this.sales = sales;
            this.orders = orders;
        }
    }
}

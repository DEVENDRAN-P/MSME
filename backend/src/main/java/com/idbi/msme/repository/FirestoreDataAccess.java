package com.idbi.msme.repository;

import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class FirestoreDataAccess {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreDataAccess.class);

    private final Firestore firestore;

    @Autowired
    public FirestoreDataAccess(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestoreDataAccess initialized with Firestore backend");
    }

    public Optional<com.idbi.msme.model.UserProfile> findUserById(String uid) {
        try {
            var doc = firestore.collection("users").document(uid).get().get();
            if (!doc.exists()) return Optional.empty();
            var d = doc.getData();
            com.idbi.msme.model.UserProfile u = new com.idbi.msme.model.UserProfile();
            u.setId(doc.getId());
            u.setEmail(d.get("email") != null ? d.get("email").toString() : "");
            u.setFullName(d.get("fullName") != null ? d.get("fullName").toString() : "");
            u.setRole(d.get("role") != null ? d.get("role").toString() : "ROLE_MSME");
            u.setPhone(d.get("phone") != null ? d.get("phone").toString() : null);
            u.setStatus(d.get("status") != null ? d.get("status").toString() : "ACTIVE");
            return Optional.of(u);
        } catch (Exception e) {
            logger.error("Error finding user by id {}: {}", uid, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void saveUserProfile(String uid, String email, String fullName, String role, String phone) {
        try {
            Map<String, Object> profile = new HashMap<>();
            profile.put("email", email);
            profile.put("fullName", fullName);
            profile.put("role", role);
            profile.put("phone", phone);
            profile.put("status", "ACTIVE");
            profile.put("createdAt", java.time.LocalDateTime.now().toString());
            profile.put("updatedAt", java.time.LocalDateTime.now().toString());
            firestore.collection("users").document(uid).set(profile).get();
        } catch (Exception e) {
            logger.error("Error saving user profile for {}: {}", uid, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean existsByEmail(String email) {
        try {
            return !unwrap(firestore.collection("users").whereEqualTo("email", email).limit(1).get()).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ======================== BUSINESS ========================

    public void saveBusiness(com.idbi.msme.model.BusinessProfile b) {
        try {
            firestore.collection("businesses").document(b.getId()).set(b).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<com.idbi.msme.model.BusinessProfile> findBusinessByOwnerId(String ownerId) {
        try {
            var snap = firestore.collection("businesses").whereEqualTo("ownerId", ownerId).limit(1).get().get();
            if (snap.isEmpty()) return Optional.empty();
            return Optional.ofNullable(snap.getDocuments().get(0).toObject(com.idbi.msme.model.BusinessProfile.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<com.idbi.msme.model.BusinessProfile> findBusinessById(String id) {
        try {
            var doc = firestore.collection("businesses").document(id).get().get();
            if (!doc.exists()) return Optional.empty();
            return Optional.ofNullable(doc.toObject(com.idbi.msme.model.BusinessProfile.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<com.idbi.msme.model.BusinessProfile> findAllBusinesses() {
        try {
            return firestore.collection("businesses").get().get().getDocuments().stream()
                    .map(d -> d.toObject(com.idbi.msme.model.BusinessProfile.class)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByGstin(String gstin) {
        try { return !unwrap(firestore.collection("businesses").whereEqualTo("gstin", gstin).limit(1).get()).isEmpty(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean existsByPan(String pan) {
        try { return !unwrap(firestore.collection("businesses").whereEqualTo("pan", pan).limit(1).get()).isEmpty(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean existsByUdyamNumber(String udyamNumber) {
        try { return !unwrap(firestore.collection("businesses").whereEqualTo("udyamNumber", udyamNumber).limit(1).get()).isEmpty(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean existsByOwnerId(String ownerId) {
        try { return !unwrap(firestore.collection("businesses").whereEqualTo("ownerId", ownerId).limit(1).get()).isEmpty(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // ======================== LOANS ========================

    public void saveLoan(com.idbi.msme.model.LoanDocument loan) {
        try { firestore.collection("loans").document(loan.getId()).set(loan).get(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.LoanDocument> findLoansByOwnerId(String ownerId) {
        var biz = findBusinessByOwnerId(ownerId);
        return biz.isEmpty() ? Collections.emptyList() : findLoansByBusinessId(biz.get().getId());
    }

    public List<com.idbi.msme.model.LoanDocument> findLoansByBusinessId(String businessId) {
        try {
            return firestore.collection("loans").whereEqualTo("businessId", businessId).get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.LoanDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ======================== CONSENTS ========================

    public void saveConsent(com.idbi.msme.model.ConsentDocument consent) {
        try { firestore.collection("consents").document(consent.getId()).set(consent).get(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public Optional<com.idbi.msme.model.ConsentDocument> findConsentById(String id) {
        try {
            var doc = firestore.collection("consents").document(id).get().get();
            return doc.exists() ? Optional.ofNullable(doc.toObject(com.idbi.msme.model.ConsentDocument.class)) : Optional.empty();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.ConsentDocument> findConsentsByOwnerId(String ownerId) {
        var biz = findBusinessByOwnerId(ownerId);
        if (biz.isEmpty()) return Collections.emptyList();
        String bizId = biz.get().getId();
        try {
            return firestore.collection("consents").whereEqualTo("businessId", bizId).get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.ConsentDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.ConsentDocument> findConsentsByOwnerIdAndStatus(String ownerId, String status) {
        var biz = findBusinessByOwnerId(ownerId);
        if (biz.isEmpty()) return Collections.emptyList();
        String bizId = biz.get().getId();
        try {
            return firestore.collection("consents").whereEqualTo("businessId", bizId).whereEqualTo("status", status).get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.ConsentDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.ConsentDocument> findConsentsByLenderId(String lenderId) {
        try {
            return firestore.collection("consents").whereEqualTo("requestedById", lenderId).get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.ConsentDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean hasActiveConsent(String businessId, String lenderId) {
        try {
            var snap = firestore.collection("consents").whereEqualTo("businessId", businessId)
                    .whereEqualTo("requestedById", lenderId).whereEqualTo("status", "APPROVED").get().get();
            return snap.getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.ConsentDocument.class))
                    .anyMatch(c -> c.getValidUntil() != null && java.time.LocalDateTime.parse(c.getValidUntil()).isAfter(java.time.LocalDateTime.now()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ======================== DATA STREAMS ========================

    public void deleteSubCollection(String businessId, String collection) {
        try {
            var docs = firestore.collection("businesses").document(businessId).collection(collection).get().get().getDocuments();
            var batch = firestore.batch();
            for (var doc : docs) batch.delete(doc.getReference());
            if (!docs.isEmpty()) batch.commit().get();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void saveGstFiling(com.idbi.msme.model.GstFilingDocument f) {
        try {
            String id = f.getId() != null ? f.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(f.getBusinessId()).collection("gstFilings").document(id).set(f).get();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void saveUpiTransaction(com.idbi.msme.model.UpiTransactionDocument t) {
        try {
            String id = t.getId() != null ? t.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(t.getBusinessId()).collection("upiTransactions").document(id).set(t).get();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void saveAaBankTransaction(com.idbi.msme.model.AaBankTransactionDocument b) {
        try {
            String id = b.getId() != null ? b.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(b.getBusinessId()).collection("aaBankTransactions").document(id).set(b).get();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void saveEpfoRecord(com.idbi.msme.model.EpfoRecordDocument e) {
        try {
            String id = e.getId() != null ? e.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(e.getBusinessId()).collection("epfoRecords").document(id).set(e).get();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public void saveUtilityPayment(com.idbi.msme.model.UtilityPaymentDocument u) {
        try {
            String id = u.getId() != null ? u.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(u.getBusinessId()).collection("utilityPayments").document(id).set(u).get();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public void saveEcommerceSale(com.idbi.msme.model.EcommerceSaleDocument s) {
        try {
            String id = s.getId() != null ? s.getId() : UUID.randomUUID().toString();
            firestore.collection("businesses").document(s.getBusinessId()).collection("ecommerceSales").document(id).set(s).get();
        } catch (Exception ex) { throw new RuntimeException(ex); }
    }

    public List<com.idbi.msme.model.GstFilingDocument> getGstFilings(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("gstFilings").orderBy("filingMonth").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.GstFilingDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.UpiTransactionDocument> getUpiTransactions(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("upiTransactions").orderBy("month").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.UpiTransactionDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.AaBankTransactionDocument> getAaBankTransactions(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("aaBankTransactions").orderBy("month").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.AaBankTransactionDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.EpfoRecordDocument> getEpfoRecords(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("epfoRecords").orderBy("month").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.EpfoRecordDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.UtilityPaymentDocument> getUtilityPayments(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("utilityPayments").orderBy("billingMonth").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.UtilityPaymentDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<com.idbi.msme.model.EcommerceSaleDocument> getEcommerceSales(String businessId) {
        try {
            return firestore.collection("businesses").document(businessId).collection("ecommerceSales").orderBy("month").get().get()
                    .getDocuments().stream().map(d -> d.toObject(com.idbi.msme.model.EcommerceSaleDocument.class)).collect(Collectors.toList());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void saveAuditLog(com.idbi.msme.model.AuditLogDocument log) {
        try { firestore.collection("auditLogs").document(log.getId()).set(log).get(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private <T> T unwrap(com.google.api.core.ApiFuture<T> future) {
        try { return future.get(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
        catch (ExecutionException e) { throw new RuntimeException(e); }
    }
}

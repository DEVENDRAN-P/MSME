package com.idbi.msme.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final Firestore firestore;

    public DatabaseSeeder(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running development seed data...");

        String msmeUid = createFirebaseUser("owner@saraswatifabrics.in", "password123", "Saraswati Fabrics Owner");
        if (msmeUid != null) {
            saveUserProfile(msmeUid, "owner@saraswatifabrics.in", "Saraswati Fabrics Owner", "ROLE_MSME", "9876543210");
            String businessId = saveBusinessProfile(msmeUid);
            logger.info("Seeded MSME user={} business={}", msmeUid, businessId);
        }

        String lenderUid = createFirebaseUser("credit.mgr@idbi.com", "password123", "IDBI Credit Manager");
        if (lenderUid != null) {
            saveUserProfile(lenderUid, "credit.mgr@idbi.com", "IDBI Credit Manager", "ROLE_CREDIT_MANAGER", "9876543211");
            logger.info("Seeded lender user={}", lenderUid);
        }

        String adminUid = createFirebaseUser("sys.admin@idbi.com", "password123", "System Administrator");
        if (adminUid != null) {
            saveUserProfile(adminUid, "sys.admin@idbi.com", "System Administrator", "ROLE_ADMIN", "9876543212");
            logger.info("Seeded admin user={}", adminUid);
        }

        logger.info("Development seed data completed.");
    }

    private String createFirebaseUser(String email, String password, String displayName) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(displayName)
                    .setEmailVerified(true);
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            logger.info("Created Firebase user: {} ({})", email, userRecord.getUid());
            return userRecord.getUid();
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            logger.info("Firebase user creation failed for {}, checking if user already exists: {}", email, e.getMessage());
            try {
                UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(email);
                logger.info("User {} already exists in Firebase with uid: {}", email, existingUser.getUid());
                return existingUser.getUid();
            } catch (Exception ex) {
                logger.error("Failed to create or retrieve Firebase user {}: {}", email, e.getMessage());
                return null;
            }
        }
    }

    private void saveUserProfile(String uid, String email, String fullName, String role, String phone) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);
        profile.put("fullName", fullName);
        profile.put("role", role);
        profile.put("phone", phone);
        profile.put("status", "ACTIVE");
        profile.put("createdAt", LocalDateTime.now().toString());
        profile.put("updatedAt", LocalDateTime.now().toString());
        try {
            firestore.collection("users").document(uid).set(profile).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to save user profile for {}: {}", uid, e.getMessage());
        }
    }

    private String saveBusinessProfile(String ownerId) {
        String businessId = "BIZ-" + ownerId.substring(0, 8);
        Map<String, Object> biz = new HashMap<>();
        biz.put("ownerId", ownerId);
        biz.put("legalName", "Saraswati Fabrics Private Limited");
        biz.put("tradeName", "Saraswati Fabrics");
        biz.put("gstin", "27AAAAA1111A1Z1");
        biz.put("pan", "AAAAA1111A");
        biz.put("udyamNumber", "UDYAM-MH-33-0000001");
        biz.put("incorporationDate", "2015-06-15");
        biz.put("constitution", "Private Limited Company");
        biz.put("industrySector", "Textile Manufacturing");
        biz.put("addressLine1", "102, Industrial Estate, Lower Parel");
        biz.put("addressLine2", "Senapati Bapat Marg");
        biz.put("city", "Mumbai");
        biz.put("state", "Maharashtra");
        biz.put("pincode", "400013");
        biz.put("createdAt", LocalDateTime.now().toString());
        biz.put("updatedAt", LocalDateTime.now().toString());
        try {
            firestore.collection("businesses").document(businessId).set(biz).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to save business profile: {}", e.getMessage());
        }
        return businessId;
    }
}

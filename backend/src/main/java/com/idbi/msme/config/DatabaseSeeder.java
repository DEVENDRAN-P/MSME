package com.idbi.msme.config;

import com.idbi.msme.model.*;
import com.idbi.msme.repository.*;
import com.idbi.msme.service.DataIngestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final DataIngestService dataIngestService;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          BusinessRepository businessRepository,
                          DataIngestService dataIngestService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.dataIngestService = dataIngestService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed MSME Owner
        if (!userRepository.existsByEmail("owner@saraswatifabrics.in")) {
            User msmeUser = new User(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    "owner@saraswatifabrics.in",
                    passwordEncoder.encode("password123"),
                    "Saraswati Fabrics Owner",
                    UserRole.ROLE_MSME,
                    "9876543210",
                    UserStatus.ACTIVE
            );
            userRepository.save(msmeUser);

            // Seed Business for MSME Owner
            Business business = new Business(
                    UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    msmeUser,
                    "Saraswati Fabrics Private Limited",
                    "Saraswati Fabrics",
                    "27AAAAA1111A1Z1",
                    "AAAAA1111A",
                    "UDYAM-MH-33-0000001",
                    LocalDate.of(2015, 6, 15),
                    "Private Limited Company",
                    "Textile Manufacturing",
                    "102, Industrial Estate, Lower Parel",
                    "Senapati Bapat Marg",
                    "Mumbai",
                    "Maharashtra",
                    "400013"
            );
            businessRepository.save(business);

            // Sync all alternate data for this business automatically
            dataIngestService.syncAlternateData(business.getId(), "ALL");
        }

        // 2. Seed Lender Officer
        if (!userRepository.existsByEmail("credit.mgr@idbi.com")) {
            User lenderUser = new User(
                    UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    "credit.mgr@idbi.com",
                    passwordEncoder.encode("password123"),
                    "IDBI Credit Manager",
                    UserRole.ROLE_CREDIT_MANAGER,
                    "9876543211",
                    UserStatus.ACTIVE
            );
            userRepository.save(lenderUser);
        }

        // 3. Seed Platform Admin
        if (!userRepository.existsByEmail("sys.admin@idbi.com")) {
            User adminUser = new User(
                    UUID.fromString("44444444-4444-4444-4444-444444444444"),
                    "sys.admin@idbi.com",
                    passwordEncoder.encode("password123"),
                    "System Administrator",
                    UserRole.ROLE_ADMIN,
                    "9876543212",
                    UserStatus.ACTIVE
            );
            userRepository.save(adminUser);
        }
    }
}

package com.gadgetgo.config;

import com.gadgetgo.catalog.domain.RepairService;
import com.gadgetgo.catalog.repository.RepairServiceRepository;
import com.gadgetgo.customer.domain.CustomerAccount;
import com.gadgetgo.customer.domain.CustomerRole;
import com.gadgetgo.customer.repository.CustomerAccountRepository;
import com.gadgetgo.supplier.domain.SupplierProfile;
import com.gadgetgo.supplier.repository.SupplierProfileRepository;
import com.gadgetgo.technician.domain.TechnicianAvailability;
import com.gadgetgo.technician.domain.TechnicianProfile;
import com.gadgetgo.technician.domain.TechnicianServiceMode;
import com.gadgetgo.technician.repository.TechnicianProfileRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Local-only, environment-configured demo account provisioning.
 * Never enabled in production or outside the "local" Spring profile.
 * Provisions 5 default technician accounts and 5 default supplier accounts for local testing.
 */
@Component
@Profile("local")
@ConditionalOnProperty(name = "app.provisioning.demo-accounts.enabled", havingValue = "true")
public class DemoAccountProvisioner implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(DemoAccountProvisioner.class.getName());

    private final CustomerAccountRepository accountRepository;
    private final TechnicianProfileRepository technicianRepository;
    private final SupplierProfileRepository supplierRepository;
    private final RepairServiceRepository repairServiceRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public record TechnicianSeedSpec(
            String primaryEmailKey, String fallbackEmailKey, String defaultEmail,
            String primaryNameKey, String fallbackNameKey, String defaultName,
            String primaryPasswordKey, String fallbackPasswordKey,
            String primaryAreaKey, String fallbackAreaKey, String defaultArea,
            String primaryModesKey, String fallbackModesKey, String defaultModes,
            String primaryCapacityKey, int defaultCapacity,
            String primarySkillsKey, String fallbackSkillsKey, String defaultSkills
    ) {}

    public record SupplierSeedSpec(
            String primaryEmailKey, String fallbackEmailKey, String defaultEmail,
            String primaryNameKey, String fallbackNameKey, String defaultName,
            String primaryPasswordKey, String fallbackPasswordKey,
            String primaryBusinessKey, String fallbackBusinessKey, String defaultBusiness,
            String primaryContactKey, String fallbackContactKey, String defaultContact
    ) {}

    private static final List<TechnicianSeedSpec> TECHNICIAN_SPECS = List.of(
            new TechnicianSeedSpec(
                    "DEMO_TECH1_EMAIL", "DEMO_TECHNICIAN_EMAIL", "alex.rivera@gadgetgo.local",
                    "DEMO_TECH1_NAME", "DEMO_TECHNICIAN_NAME", "Alex Rivera",
                    "DEMO_TECH1_PASSWORD", "DEMO_TECHNICIAN_PASSWORD",
                    "DEMO_TECH1_SERVICE_AREA", "DEMO_TECHNICIAN_SERVICE_AREA", "Metro Central & Downtown",
                    "DEMO_TECH1_SERVICE_MODES", "DEMO_TECHNICIAN_SERVICE_MODES", "HOME_VISIT,REPAIR_CENTRE",
                    "DEMO_TECH1_CAPACITY", 5,
                    "DEMO_TECH1_SKILLS", "DEMO_TECHNICIAN_REPAIR_SERVICE_IDS",
                    "smartphone-screen-repair,smartphone-battery-replacement,smartphone-charging-port-repair,smartphone-button-repair,smartphone-camera-repair,smartphone-speaker-microphone-repair,tablet-screen-repair,tablet-battery-replacement,tablet-charging-port-repair,tablet-button-repair"
            ),
            new TechnicianSeedSpec(
                    "DEMO_TECH2_EMAIL", null, "sam.chen@gadgetgo.local",
                    "DEMO_TECH2_NAME", null, "Sam Chen",
                    "DEMO_TECH2_PASSWORD", null,
                    "DEMO_TECH2_SERVICE_AREA", null, "North District & Suburbs",
                    "DEMO_TECH2_SERVICE_MODES", null, "HOME_VISIT,REPAIR_CENTRE",
                    "DEMO_TECH2_CAPACITY", 4,
                    "DEMO_TECH2_SKILLS", null,
                    "smartphone-screen-repair,smartphone-battery-replacement,smartphone-button-repair,smartwatch-screen-repair,smartwatch-battery-replacement,smartwatch-button-repair,smartwatch-charging-contact-repair"
            ),
            new TechnicianSeedSpec(
                    "DEMO_TECH3_EMAIL", null, "jordan.taylor@gadgetgo.local",
                    "DEMO_TECH3_NAME", null, "Jordan Taylor",
                    "DEMO_TECH3_PASSWORD", null,
                    "DEMO_TECH3_SERVICE_AREA", null, "Westside Tech Corridor",
                    "DEMO_TECH3_SERVICE_MODES", null, "REPAIR_CENTRE",
                    "DEMO_TECH3_CAPACITY", 4,
                    "DEMO_TECH3_SKILLS", null,
                    "laptop-screen-repair,laptop-battery-replacement,laptop-keyboard-repair,laptop-charging-port-repair,laptop-trackpad-repair,laptop-overheating-inspection,desktop-storage-replacement,desktop-cooling-inspection,desktop-connectivity-repair"
            ),
            new TechnicianSeedSpec(
                    "DEMO_TECH4_EMAIL", null, "morgan.patel@gadgetgo.local",
                    "DEMO_TECH4_NAME", null, "Morgan Patel",
                    "DEMO_TECH4_PASSWORD", null,
                    "DEMO_TECH4_SERVICE_AREA", null, "East Valley & Surrounds",
                    "DEMO_TECH4_SERVICE_MODES", null, "HOME_VISIT",
                    "DEMO_TECH4_CAPACITY", 3,
                    "DEMO_TECH4_SKILLS", null,
                    "smartphone-screen-repair,laptop-screen-repair,laptop-battery-replacement,laptop-charging-port-repair,laptop-keyboard-repair,laptop-trackpad-repair,laptop-overheating-inspection,desktop-power-inspection"
            ),
            new TechnicianSeedSpec(
                    "DEMO_TECH5_EMAIL", null, "casey.kim@gadgetgo.local",
                    "DEMO_TECH5_NAME", null, "Casey Kim",
                    "DEMO_TECH5_PASSWORD", null,
                    "DEMO_TECH5_SERVICE_AREA", null, "South Metro Area",
                    "DEMO_TECH5_SERVICE_MODES", null, "HOME_VISIT,REPAIR_CENTRE",
                    "DEMO_TECH5_CAPACITY", 4,
                    "DEMO_TECH5_SKILLS", null,
                    "smartphone-screen-repair,smartphone-camera-repair,smartphone-button-repair,tablet-charging-port-repair,laptop-charging-port-repair,smartwatch-screen-repair,smartwatch-button-repair,tablet-screen-repair,smartwatch-charging-contact-repair"
            )
    );

    private static final List<SupplierSeedSpec> SUPPLIER_SPECS = List.of(
            new SupplierSeedSpec(
                    "DEMO_SUPP1_EMAIL", "DEMO_SUPPLIER_EMAIL", "marcus.vance@gadgetgo.local",
                    "DEMO_SUPP1_NAME", "DEMO_SUPPLIER_NAME", "Marcus Vance",
                    "DEMO_SUPP1_PASSWORD", "DEMO_SUPPLIER_PASSWORD",
                    "DEMO_SUPP1_BUSINESS_NAME", "DEMO_SUPPLIER_BUSINESS_NAME", "Apex Mobile Spares Ltd",
                    "DEMO_SUPP1_CONTACT_EMAIL", "DEMO_SUPPLIER_CONTACT_EMAIL", "contact@apexmobilespares.local"
            ),
            new SupplierSeedSpec(
                    "DEMO_SUPP2_EMAIL", null, "elena.rostova@gadgetgo.local",
                    "DEMO_SUPP2_NAME", null, "Elena Rostova",
                    "DEMO_SUPP2_PASSWORD", null,
                    "DEMO_SUPP2_BUSINESS_NAME", null, "Silicon Circuit Wholesale",
                    "DEMO_SUPP2_CONTACT_EMAIL", null, "orders@siliconcircuit.local"
            ),
            new SupplierSeedSpec(
                    "DEMO_SUPP3_EMAIL", null, "david.oconnor@gadgetgo.local",
                    "DEMO_SUPP3_NAME", null, "David O'Connor",
                    "DEMO_SUPP3_PASSWORD", null,
                    "DEMO_SUPP3_BUSINESS_NAME", null, "ByteParts Direct & Logistics",
                    "DEMO_SUPP3_CONTACT_EMAIL", null, "support@bytepartsdirect.local"
            ),
            new SupplierSeedSpec(
                    "DEMO_SUPP4_EMAIL", null, "amina.diallo@gadgetgo.local",
                    "DEMO_SUPP4_NAME", null, "Amina Diallo",
                    "DEMO_SUPP4_PASSWORD", null,
                    "DEMO_SUPP4_BUSINESS_NAME", null, "Precision Micro Displays",
                    "DEMO_SUPP4_CONTACT_EMAIL", null, "sales@precisionmicro.local"
            ),
            new SupplierSeedSpec(
                    "DEMO_SUPP5_EMAIL", null, "lucas.lindqvist@gadgetgo.local",
                    "DEMO_SUPP5_NAME", null, "Lucas Lindqvist",
                    "DEMO_SUPP5_PASSWORD", null,
                    "DEMO_SUPP5_BUSINESS_NAME", null, "VoltCraft Power Systems",
                    "DEMO_SUPP5_CONTACT_EMAIL", null, "b2b@voltcraftpower.local"
            )
    );

    public DemoAccountProvisioner(CustomerAccountRepository accountRepository,
                                  TechnicianProfileRepository technicianRepository,
                                  SupplierProfileRepository supplierRepository,
                                  RepairServiceRepository repairServiceRepository,
                                  PasswordEncoder passwordEncoder,
                                  Environment environment) {
        this.accountRepository = accountRepository;
        this.technicianRepository = technicianRepository;
        this.supplierRepository = supplierRepository;
        this.repairServiceRepository = repairServiceRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String defaultPassword = resolveProperty("DEMO_DEFAULT_PASSWORD", null);

        int techniciansCreated = 0;
        for (TechnicianSeedSpec spec : TECHNICIAN_SPECS) {
            if (provisionTechnician(spec, defaultPassword)) {
                techniciansCreated++;
            }
        }

        int suppliersCreated = 0;
        for (SupplierSeedSpec spec : SUPPLIER_SPECS) {
            if (provisionSupplier(spec, defaultPassword)) {
                suppliersCreated++;
            }
        }

        LOGGER.info(String.format(
                "Local demo provider provisioning complete: %d new technicians, %d new suppliers. Credentials were not logged.",
                techniciansCreated, suppliersCreated));
    }

    private boolean provisionTechnician(TechnicianSeedSpec spec, String defaultPassword) {
        String email = resolveProperty(spec.primaryEmailKey(), spec.fallbackEmailKey(), spec.defaultEmail());
        String name = resolveProperty(spec.primaryNameKey(), spec.fallbackNameKey(), spec.defaultName());
        String password = resolveProperty(spec.primaryPasswordKey(), spec.fallbackPasswordKey(), defaultPassword);

        validatePassword(password, "Password for technician " + email + " (set DEMO_DEFAULT_PASSWORD or " + spec.primaryPasswordKey() + ")");

        CustomerAccount account = findOrCreateAccount(email, name, password, CustomerRole.TECHNICIAN);
        String serviceArea = resolveProperty(spec.primaryAreaKey(), spec.fallbackAreaKey(), spec.defaultArea());
        String serviceModes = resolveProperty(spec.primaryModesKey(), spec.fallbackModesKey(), spec.defaultModes());
        int capacity = resolveIntProperty(spec.primaryCapacityKey(), spec.defaultCapacity());
        String skills = resolveProperty(spec.primarySkillsKey(), spec.fallbackSkillsKey(), spec.defaultSkills());

        TechnicianProfile existingProfile = technicianRepository.findByAccount_Id(account.getId()).orElse(null);
        if (existingProfile != null) {
            existingProfile.setServiceModes(parseModes(serviceModes));
            existingProfile.setSkills(resolveRepairServices(skills));
            existingProfile.setCapacityLimit(capacity);
            if (serviceArea != null && !serviceArea.isBlank()) {
                existingProfile.setServiceArea(serviceArea.trim());
            }
            technicianRepository.save(existingProfile);
            return false;
        }

        TechnicianProfile profile = new TechnicianProfile(account, true, blankToNull(serviceArea),
                TechnicianAvailability.AVAILABLE, capacity);
        profile.setServiceModes(parseModes(serviceModes));
        profile.setSkills(resolveRepairServices(skills));
        technicianRepository.save(profile);
        return true;
    }

    private boolean provisionSupplier(SupplierSeedSpec spec, String defaultPassword) {
        String email = resolveProperty(spec.primaryEmailKey(), spec.fallbackEmailKey(), spec.defaultEmail());
        String name = resolveProperty(spec.primaryNameKey(), spec.fallbackNameKey(), spec.defaultName());
        String password = resolveProperty(spec.primaryPasswordKey(), spec.fallbackPasswordKey(), defaultPassword);

        validatePassword(password, "Password for supplier " + email + " (set DEMO_DEFAULT_PASSWORD or " + spec.primaryPasswordKey() + ")");

        CustomerAccount account = findOrCreateAccount(email, name, password, CustomerRole.SUPPLIER);
        if (supplierRepository.findByAccount_Id(account.getId()).isPresent()) {
            return false;
        }

        String businessName = resolveProperty(spec.primaryBusinessKey(), spec.fallbackBusinessKey(), spec.defaultBusiness());
        String contactEmail = resolveProperty(spec.primaryContactKey(), spec.fallbackContactKey(), spec.defaultContact());

        supplierRepository.save(new SupplierProfile(account, businessName, contactEmail, true));
        return true;
    }

    private CustomerAccount findOrCreateAccount(String emailValue, String name, String rawPassword, CustomerRole role) {
        String email = emailValue.trim().toLowerCase(Locale.ROOT);
        CustomerAccount existing = accountRepository.findByEmail(email).orElse(null);
        if (existing != null) {
            if (existing.getRole() != role) {
                throw new IllegalStateException("A configured local demo email is already assigned to a different role: " + email);
            }
            if (!passwordEncoder.matches(rawPassword, existing.getPasswordHash())) {
                existing.updatePassword(passwordEncoder.encode(rawPassword));
                return accountRepository.saveAndFlush(existing);
            }
            return existing;
        }
        return accountRepository.saveAndFlush(new CustomerAccount(name.trim(), email,
                passwordEncoder.encode(rawPassword), role));
    }

    private Set<RepairService> resolveRepairServices(String configuredTokens) {
        if (configuredTokens == null || configuredTokens.isBlank()) return Set.of();
        Set<RepairService> result = new LinkedHashSet<>();
        String[] tokens = configuredTokens.split(",");
        for (String rawToken : tokens) {
            String token = rawToken.trim();
            if (token.isEmpty()) continue;
            RepairService service = null;
            if (token.matches("\\d+")) {
                service = repairServiceRepository.findById(Long.parseLong(token)).orElse(null);
            }
            if (service == null) {
                service = repairServiceRepository.findBySlugAndActiveTrueAndCategory_ActiveTrue(token).orElse(null);
            }
            if (service == null || !service.isActive() || !service.getCategory().isActive()) {
                throw new IllegalStateException("Configured demo technician skill '" + token + "' must reference an active catalogue service.");
            }
            result.add(service);
        }
        return result;
    }

    private static Set<TechnicianServiceMode> parseModes(String configuredModes) {
        if (configuredModes == null || configuredModes.isBlank()) return Set.of();
        EnumSet<TechnicianServiceMode> result = EnumSet.noneOf(TechnicianServiceMode.class);
        try {
            Arrays.stream(configuredModes.split(","))
                    .map(String::trim).filter(value -> !value.isEmpty())
                    .map(TechnicianServiceMode::valueOf).forEach(result::add);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("DEMO_TECHNICIAN_SERVICE_MODES must use HOME_VISIT and/or REPAIR_CENTRE.");
        }
        return result;
    }

    private static void validatePassword(String value, String propertyDescription) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyDescription + " must be configured when local demo provisioning is enabled.");
        }
        if (value.length() < 10 || value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(propertyDescription + " must contain 10–72 UTF-8 bytes.");
        }
    }

    private String resolveProperty(String primaryKey, String defaultValue) {
        String val = environment.getProperty(primaryKey);
        return (val != null && !val.isBlank()) ? val.trim() : defaultValue;
    }

    private String resolveProperty(String primaryKey, String fallbackKey, String defaultValue) {
        String val = environment.getProperty(primaryKey);
        if (val != null && !val.isBlank()) return val.trim();
        if (fallbackKey != null) {
            String fallbackVal = environment.getProperty(fallbackKey);
            if (fallbackVal != null && !fallbackVal.isBlank()) return fallbackVal.trim();
        }
        return defaultValue;
    }

    private int resolveIntProperty(String primaryKey, int defaultValue) {
        String val = environment.getProperty(primaryKey);
        if (val != null && !val.isBlank()) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

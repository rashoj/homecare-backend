package com.homecare.config;

import com.homecare.entity.*;
import com.homecare.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo@12345";
    private static final String ADMIN_EMAIL = "admin@carebridge-demo.com";

    @Value("${demo.seed.enabled:false}")
    private boolean demoSeedEnabled;

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClockRecordRepository clockRecordRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentReviewRepository incidentReviewRepository;
    private final ServiceDocumentationRepository serviceDocumentationRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepository,
            ClientRepository clientRepository,
            MedicationRepository medicationRepository,
            MedicationLogRepository medicationLogRepository,
            AppointmentRepository appointmentRepository,
            ClockRecordRepository clockRecordRepository,
            VisitNoteRepository visitNoteRepository,
            IncidentRepository incidentRepository,
            IncidentReviewRepository incidentReviewRepository,
            ServiceDocumentationRepository serviceDocumentationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.medicationRepository = medicationRepository;
        this.medicationLogRepository = medicationLogRepository;
        this.appointmentRepository = appointmentRepository;
        this.clockRecordRepository = clockRecordRepository;
        this.visitNoteRepository = visitNoteRepository;
        this.incidentRepository = incidentRepository;
        this.incidentReviewRepository = incidentReviewRepository;
        this.serviceDocumentationRepository = serviceDocumentationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!demoSeedEnabled) {
            return;
        }

        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            System.out.println("Demo data already exists. Skipping seed.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);

        User admin = createUser(
                "CareBridge Admin",
                ADMIN_EMAIL,
                encodedPassword,
                Role.ADMIN
        );

        User supervisor = createUser(
                "Olivia Supervisor",
                "supervisor@carebridge-demo.com",
                encodedPassword,
                Role.ADMIN
        );

        List<User> caregivers = createCaregivers(encodedPassword);
        List<Client> clients = createClients();

        for (int index = 0; index < clients.size(); index++) {
            Client client = clients.get(index);
            User caregiver = caregivers.get(index % caregivers.size());

            seedMedicationWorkflow(client, caregiver, index);
            seedAppointmentCareWorkflow(client, caregiver, supervisor, index);
        }

        System.out.println("Expanded CareBridge demo data seeded successfully.");
        System.out.println("Admin login: " + ADMIN_EMAIL + " / " + DEMO_PASSWORD);
        System.out.println("Supervisor login: supervisor@carebridge-demo.com / " + DEMO_PASSWORD);
        System.out.println("Caregiver login: alex@carebridge-demo.com / " + DEMO_PASSWORD);
        System.out.println("Demo Admin User ID: " + admin.getId());
    }

    private List<User> createCaregivers(String password) {
        return List.of(
                createUser("Alex Uprety", "alex@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Sarah Caregiver", "sarah@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Maria Johnson", "maria.caregiver@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("David Miller", "david@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Emily Carter", "emily@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("James Wilson", "james@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Priya Sharma", "priya@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Kevin Brown", "kevin@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Linda Davis", "linda.caregiver@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Robert Lee", "robert.caregiver@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Amina Yusuf", "amina@carebridge-demo.com", password, Role.CAREGIVER),
                createUser("Daniel Thompson", "daniel@carebridge-demo.com", password, Role.CAREGIVER)
        );
    }

    private List<Client> createClients() {
        return List.of(
                createClient("Prakash Thapa", 1958, 4, 12, "123 Demo Street, Harrisburg, PA"),
                createClient("John Smith", 1949, 8, 22, "221 Market Street, Harrisburg, PA"),
                createClient("Maria Lopez", 1962, 2, 7, "55 Green Lane, Lancaster, PA"),
                createClient("Robert Williams", 1951, 11, 15, "88 North Street, Harrisburg, PA"),
                createClient("Linda Carter", 1947, 6, 3, "14 Walnut Avenue, Lancaster, PA"),
                createClient("Samuel Johnson", 1956, 9, 28, "909 Pine Road, Harrisburg, PA"),
                createClient("Patricia Brown", 1950, 1, 19, "12 River Road, Lancaster, PA"),
                createClient("George Anderson", 1944, 12, 2, "43 Oak Drive, Harrisburg, PA"),
                createClient("Nancy Wilson", 1959, 5, 18, "71 Maple Court, Lancaster, PA"),
                createClient("Thomas Garcia", 1953, 7, 9, "66 College Avenue, Harrisburg, PA"),
                createClient("Helen Martinez", 1948, 3, 25, "21 Queen Street, Lancaster, PA"),
                createClient("William Davis", 1955, 10, 30, "311 Chestnut Street, Harrisburg, PA"),
                createClient("Barbara Miller", 1952, 2, 14, "78 King Street, Lancaster, PA"),
                createClient("Richard Moore", 1946, 4, 6, "10 Front Street, Harrisburg, PA"),
                createClient("Elizabeth Taylor", 1960, 8, 11, "5 Prince Street, Lancaster, PA"),
                createClient("Joseph Thomas", 1957, 9, 5, "17 State Street, Harrisburg, PA"),
                createClient("Susan Jackson", 1949, 11, 21, "45 Lime Street, Lancaster, PA"),
                createClient("Charles White", 1954, 12, 13, "93 Union Street, Harrisburg, PA"),
                createClient("Karen Harris", 1956, 6, 17, "120 Orange Street, Lancaster, PA"),
                createClient("Donald Martin", 1951, 1, 29, "6 Liberty Street, Harrisburg, PA")
        );
    }

    private void seedMedicationWorkflow(Client client, User caregiver, int index) {
        Medication morningMed = createMedication(
                client,
                "Metformin",
                "500mg",
                "DAILY",
                LocalTime.of(8, 0),
                "Give with breakfast."
        );

        Medication eveningMed = createMedication(
                client,
                "Lisinopril",
                "10mg",
                "DAILY",
                LocalTime.of(18, 0),
                "Monitor blood pressure before administration."
        );

        createMedicationLog(
                morningMed,
                client,
                caregiver,
                LocalDate.now().atTime(8, 0),
                "GIVEN",
                "Medication given with breakfast.",
                null,
                null,
                null,
                caregiver.getFullName()
        );

        if (index % 5 == 0) {
            createMedicationLog(
                    eveningMed,
                    client,
                    caregiver,
                    LocalDate.now().atTime(18, 0),
                    "MISSED",
                    "Dose was not administered.",
                    null,
                    "Client was unavailable during scheduled medication pass.",
                    null,
                    caregiver.getFullName()
            );
        } else if (index % 6 == 0) {
            createMedicationLog(
                    eveningMed,
                    client,
                    caregiver,
                    LocalDate.now().atTime(18, 0),
                    "REFUSED",
                    "Client refused after education was provided.",
                    null,
                    null,
                    "Client refused medication.",
                    caregiver.getFullName()
            );
        } else if (index % 7 == 0) {
            createMedicationLog(
                    eveningMed,
                    client,
                    caregiver,
                    LocalDateTime.now().minusHours(2),
                    "PRN_GIVEN",
                    "PRN medication effective after 30 minutes.",
                    "Client reported headache.",
                    null,
                    null,
                    caregiver.getFullName()
            );
        }
    }

    private void seedAppointmentCareWorkflow(
            Client client,
            User caregiver,
            User supervisor,
            int index
    ) {
        LocalDateTime shiftStart = LocalDate.now().atTime(9 + (index % 5), 0);
        LocalDateTime shiftEnd = shiftStart.plusHours(3);
        boolean missedShift = index % 6 == 0;

        Appointment appointment = appointmentRepository.save(
                Appointment.builder()
                        .client(client)
                        .caregiver(caregiver)
                        .startTime(shiftStart)
                        .endTime(shiftEnd)
                        .serviceType(index % 3 == 0 ? "PERSONAL_CARE" : "ADL_ASSISTANCE")
                        .shiftType("REGULAR")
                        .status(missedShift ? "MISSED" : "COMPLETED")
                        .evvRequired(true)
                        .billable(true)
                        .repeatType("NONE")
                        .completed(!missedShift)
                        .notes("Demo scheduled care shift for investor walkthrough.")
                        .build()
        );

        if (missedShift) {
            seedMissedShiftIncident(appointment, client, caregiver, supervisor, index);
            return;
        }

        seedClockRecord(appointment, shiftStart, shiftEnd);
        seedVisitNote(appointment, client, caregiver, index);
        seedServiceDocumentation(appointment, client, caregiver, shiftStart, shiftEnd, index);

        if (index % 7 == 0) {
            seedCareIncident(appointment, client, caregiver, supervisor, index);
        }
    }

    private void seedClockRecord(
            Appointment appointment,
            LocalDateTime shiftStart,
            LocalDateTime shiftEnd
    ) {
        clockRecordRepository.save(
                ClockRecord.builder()
                        .appointment(appointment)
                        .clockInTime(shiftStart.minusMinutes(3))
                        .clockOutTime(shiftEnd.plusMinutes(4))
                        .clockInLatitude(40.2732)
                        .clockInLongitude(-76.8867)
                        .clockOutLatitude(40.2732)
                        .clockOutLongitude(-76.8867)
                        .status("CLOCKED_OUT")
                        .clockInNotes("Caregiver arrived and GPS verified.")
                        .clockOutNotes("Shift completed successfully.")
                        .build()
        );
    }

    private void seedVisitNote(
            Appointment appointment,
            Client client,
            User caregiver,
            int index
    ) {
        visitNoteRepository.save(
                VisitNote.builder()
                        .appointment(appointment)
                        .client(client)
                        .caregiver(caregiver)
                        .generalNotes("Client was comfortable and cooperative during the shift.")
                        .meals("Assisted with meal preparation and hydration reminders.")
                        .medicationNotes("Medication reminders completed according to care plan.")
                        .mobilityNotes("Client ambulated safely with standby assistance.")
                        .moodNotes("Client was calm and engaged.")
                        .hygieneCare("Assisted with grooming and light hygiene support.")
                        .safetyConcerns(index % 7 == 0 ? "Fall risk reviewed with client." : "No safety concerns observed.")
                        .familyUpdate("Family updated on client status.")
                        .aiSummary("Client remained stable. ADL support and medication reminders were completed.")
                        .incidentReported(index % 7 == 0)
                        .incidentDetails(index % 7 == 0 ? "Minor safety concern documented for supervisor review." : null)
                        .build()
        );
    }

    private void seedServiceDocumentation(
            Appointment appointment,
            Client client,
            User caregiver,
            LocalDateTime shiftStart,
            LocalDateTime shiftEnd,
            int index
    ) {
        boolean rejected = index % 5 == 0;
        boolean timeCorrected = index % 4 == 0;

        serviceDocumentationRepository.save(
                ServiceDocumentation.builder()
                        .appointment(appointment)
                        .client(client)
                        .caregiver(caregiver)
                        .shiftTasksCompleted("Medication reminders, meal prep, mobility assistance, safety monitoring.")
                        .adlsCompleted("Bathing assistance, grooming support, toileting reminders.")
                        .goalProgressNotes("Client demonstrated improved engagement and mobility today.")
                        .dailyServiceNotes("Shift completed successfully with no major concerns.")
                        .shiftCompleted(true)
                        .caregiverSignature(caregiver.getFullName())
                        .status(rejected ? "REJECTED" : "APPROVED")
                        .locked(!rejected)
                        .supervisorComments(
                                rejected
                                        ? "Please provide more detailed ADL notes."
                                        : "Documentation approved."
                        )
                        .submittedAt(LocalDateTime.now().minusHours(1))
                        .approvedAt(rejected ? null : LocalDateTime.now())
                        .correctedClockInTime(timeCorrected ? shiftStart.minusMinutes(2) : null)
                        .correctedClockOutTime(timeCorrected ? shiftEnd.plusMinutes(6) : null)
                        .timeCorrectionApproved(timeCorrected)
                        .correctionReason(
                                timeCorrected
                                        ? "Clock-out time adjusted after supervisor review."
                                        : null
                        )
                        .build()
        );
    }

    private void seedCareIncident(
            Appointment appointment,
            Client client,
            User caregiver,
            User supervisor,
            int index
    ) {
        Incident incident = incidentRepository.save(
                Incident.builder()
                        .appointment(appointment)
                        .client(client)
                        .caregiver(caregiver)
                        .incidentDateTime(LocalDateTime.now().minusHours(2))
                        .incidentType(index % 2 == 0 ? "FALL_RISK" : "BEHAVIORAL_INCIDENT")
                        .severity(index % 3 == 0 ? "HIGH" : "MEDIUM")
                        .description("Client experienced a minor behavioral escalation during shift.")
                        .immediateActionTaken("Caregiver de-escalated situation and notified supervisor.")
                        .witnessName("Sarah Johnson")
                        .witnessPhone("717-555-9090")
                        .witnessStatement("Client became verbally escalated but calmed after intervention.")
                        .status("UNDER_REVIEW")
                        .stateReportable(index % 3 == 0)
                        .build()
        );

        incidentReviewRepository.save(
                IncidentReview.builder()
                        .incident(incident)
                        .reviewedBy(supervisor)
                        .reviewStatus(index % 3 == 0 ? "UNDER_REVIEW" : "RESOLVED")
                        .supervisorNotes("Supervisor reviewed incident and follow-up was initiated.")
                        .correctiveAction("Behavior support strategies reinforced with staff.")
                        .followUpRequired("Continue monitoring during future visits.")
                        .reviewedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void seedMissedShiftIncident(
            Appointment appointment,
            Client client,
            User caregiver,
            User supervisor,
            int index
    ) {
        Incident incident = incidentRepository.save(
                Incident.builder()
                        .appointment(appointment)
                        .client(client)
                        .caregiver(caregiver)
                        .incidentDateTime(LocalDateTime.now().minusHours(4))
                        .incidentType("MISSED_VISIT")
                        .severity("HIGH")
                        .description("Scheduled caregiver shift was missed and required supervisor review.")
                        .immediateActionTaken("Supervisor contacted caregiver and reassigned coverage.")
                        .witnessName(null)
                        .witnessPhone(null)
                        .witnessStatement(null)
                        .status("UNDER_REVIEW")
                        .stateReportable(index % 2 == 0)
                        .build()
        );

        incidentReviewRepository.save(
                IncidentReview.builder()
                        .incident(incident)
                        .reviewedBy(supervisor)
                        .reviewStatus("UNDER_REVIEW")
                        .supervisorNotes("Missed shift flagged for follow-up and staffing review.")
                        .correctiveAction("Caregiver attendance pattern will be reviewed.")
                        .followUpRequired("Scheduler must confirm backup coverage workflow.")
                        .reviewedAt(LocalDateTime.now())
                        .build()
        );
    }

    private User createUser(String fullName, String email, String password, Role role) {
        return userRepository.save(
                User.builder()
                        .fullName(fullName)
                        .email(email)
                        .password(password)
                        .role(role)
                        .phoneNumber("717-555-0100")
                        .active(true)
                        .build()
        );
    }

    private Client createClient(
            String fullName,
            int year,
            int month,
            int day,
            String address
    ) {
        return clientRepository.save(
                Client.builder()
                        .fullName(fullName)
                        .dateOfBirth(LocalDate.of(year, month, day))
                        .phoneNumber("717-555-0200")
                        .address(address)
                        .latitude(40.2732)
                        .longitude(-76.8867)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    private Medication createMedication(
            Client client,
            String name,
            String dosage,
            String frequency,
            LocalTime scheduledTime,
            String instructions
    ) {
        return medicationRepository.save(
                Medication.builder()
                        .client(client)
                        .medicationName(name)
                        .dosage(dosage)
                        .frequency(frequency)
                        .scheduledTime(scheduledTime)
                        .instructions(instructions)
                        .active(true)
                        .build()
        );
    }

    private void createMedicationLog(
            Medication medication,
            Client client,
            User caregiver,
            LocalDateTime scheduledAt,
            String status,
            String notes,
            String prnReason,
            String missedReason,
            String refusalReason,
            String caregiverSignature
    ) {
        medicationLogRepository.save(
                MedicationLog.builder()
                        .medication(medication)
                        .client(client)
                        .caregiver(caregiver)
                        .scheduledAt(scheduledAt)
                        .status(status)
                        .notes(notes)
                        .prn("PRN_GIVEN".equals(status))
                        .prnReason(prnReason)
                        .missedReason(missedReason)
                        .refusalReason(refusalReason)
                        .caregiverSignature(caregiverSignature)
                        .build()
        );
    }
}
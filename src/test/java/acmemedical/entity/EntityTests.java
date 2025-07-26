/********************************************************************************************************
 * File:  EntityTests.java Course Materials CST 8277
 *
 * @author Yizhen Xu
 * 
 * Entity Layer Tests - 10+ tests focusing on entity classes
 */
package acmemedical.entity; 

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import acmemedical.entity.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityTests {

    private Physician physician;
    private Patient patient;
    private Medicine medicine;
    private MedicalSchool medicalSchool;
    private MedicalTraining medicalTraining;
    private MedicalCertificate medicalCertificate;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        // Initialize test entities
        physician = new Physician();
        patient = new Patient();
        medicine = new Medicine();
        medicalSchool = new PublicSchool();
        medicalTraining = new MedicalTraining();
        medicalCertificate = new MedicalCertificate();
        prescription = new Prescription();
    }

    @Test
    @Order(1)
    void testPhysicianEntityCreation() {
        physician.setFirstName("John");
        physician.setLastName("Doe");
        
        assertNotNull(physician);
        assertEquals("John", physician.getFirstName());
        assertEquals("Doe", physician.getLastName());
        assertNotNull(physician.getMedicalCertificates());
        assertNotNull(physician.getPrescriptions());
        assertTrue(physician.getMedicalCertificates().isEmpty());
        assertTrue(physician.getPrescriptions().isEmpty());
    }

    @Test
    @Order(2)
    void testPatientEntityCreation() {
        patient.setFirstName("Jane");
        patient.setLastName("Smith");
        patient.setYear(1990);
        patient.setAddress("123 Main St");
        patient.setHeight(170);
        patient.setWeight(65);
        patient.setSmoker((byte) 0);
        
        assertNotNull(patient);
        assertEquals("Jane", patient.getFirstName());
        assertEquals("Smith", patient.getLastName());
        assertEquals(1990, patient.getYear());
        assertEquals("123 Main St", patient.getAddress());
        assertEquals(170, patient.getHeight());
        assertEquals(65, patient.getWeight());
        assertEquals((byte) 0, patient.getSmoker());
        assertNotNull(patient.getPrescriptions());
    }

    @Test
    @Order(3)
    void testMedicineEntityCreation() {
        medicine.setDrugName("Aspirin");
        medicine.setManufacturerName("Bayer");
        medicine.setDosageInformation("Take 1 tablet daily");
        medicine.setChemicalName("Acetylsalicylic acid");
        medicine.setGenericName("ASA");
        
        assertNotNull(medicine);
        assertEquals("Aspirin", medicine.getDrugName());
        assertEquals("Bayer", medicine.getManufacturerName());
        assertEquals("Take 1 tablet daily", medicine.getDosageInformation());
        assertEquals("Acetylsalicylic acid", medicine.getChemicalName());
        assertEquals("ASA", medicine.getGenericName());
        assertNotNull(medicine.getPrescriptions());
    }

    @Test
    @Order(4)
    void testPublicSchoolInheritance() {
        PublicSchool publicSchool = new PublicSchool();
        publicSchool.setName("University of Toronto Medical School");
        
        assertNotNull(publicSchool);
        assertTrue(publicSchool instanceof MedicalSchool);
        assertEquals("University of Toronto Medical School", publicSchool.getName());
        assertNotNull(publicSchool.getMedicalTrainings());
    }

    @Test
    @Order(5)
    void testPrivateSchoolInheritance() {
        PrivateSchool privateSchool = new PrivateSchool();
        privateSchool.setName("Harvard Medical School");
        
        assertNotNull(privateSchool);
        assertTrue(privateSchool instanceof MedicalSchool);
        assertEquals("Harvard Medical School", privateSchool.getName());
        assertNotNull(privateSchool.getMedicalTrainings());
    }

    @Test
    @Order(6)
    void testDurationAndStatusEmbeddable() {
        DurationAndStatus duration = new DurationAndStatus();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(6);
        
        duration.setStartDate(startDate);
        duration.setEndDate(endDate);
        duration.setActive((byte) 1);
        
        assertNotNull(duration);
        assertEquals(startDate, duration.getStartDate());
        assertEquals(endDate, duration.getEndDate());
        assertEquals((byte) 1, duration.getActive());
    }

    @Test
    @Order(7)
    void testMedicalTrainingEntityWithEmbedded() {
        PublicSchool school = new PublicSchool();
        school.setName("Test Medical School");
        
        DurationAndStatus duration = new DurationAndStatus();
        duration.setStartDate(LocalDateTime.now());
        duration.setEndDate(LocalDateTime.now().plusYears(4));
        duration.setActive((byte) 1);
        
        medicalTraining.setMedicalSchool(school);
        medicalTraining.setDurationAndStatus(duration);
        
        assertNotNull(medicalTraining);
        assertEquals(school, medicalTraining.getMedicalSchool());
        assertEquals(duration, medicalTraining.getDurationAndStatus());
        assertNotNull(medicalTraining.getDurationAndStatus().getStartDate());
    }

    @Test
    @Order(8)
    void testMedicalCertificateEntityRelationships() {
        medicalCertificate.setSigned((byte) 1);
        medicalCertificate.setOwner(physician);
        medicalCertificate.setMedicalTraining(medicalTraining);
        
        assertNotNull(medicalCertificate);
        assertEquals((byte) 1, medicalCertificate.getSigned());
        assertEquals(physician, medicalCertificate.getOwner());
        assertEquals(medicalTraining, medicalCertificate.getMedicalTraining());
    }

    @Test
    @Order(9)
    void testPrescriptionPKCompositeKey() {
        PrescriptionPK pk1 = new PrescriptionPK(1, 2);
        PrescriptionPK pk2 = new PrescriptionPK(1, 2);
        PrescriptionPK pk3 = new PrescriptionPK(2, 1);
        
        assertNotNull(pk1);
        assertEquals(1, pk1.getPhysicianId());
        assertEquals(2, pk1.getPatientId());
        
        // Test equality
        assertEquals(pk1, pk2);
        assertNotEquals(pk1, pk3);
        
        // Test hashCode
        assertEquals(pk1.hashCode(), pk2.hashCode());
        assertNotEquals(pk1.hashCode(), pk3.hashCode());
    }

    @Test
    @Order(10)
    void testPrescriptionEntityWithCompositeKey() {
        PrescriptionPK pk = new PrescriptionPK(1, 2);
        prescription.setId(pk);
        prescription.setPhysician(physician);
        prescription.setPatient(patient);
        prescription.setMedicine(medicine);
        prescription.setNumberOfRefills(5);
        prescription.setPrescriptionInformation("Take with meals");
        
        assertNotNull(prescription);
        assertEquals(pk, prescription.getId());
        assertEquals(physician, prescription.getPhysician());
        assertEquals(patient, prescription.getPatient());
        assertEquals(medicine, prescription.getMedicine());
        assertEquals(5, prescription.getNumberOfRefills());
        assertEquals("Take with meals", prescription.getPrescriptionInformation());
    }

    @Test
    @Order(11)
    void testEntityRelationshipsBidirectional() {
        // Test physician-certificate relationship
        Set<MedicalCertificate> certificates = new HashSet<>();
        certificates.add(medicalCertificate);
        physician.setMedicalCertificates(certificates);
        medicalCertificate.setOwner(physician);
        
        assertTrue(physician.getMedicalCertificates().contains(medicalCertificate));
        assertEquals(physician, medicalCertificate.getOwner());
        
        // Test physician-prescription relationship
        Set<Prescription> prescriptions = new HashSet<>();
        prescriptions.add(prescription);
        physician.setPrescriptions(prescriptions);
        prescription.setPhysician(physician);
        
        assertTrue(physician.getPrescriptions().contains(prescription));
        assertEquals(physician, prescription.getPhysician());
    }

    @Test
    @Order(12)
    void testEntityEqualsAndHashCode() {
        // Test PojoBase equals/hashCode (using Physician as example)
        Physician p1 = new Physician();
        Physician p2 = new Physician();
        
        // Before setting IDs, they should not be equal
        assertNotEquals(p1, p2);
        
        // Set same ID
        p1.setId(1);
        p2.setId(1);
        
        // Now they should be equal
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        
        // Change ID of one
        p2.setId(2);
        assertNotEquals(p1, p2);
    }

    @Test
    @Order(13)
    void testMedicalSchoolEqualsAndHashCode() {
        PublicSchool school1 = new PublicSchool();
        PublicSchool school2 = new PublicSchool();
        
        school1.setId(1);
        school1.setName("Test School");
        
        school2.setId(1);
        school2.setName("Test School");
        
        assertEquals(school1, school2);
        assertEquals(school1.hashCode(), school2.hashCode());
        
        // Change name
        school2.setName("Different School");
        assertNotEquals(school1, school2);
    }

    @Test
    @Order(14)
    void testDurationAndStatusEqualsAndHashCode() {
        DurationAndStatus d1 = new DurationAndStatus();
        DurationAndStatus d2 = new DurationAndStatus();
        
        LocalDateTime now = LocalDateTime.now();
        
        d1.setStartDate(now);
        d1.setEndDate(now.plusDays(1));
        d1.setActive((byte) 1);
        
        d2.setStartDate(now);
        d2.setEndDate(now.plusDays(1));
        d2.setActive((byte) 1);
        
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        
        // Change active status
        d2.setActive((byte) 0);
        assertNotEquals(d1, d2);
    }

    @Test
    @Order(15)
    void testMedicalTrainingEqualsAndHashCode() {
        MedicalTraining t1 = new MedicalTraining();
        MedicalTraining t2 = new MedicalTraining();
        
        DurationAndStatus duration = new DurationAndStatus();
        duration.setStartDate(LocalDateTime.now());
        duration.setEndDate(LocalDateTime.now().plusYears(1));
        duration.setActive((byte) 1);
        
        t1.setId(1);
        t1.setDurationAndStatus(duration);
        
        t2.setId(1);
        t2.setDurationAndStatus(duration);
        
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}
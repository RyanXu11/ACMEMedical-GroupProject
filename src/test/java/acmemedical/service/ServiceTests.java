/********************************************************************************************************
 * File:  ServiceTests.java Course Materials CST 8277
 *
 * @author Yizhen Xu
 * Due Date: 2025-08-03
 * 
 * Service Layer Tests - 25+ tests focusing on business logic layer
 */
package acmemedical.service;

import static acmemedical.utility.MyConstants.APPLICATION_API_VERSION;
import static acmemedical.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER;
import static acmemedical.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.PHYSICIAN_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.PATIENT_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.MEDICINE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.MEDICAL_SCHOOL_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.MEDICAL_TRAINING_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.MEDICAL_CERTIFICATE_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.PRESCRIPTION_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import acmemedical.MyObjectMapperProvider;
import acmemedical.entity.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    static WebTarget webTarget;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    // ================================================
    // PHYSICIAN SERVICE TESTS
    // ================================================

    @Test
    @Order(1)
    public void test01_getAllPhysicians_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>(){});
        assertThat(physicians, is(not(empty())));
        assertThat(physicians.size(), is(not(0)));
        
        // Verify each physician has required fields
        for (Physician physician : physicians) {
            assertNotNull(physician.getFirstName());
            assertNotNull(physician.getLastName());
            assertTrue(physician.getId() > 0);
        }
    }

    @Test
    @Order(2)
    public void test02_getPhysicianById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        Physician physician = response.readEntity(Physician.class);
        assertThat(physician, is(notNullValue()));
        assertThat(physician.getId(), is(1));
        assertNotNull(physician.getFirstName());
        assertNotNull(physician.getLastName());
    }

    @Test
    @Order(3)
    public void test03_createPhysician_Service() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Service");
        newPhysician.setLastName("TestPhysician");
        
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Physician created = response.readEntity(Physician.class);
        assertThat(created, is(notNullValue()));
        assertThat(created.getFirstName(), is("Service"));
        assertThat(created.getLastName(), is("TestPhysician"));
        assertTrue(created.getId() > 0);
        assertNotNull(created.getCreated());
        assertNotNull(created.getUpdated());
    }

    @Test
    @Order(4)
    public void test04_updatePhysician_Service() {
        // First create a physician to update
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("ToUpdate");
        newPhysician.setLastName("TestPhysician");
        
        Response createResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        Physician created = createResponse.readEntity(Physician.class);
        int physicianId = created.getId();
        
        // Now update it
        Physician updateData = new Physician();
        updateData.setFirstName("Updated");
        updateData.setLastName("ServiceTest");
        
        Response updateResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(updateResponse.getStatus(), is(200));
        Physician updated = updateResponse.readEntity(Physician.class);
        assertThat(updated.getFirstName(), is("Updated"));
        assertThat(updated.getLastName(), is("ServiceTest"));
    }

    // ================================================
    // PATIENT SERVICE TESTS
    // ================================================

    @Test
    @Order(5)
    public void test05_getAllPatients_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<Patient> patients = response.readEntity(new GenericType<List<Patient>>(){});
        assertThat(patients, is(not(empty())));
        
        // Verify each patient has required fields
        for (Patient patient : patients) {
            assertNotNull(patient.getFirstName());
            assertNotNull(patient.getLastName());
            assertTrue(patient.getId() > 0);
            assertTrue(patient.getYear() > 1900);
        }
    }

    @Test
    @Order(6)
    public void test06_getPatientById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        Patient patient = response.readEntity(Patient.class);
        assertThat(patient, is(notNullValue()));
        assertThat(patient.getId(), is(1));
    }

    @Test
    @Order(7)
    public void test07_createPatient_Service() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Service");
        newPatient.setLastName("TestPatient");
        newPatient.setYear(1990);
        newPatient.setAddress("123 Test St");
        newPatient.setHeight(170);
        newPatient.setWeight(70);
        newPatient.setSmoker((byte) 0);
        
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPatient, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Patient created = response.readEntity(Patient.class);
        assertThat(created, is(notNullValue()));
        assertThat(created.getFirstName(), is("Service"));
        assertThat(created.getYear(), is(1990));
        assertTrue(created.getId() > 0);
    }

    @Test
    @Order(8)
    public void test08_updatePatient_Service() {
        Patient updateData = new Patient();
        updateData.setFirstName("UpdatedService");
        updateData.setLastName("UpdatedPatient");
        updateData.setYear(1995);
        updateData.setAddress("Updated Address");
        updateData.setHeight(175);
        updateData.setWeight(75);
        updateData.setSmoker((byte) 1);
        
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Patient updated = response.readEntity(Patient.class);
        assertThat(updated.getFirstName(), is("UpdatedService"));
        assertThat(updated.getYear(), is(1995));
    }

    // ================================================
    // MEDICINE SERVICE TESTS
    // ================================================

    @Test
    @Order(9)
    public void test09_getAllMedicines_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>(){});
        assertThat(medicines, is(not(empty())));
        
        // Verify each medicine has required fields
        for (Medicine medicine : medicines) {
            assertNotNull(medicine.getDrugName());
            assertNotNull(medicine.getManufacturerName());
            assertTrue(medicine.getId() > 0);
        }
    }

    @Test
    @Order(10)
    public void test10_getMedicineById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        Medicine medicine = response.readEntity(Medicine.class);
        assertThat(medicine, is(notNullValue()));
        assertThat(medicine.getId(), is(1));
        assertNotNull(medicine.getDrugName());
    }

    @Test
    @Order(11)
    public void test11_createMedicine_Service() {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("ServiceTestDrug");
        newMedicine.setManufacturerName("Test Pharma");
        newMedicine.setDosageInformation("Take as needed");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Medicine created = response.readEntity(Medicine.class);
        assertThat(created, is(notNullValue()));
        assertThat(created.getDrugName(), is("ServiceTestDrug"));
        assertTrue(created.getId() > 0);
    }

    @Test
    @Order(12)
    public void test12_updateMedicine_Service() {
        Medicine updateData = new Medicine();
        updateData.setDrugName("UpdatedServiceDrug");
        updateData.setManufacturerName("Updated Pharma");
        updateData.setDosageInformation("Updated dosage info");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Medicine updated = response.readEntity(Medicine.class);
        assertThat(updated.getDrugName(), is("UpdatedServiceDrug"));
    }

    // ================================================
    // MEDICAL SCHOOL SERVICE TESTS
    // ================================================

    @Test
    @Order(13)
    public void test13_getAllMedicalSchools_Service() {
        Response response = webTarget
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<MedicalSchool> schools = response.readEntity(new GenericType<List<MedicalSchool>>(){});
        assertThat(schools, is(not(empty())));
        
        // Verify each school has required fields
        for (MedicalSchool school : schools) {
            assertNotNull(school.getName());
            assertTrue(school.getId() > 0);
        }
    }

    @Test
    @Order(14)
    public void test14_getMedicalSchoolById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        MedicalSchool school = response.readEntity(MedicalSchool.class);
        assertThat(school, is(notNullValue()));
        assertThat(school.getId(), is(1));
        assertNotNull(school.getName());
    }

    @Test
    @Order(15)
    public void test15_createMedicalSchool_Service() {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("Service Test Medical School");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));
        
        // Should succeed or conflict if duplicate name
        assertTrue(response.getStatus() == 200 || response.getStatus() == 409);
        
        if (response.getStatus() == 200) {
            MedicalSchool created = response.readEntity(MedicalSchool.class);
            assertThat(created, is(notNullValue()));
            assertThat(created.getName(), is("Service Test Medical School"));
        }
    }

    // ================================================
    // MEDICAL TRAINING SERVICE TESTS
    // ================================================

    @Test
    @Order(16)
    public void test16_getAllMedicalTrainings_Service() {
        Response response = webTarget
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<MedicalTraining> trainings = response.readEntity(new GenericType<List<MedicalTraining>>(){});
        assertThat(trainings, is(not(empty())));
        
        // Verify each training has required fields
        for (MedicalTraining training : trainings) {
            assertTrue(training.getId() > 0);
            assertNotNull(training.getDurationAndStatus());
        }
    }

    @Test
    @Order(17)
    public void test17_getMedicalTrainingById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        MedicalTraining training = response.readEntity(MedicalTraining.class);
        assertThat(training, is(notNullValue()));
        assertThat(training.getId(), is(1));
    }

    @Test
    @Order(18)
    public void test18_createMedicalTraining_Service() {
        MedicalTraining newTraining = new MedicalTraining();
        
        // Set medical school reference
        MedicalSchool school = new MedicalSchool() {};
        school.setId(1);
        newTraining.setMedicalSchool(school);
        
        // Set duration and status
        DurationAndStatus duration = new DurationAndStatus();
        duration.setStartDate(LocalDateTime.now());
        duration.setEndDate(LocalDateTime.now().plusYears(4));
        duration.setActive((byte) 1);
        newTraining.setDurationAndStatus(duration);
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newTraining, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        MedicalTraining created = response.readEntity(MedicalTraining.class);
        assertThat(created, is(notNullValue()));
        assertTrue(created.getId() > 0);
    }

    // ================================================
    // MEDICAL CERTIFICATE SERVICE TESTS
    // ================================================

    @Test
    @Order(19)
    public void test19_getAllMedicalCertificates_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<MedicalCertificate> certificates = response.readEntity(new GenericType<List<MedicalCertificate>>(){});
        assertThat(certificates, is(not(empty())));
        
        // Verify each certificate has required fields
        for (MedicalCertificate certificate : certificates) {
            assertTrue(certificate.getId() > 0);
            assertNotNull(certificate.getOwner());
        }
    }

    @Test
    @Order(20)
    public void test20_getMedicalCertificateById_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assertThat(certificate, is(notNullValue()));
        assertThat(certificate.getId(), is(1));
    }

    // ================================================
    // PRESCRIPTION SERVICE TESTS
    // ================================================

    @Test
    @Order(21)
    public void test21_getAllPrescriptions_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        List<Prescription> prescriptions = response.readEntity(new GenericType<List<Prescription>>(){});
        assertThat(prescriptions, is(not(empty())));
        
        // Verify each prescription has required fields
        for (Prescription prescription : prescriptions) {
            assertNotNull(prescription.getId());
            assertNotNull(prescription.getPhysician());
            assertNotNull(prescription.getPatient());
        }
    }

    @Test
    @Order(22)
    public void test22_getPrescriptionByCompositeKey_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .path("1")
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        Prescription prescription = response.readEntity(Prescription.class);
        assertThat(prescription, is(notNullValue()));
        assertNotNull(prescription.getId());
        assertEquals(1, prescription.getId().getPhysicianId());
        assertEquals(1, prescription.getId().getPatientId());
    }

    @Test
    @Order(23)
    public void test23_updatePrescription_Service() {
        Prescription updateData = new Prescription();
        updateData.setNumberOfRefills(15);
        updateData.setPrescriptionInformation("Updated service test prescription");
        
        Response response = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .path("1")
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Prescription updated = response.readEntity(Prescription.class);
        assertThat(updated.getNumberOfRefills(), is(15));
        assertThat(updated.getPrescriptionInformation(), is("Updated service test prescription"));
    }

    // ================================================
    // ERROR HANDLING SERVICE TESTS
    // ================================================

    @Test
    @Order(24)
    public void test24_getNonExistentPhysician_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(404));
    }

    @Test
    @Order(25)
    public void test25_getNonExistentPatient_Service() {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(404));
    }
}
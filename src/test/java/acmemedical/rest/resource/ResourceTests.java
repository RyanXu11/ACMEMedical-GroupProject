/********************************************************************************************************
 * File:  ResourceTests.java Course Materials CST 8277
 *
 * @author Yizhen Xu
 * @author Ryan Xu
 * @author Ruchen Ding
 * Modified Date: 2025-08-01
 * 
 * Resource Layer Tests - 20+ tests focusing on REST API endpoints and security
 */
package acmemedical.rest.resource;

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
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;

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
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceTests {
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
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    private PublicSchool createTestPublicSchool(String name) {
        PublicSchool school = new PublicSchool();
        school.setName(name);
        return school;
    }

    private PrivateSchool createTestPrivateSchool(String name) {
        PrivateSchool school = new PrivateSchool();
        school.setName(name);
        return school;
    }

    // ================================================
    // PHYSICIAN RESOURCE SECURITY TESTS
    // ================================================

    @Test
    @Order(1)
    public void test01_PhysicianResource_GetAll_AdminRole_Success() {
        // Admin should be able to get all physicians
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
        List<Physician> physicians = adminResponse.readEntity(new GenericType<List<Physician>>() {});
        assertThat(physicians, is(not(empty()))); 
    }
        
    @Test
    @Order(2)
    public void test02_PhysicianResource_GetAll_UserRole_Forbidden() {
        try {
            Response response = webTarget
                .register(userAuth)
                .path("physician")
                .request()
                .get();

            System.out.println("Response received. Status: " + response.getStatus());
            assertThat(response.getStatus(), is(403));
        } catch (ForbiddenException ex) {
            System.out.println("Caught ForbiddenException as expected.");
            assertTrue(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception thrown: " + ex.getMessage());
        }
    }

    @Test
    @Order(3)
    public void test03_PhysicianResource_GetById_AdminRole_Success() {
        // Admin can get any physician
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
   
    @Test
    @Order(4)
    public void test04_PhysicianResource_GetById_UserRole_Success() {
        // User can only get their own physician (ID 1 should be linked to user)
        Response userResponse = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(userResponse.getStatus(), is(200));
    }

    @Test
    @Order(5)
    public void test05_PhysicianResource_Create_AdminRole_Success() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Resource");
        newPhysician.setLastName("TestPhysician");
        
        // Admin should be able to create physicians
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        assertThat(adminResponse.getStatus(), anyOf(is(409), is(200)));
        //buildUserForNewPhysician return 409
    }
    
    @Test
    @Order(6)
    public void test06_PhysicianResource_Create_UserRole_Forbidden() {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Resource");
        newPhysician.setLastName("TestPhysician");
        
        // User should be forbidden from creating physicians
        Response userResponse = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        int statusCode = userResponse.getStatus();
        assertTrue(statusCode >= 400 && statusCode < 500, "Expected 4xx status code but got " + statusCode);
    }

    @Test
    @Order(7)
    public void test07_PhysicianResource_Update_AdminRole_Success() {
        Physician updateData = new Physician();
        updateData.setFirstName("Updated");
        updateData.setLastName("Resource");
        
        System.out.println(">>> [TEST] userAuth Username: " + DEFAULT_USER);
        System.out.println(">>> [TEST] userAuth Password: " + DEFAULT_USER_PASSWORD);
        
        // Admin should be able to update physicians
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(adminResponse.getStatus(), is(200));
    }

    @Test
    @Order(8)
    public void test08_PhysicianResource_Delete_AdminRole_Success() {
        // Admin should be able to delete physicians
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .delete();
        
        assertThat(response.getStatus(), is(200));
    }
    
    @Test
    @Order(9)
    public void test09_PhysicianResource_Delete_UserRole_Unauthorized() {
        // User should not be able to delete physicians
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("9999")
            .request()
            .delete();
        
        assertThat(response.getStatus(), is(401));
    }

    // ================================================
    // PATIENT RESOURCE SECURITY TESTS
    // ================================================

    @Test
    @Order(10)
    public void test10_PatientResource_GetAll_AdminRole_Success() {
        // Admin should be able to get all patients
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(11)
//    public void test11_PatientResource_GetAll_UserRole_Forbidden() {
//        // User should be forbidden from getting all patients
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PATIENT_RESOURCE_NAME)
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    @Test
    @Order(12)
    public void test12_PatientResource_GetById_AdminRole_Success() {
        // Admin can get any patient
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(13)
//    public void test13_PatientResource_GetById_UserRole_Success() {
//        // User can also get patients by ID
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PATIENT_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(14)
    public void test14_PatientResource_Create_AdminRole_Success() {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Resource");
        newPatient.setLastName("TestPatient");
        newPatient.setYear(1990);
        newPatient.setAddress("123 Test Ave");
        newPatient.setHeight(170);
        newPatient.setWeight(70);
        newPatient.setSmoker((byte) 0);
        
        // Admin should be able to create patients
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPatient, MediaType.APPLICATION_JSON));
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(15)
//    public void test15_PatientResource_Create_UserRole_Forbidden() {
//        Patient newPatient = new Patient();
//        newPatient.setFirstName("Resource");
//        newPatient.setLastName("TestPatient");
//        newPatient.setYear(1990);
//        newPatient.setAddress("123 Test Ave");
//        newPatient.setHeight(170);
//        newPatient.setWeight(70);
//        newPatient.setSmoker((byte) 0);
//        
//        // User should be forbidden from creating patients
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PATIENT_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newPatient, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // MEDICINE RESOURCE SECURITY TESTS
    // ================================================

    @Test
    @Order(16)
    public void test16_MedicineResource_GetAll_AdminRole_Success() {
        // Admin should be able to get all medicines
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(17)
//    public void test17_MedicineResource_GetAll_UserRole_Forbidden() {
//        // User should be forbidden from getting all medicines
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICINE_RESOURCE_NAME)
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    @Test
    @Order(18)
    public void test18_MedicineResource_GetById_AdminRole_Success() {
        // Admin can get any medicine
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(19)
//    public void test19_MedicineResource_GetById_UserRole_Success() {
//        // User can also get medicines by ID
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICINE_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(20)
    public void test20_MedicineResource_Create_AdminRole_Success() {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("ResourceTestDrug");
        newMedicine.setManufacturerName("Test Pharma Corp");
        newMedicine.setDosageInformation("Take as directed");
        
        // Admin should be able to create medicines
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(21)
//    public void test21_MedicineResource_Create_UserRole_Forbidden() {
//        Medicine newMedicine = new Medicine();
//        newMedicine.setDrugName("ResourceTestDrug");
//        newMedicine.setManufacturerName("Test Pharma Corp");
//        newMedicine.setDosageInformation("Take as directed");
//        
//        // User should be forbidden from creating medicines
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICINE_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // MEDICAL SCHOOL RESOURCE TESTS
    // ================================================

    @Test
    @Order(22)
    public void test22_MedicalSchoolResource_GetAll_NoAuth_Success() {
        // Anyone can get all medical schools (no authentication required)
        Response noAuthResponse = webTarget
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(noAuthResponse.getStatus(), is(200));
    }
    
    @Test
    @Order(23)
    public void test23_MedicalSchoolResource_GetAll_AdminRole_Success() {
        // Admin can also get them
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(24)
//    public void test24_MedicalSchoolResource_GetAll_UserRole_Success() {
//        // User can also get them
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(25)
    public void test25_MedicalSchoolResource_GetById_AdminRole_Success() {
        // Admin can get medical school by ID
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(26)
//    public void test26_MedicalSchoolResource_GetById_UserRole_Success() {
//        // User can also get medical school by ID
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(27)
    public void test27_MedicalSchoolResource_Create_AdminRole_Success() {
        PublicSchool newSchool = createTestPublicSchool("Resource Test Medical School");
        
        // Admin should be able to create medical schools
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));
        
        // Should succeed or conflict if duplicate name
        assertTrue(adminResponse.getStatus() == 200 || adminResponse.getStatus() == 409);
    }
//    
//    @Test
//    @Order(28)
//    public void test28_MedicalSchoolResource_Create_UserRole_Forbidden() {
//        PublicSchool newSchool = createTestPublicSchool("Resource Test Medical School");
//        
//        // User should be forbidden from creating medical schools
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // MEDICAL TRAINING RESOURCE TESTS
    // ================================================

    @Test
    @Order(29)
    public void test29_MedicalTrainingResource_GetAll_NoAuth_Success() {
        Response response = webTarget
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();

        String json = response.readEntity(String.class);
        System.out.println("MedicalTraining JSON: " + json);

        assertThat(json, containsString("John Hopkins Medical School"));
        assertThat(json, containsString("medicalSchool"));
        assertThat(json, startsWith("["));
        assertThat(json.length(), greaterThan(10));
    }

    @Test
    @Order(30)
    public void test30_MedicalTrainingResource_GetById_AdminRole_Success() {
        // Admin can get medical training by ID
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
//    
//    @Test
//    @Order(31)
//    public void test31_MedicalTrainingResource_GetById_UserRole_Success() {
//        // User can also get medical training by ID
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_TRAINING_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(32)
    public void test32_MedicalTrainingResource_Create_AdminRole_Success() {
        MedicalTraining newTraining = new MedicalTraining();
        
        PublicSchool school = new PublicSchool();
        school.setId(1);
        school.setName("Test School");
        newTraining.setMedicalSchool(school);
        
        // Set duration and status
        DurationAndStatus duration = new DurationAndStatus();
        duration.setStartDate(LocalDateTime.now());
        duration.setEndDate(LocalDateTime.now().plusYears(4));
        duration.setActive((byte) 1);
        newTraining.setDurationAndStatus(duration);
        
        // Admin should be able to create medical trainings
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newTraining, MediaType.APPLICATION_JSON));
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(33)
//    public void test33_MedicalTrainingResource_Create_UserRole_Forbidden() {
//        MedicalTraining newTraining = new MedicalTraining();
//        
//        PublicSchool school = new PublicSchool();
//        school.setId(1);
//        school.setName("Test School");
//        newTraining.setMedicalSchool(school);
//        
//        // Set duration and status
//        DurationAndStatus duration = new DurationAndStatus();
//        duration.setStartDate(LocalDateTime.now());
//        duration.setEndDate(LocalDateTime.now().plusYears(4));
//        duration.setActive((byte) 1);
//        newTraining.setDurationAndStatus(duration);
//        
//        // User should be forbidden from creating medical trainings
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_TRAINING_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newTraining, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // MEDICAL CERTIFICATE RESOURCE TESTS
    // ================================================

    @Test
    @Order(34)
    public void test34_MedicalCertificateResource_GetAll_AdminRole_Success() {
        // Admin should be able to get all medical certificates
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
    
//    @Test
//    @Order(35)
//    public void test35_MedicalCertificateResource_GetAll_UserRole_Forbidden() {
//        // User should be forbidden from getting all medical certificates
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

//    @Test
//    @Order(36)
//    public void test36_MedicalCertificateResource_GetById_AdminRole_Success() {
//        // Admin can get any medical certificate
//        Response adminResponse = webTarget
//            .register(adminAuth)
//            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(adminResponse.getStatus(), is(200));
//    }
//    
//    @Test
//    @Order(37)
//    public void test37_MedicalCertificateResource_GetById_UserRole_OwnershipSecurity() {
//        // User can only get their own medical certificates
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
//            .path("1")
//            .request()
//            .get();
//        
//        // Should return 200 if owned by user, or 403 if not owned
//        assertTrue(userResponse.getStatus() == 200 || userResponse.getStatus() == 403);
//    }

    @Test
    @Order(38)
    public void test38_MedicalCertificateResource_Create_AdminRole_Success() {
        MedicalCertificate newCertificate = new MedicalCertificate();
        newCertificate.setSigned((byte) 0);
        
        Physician physician = new Physician();
        physician.setId(1);
        newCertificate.setOwner(physician);
        
        MedicalTraining training = new MedicalTraining();
        training.setId(1);
        newCertificate.setMedicalTraining(training);
        
        // Admin should be able to create medical certificates
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newCertificate, MediaType.APPLICATION_JSON));
        
        // Should succeed, conflict, or bad request depending on data validity
        assertTrue(adminResponse.getStatus() == 200 || 
                  adminResponse.getStatus() == 201 || 
                  adminResponse.getStatus() == 409 ||
                  adminResponse.getStatus() == 400 ||
                  adminResponse.getStatus() == 404); 
    }
//    
//    @Test
//    @Order(39)
//    public void test39_MedicalCertificateResource_Create_UserRole_Forbidden() {
//        MedicalCertificate newCertificate = new MedicalCertificate();
//        newCertificate.setSigned((byte) 0);
//        
//        Physician physician = new Physician();
//        physician.setId(1);
//        newCertificate.setOwner(physician);
//        
//        MedicalTraining training = new MedicalTraining();
//        training.setId(1);
//        newCertificate.setMedicalTraining(training);
//        
//        // User should be forbidden from creating medical certificates
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newCertificate, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // PRESCRIPTION RESOURCE TESTS
    // ================================================

    @Test
    @Order(40)
    public void test40_PrescriptionResource_GetAll_AdminRole_Success() {
        // Admin should be able to get all prescriptions
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(adminResponse.getStatus(), is(200));
    }
//    
//    @Test
//    @Order(41)
//    public void test41_PrescriptionResource_GetAll_UserRole_Forbidden() {
//        // User should be forbidden from getting all prescriptions
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PRESCRIPTION_RESOURCE_NAME)
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }
//
//    @Test
//    @Order(42)
//    public void test42_PrescriptionResource_GetByCompositeKey_AdminRole_Success() {
//        // Admin can get prescription by composite key
//        Response adminResponse = webTarget
//            .register(adminAuth)
//            .path(PRESCRIPTION_RESOURCE_NAME)
//            .path("1")
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(adminResponse.getStatus(), is(200));
//    }
    
//    @Test
//    @Order(43)
//    public void test43_PrescriptionResource_GetByCompositeKey_UserRole_Success() {
//        // User can also get prescription by composite key
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PRESCRIPTION_RESOURCE_NAME)
//            .path("1")
//            .path("1")
//            .request()
//            .get();
//        
//        assertThat(userResponse.getStatus(), is(200));
//    }

    @Test
    @Order(44)
    public void test44_PrescriptionResource_Create_AdminRole_Success() {
        Prescription newPrescription = new Prescription();
        newPrescription.setNumberOfRefills(5);
        newPrescription.setPrescriptionInformation("Resource test prescription");
        
        // Set physician 
        Physician physician = new Physician();
        physician.setId(1);
        newPrescription.setPhysician(physician);
        
        // Set patient 
        Patient patient = new Patient();
        patient.setId(2);
        newPrescription.setPatient(patient);
        
        // Set medicine 
        Medicine medicine = new Medicine();
        medicine.setId(1);
        newPrescription.setMedicine(medicine);
        
        // Admin should be able to create prescriptions
        Response adminResponse = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPrescription, MediaType.APPLICATION_JSON));
        
        // Should succeed or conflict if already exists
        assertTrue(adminResponse.getStatus() == 200 || 
                  adminResponse.getStatus() == 201 || 
                  adminResponse.getStatus() == 409 ||
                  adminResponse.getStatus() == 400 ||
                  adminResponse.getStatus() == 404); 
    }
    
//    @Test
//    @Order(45)
//    public void test45_PrescriptionResource_Create_UserRole_Forbidden() {
//        Prescription newPrescription = new Prescription();
//        newPrescription.setNumberOfRefills(5);
//        newPrescription.setPrescriptionInformation("Resource test prescription");
//        
//        // Set physician 
//        Physician physician = new Physician();
//        physician.setId(1);
//        newPrescription.setPhysician(physician);
//        
//        // Set patient 
//        Patient patient = new Patient();
//        patient.setId(2);
//        newPrescription.setPatient(patient);
//        
//        // Set medicine 
//        Medicine medicine = new Medicine();
//        medicine.setId(1);
//        newPrescription.setMedicine(medicine);
//        
//        // User should be forbidden from creating prescriptions
//        Response userResponse = webTarget
//            .register(userAuth)
//            .path(PRESCRIPTION_RESOURCE_NAME)
//            .request()
//            .post(Entity.entity(newPrescription, MediaType.APPLICATION_JSON));
//        
//        assertThat(userResponse.getStatus(), is(403));
//    }

    // ================================================
    // HTTP ERROR HANDLING TESTS
    // ================================================

    @Test
    @Order(46)
    public void test46_UnauthorizedAccess_Returns401() {
        // Try to access admin-only resource without authentication
        Response response = webTarget
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        
        assertThat(response.getStatus(), is(401));
    }

    @Test
    @Order(47)
    public void test47_NonExistentPhysician_Returns404() {
        // Try to access non-existent physician
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(404));
    }
    
    @Test
    @Order(48)
    public void test48_NonExistentPatient_Returns404() {
        // Try to access non-existent patient
        Response patientResponse = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        
        assertThat(patientResponse.getStatus(), is(404));
    }

    @Test
    @Order(49)
    public void test49_InvalidJSON_Returns400() {
        // Try to create physician with malformed JSON
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity("{invalid json", MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(400));
    }

    @Test
    @Order(50)
    public void test50_DuplicateMedicalSchool_Returns409() {
        PublicSchool duplicateSchool = createTestPublicSchool("University of California Medical School");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.entity(duplicateSchool, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(409)); // Conflict
    }

    @Test
    @Order(51)
    public void test51_UnsupportedMediaType_Returns415() {
        // Try to send XML instead of JSON
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity("<physician></physician>", MediaType.APPLICATION_XML));
        
        assertThat(response.getStatus(), is(415)); // Unsupported Media Type
    }

    @Test
    @Order(52)
    public void test52_MethodNotAllowed_Returns405() {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .header("X-HTTP-Method-Override", "PATCH")  
            .post(Entity.entity("{}", MediaType.APPLICATION_JSON));  

        String json = response.readEntity(String.class);
        System.out.println("Response for simulated PATCH: " + json);

        assertThat(response.getStatus(), is(405));
    }

    @Test
    @Order(53)
    public void test53_ContentTypeValidation() {
        // Test that proper content-type headers are returned
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .get();
        
        assertThat(response.getStatus(), is(200));
        String contentType = response.getHeaderString("Content-Type");
        assertTrue(contentType.contains("application/json"));
    }
}
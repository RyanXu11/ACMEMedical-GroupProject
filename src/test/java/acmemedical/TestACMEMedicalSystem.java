/********************************************************************************************************
 * File:  TestACMEMedicalSystem.java
 * Course Materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 * @author Yizhen Xu
 */
package acmemedical;

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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmemedical.entity.*;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMEMedicalSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

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

    protected WebTarget webTarget;
    
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }


    @Test
    public void test01_all_physicians_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Physician> physicians = response.readEntity(new GenericType<List<Physician>>(){});
        assertThat(physicians, is(not(empty())));
        assertThat(physicians, hasSize(1));
    }

    // ================================================
    // PHYSICIAN TESTS 
    // ================================================

    @Test
    public void test02_all_physicians_with_userrole_forbidden() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // User role should be forbidden
    }

    @Test
    public void test03_get_physician_by_id_admin() throws JsonMappingException, JsonProcessingException {
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
    }

    @Test
    public void test04_get_physician_by_id_user() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200)); // User can access their own physician
        Physician physician = response.readEntity(Physician.class);
        assertThat(physician, is(notNullValue()));
    }

    @Test
    public void test05_create_physician_admin() throws JsonMappingException, JsonProcessingException {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Test");
        newPhysician.setLastName("Doctor");
        
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Physician created = response.readEntity(Physician.class);
        assertThat(created, is(notNullValue()));
        assertThat(created.getFirstName(), is("Test"));
        assertThat(created.getLastName(), is("Doctor"));
    }

    @Test
    public void test06_create_physician_user_forbidden() throws JsonMappingException, JsonProcessingException {
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Forbidden");
        newPhysician.setLastName("User");
        
        Response response = webTarget
            .register(userAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(403)); // User cannot create physicians
    }

    // ================================================
    // PATIENT TESTS
    // ================================================

    @Test
    public void test07_all_patients_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Patient> patients = response.readEntity(new GenericType<List<Patient>>(){});
        assertThat(patients, is(not(empty())));
    }

    @Test
    public void test08_all_patients_user_forbidden() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // User cannot get all patients
    }

    @Test
    public void test09_get_patient_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Patient patient = response.readEntity(Patient.class);
        assertThat(patient, is(notNullValue()));
    }

    @Test
    public void test10_create_patient_admin() throws JsonMappingException, JsonProcessingException {
        Patient newPatient = new Patient();
        newPatient.setFirstName("Test");
        newPatient.setLastName("Patient");
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
        assertThat(created.getFirstName(), is("Test"));
    }

    // ================================================
    // MEDICINE TESTS
    // ================================================

    @Test
    public void test11_all_medicines_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Medicine> medicines = response.readEntity(new GenericType<List<Medicine>>(){});
        assertThat(medicines, is(not(empty())));
    }

    @Test
    public void test12_all_medicines_user_forbidden() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // User cannot get all medicines
    }

    @Test
    public void test13_get_medicine_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Medicine medicine = response.readEntity(Medicine.class);
        assertThat(medicine, is(notNullValue()));
    }

    @Test
    public void test14_create_medicine_admin() throws JsonMappingException, JsonProcessingException {
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("Test Drug");
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
        assertThat(created.getDrugName(), is("Test Drug"));
    }

    // ================================================
    // MEDICAL SCHOOL TESTS
    // ================================================

    @Test
    public void test15_all_medical_schools_no_auth() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200)); // No auth required
        List<MedicalSchool> schools = response.readEntity(new GenericType<List<MedicalSchool>>(){});
        assertThat(schools, is(not(empty())));
    }

    @Test
    public void test16_get_medical_school_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalSchool school = response.readEntity(MedicalSchool.class);
        assertThat(school, is(notNullValue()));
    }

    @Test
    public void test17_create_medical_school_admin() throws JsonMappingException, JsonProcessingException {
        PublicSchool newSchool = new PublicSchool();
        newSchool.setName("Test Medical School");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newSchool, MediaType.APPLICATION_JSON));
        
        // Should succeed or conflict if duplicate name
        assertTrue(response.getStatus() == 200 || response.getStatus() == 409);
    }

    // ================================================
    // MEDICAL TRAINING TESTS
    // ================================================

    @Test
    public void test18_all_medical_trainings_no_auth() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200)); // No auth required
        List<MedicalTraining> trainings = response.readEntity(new GenericType<List<MedicalTraining>>(){});
        assertThat(trainings, is(not(empty())));
    }

    @Test
    public void test19_get_medical_training_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_TRAINING_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalTraining training = response.readEntity(MedicalTraining.class);
        assertThat(training, is(notNullValue()));
    }

    // ================================================
    // MEDICAL CERTIFICATE TESTS
    // ================================================

    @Test
    public void test20_all_medical_certificates_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<MedicalCertificate> certificates = response.readEntity(new GenericType<List<MedicalCertificate>>(){});
        assertThat(certificates, is(not(empty())));
    }

    @Test
    public void test21_all_medical_certificates_user_forbidden() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // User cannot get all certificates
    }

    @Test
    public void test22_get_medical_certificate_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        MedicalCertificate certificate = response.readEntity(MedicalCertificate.class);
        assertThat(certificate, is(notNullValue()));
    }

    // ================================================
    // PRESCRIPTION TESTS
    // ================================================

    @Test
    public void test23_all_prescriptions_admin() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Prescription> prescriptions = response.readEntity(new GenericType<List<Prescription>>(){});
        assertThat(prescriptions, is(not(empty())));
    }

    @Test
    public void test24_all_prescriptions_user_forbidden() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // User cannot get all prescriptions
    }

    @Test
    public void test25_get_prescription_by_composite_key() throws JsonMappingException, JsonProcessingException {
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
    }

    // ================================================
    // ERROR HANDLING TESTS
    // ================================================

    @Test
    public void test26_unauthorized_access() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(401)); // Unauthorized
    }

    @Test
    public void test27_non_existent_physician() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        assertThat(response.getStatus(), is(404)); // Not found
    }

    @Test
    public void test28_non_existent_patient() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("99999")
            .request()
            .get();
        assertThat(response.getStatus(), is(404)); // Not found
    }

    @Test
    public void test29_invalid_json() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity("{invalid json", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(400)); // Bad request
    }

    @Test
    public void test30_duplicate_medical_school() throws JsonMappingException, JsonProcessingException {
        PublicSchool duplicateSchool = new PublicSchool();
        duplicateSchool.setName("University of California Medical School"); // This name already exists
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICAL_SCHOOL_RESOURCE_NAME)
            .request()
            .post(Entity.entity(duplicateSchool, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(409)); // Conflict
    }

    // ================================================
    // UPDATE TESTS
    // ================================================

    @Test
    public void test31_update_physician_admin() throws JsonMappingException, JsonProcessingException {
        Physician updateData = new Physician();
        updateData.setFirstName("Updated");
        updateData.setLastName("Name");
        
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Physician updated = response.readEntity(Physician.class);
        assertThat(updated.getFirstName(), is("Updated"));
    }

    @Test
    public void test32_update_patient_admin() throws JsonMappingException, JsonProcessingException {
        Patient updateData = new Patient();
        updateData.setFirstName("Updated");
        updateData.setLastName("Patient");
        updateData.setYear(1995);
        updateData.setAddress("Updated Address");
        updateData.setHeight(175);
        updateData.setWeight(75);
        updateData.setSmoker((byte) 0);
        
        Response response = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Patient updated = response.readEntity(Patient.class);
        assertThat(updated.getFirstName(), is("Updated"));
    }

    @Test
    public void test33_update_medicine_admin() throws JsonMappingException, JsonProcessingException {
        Medicine updateData = new Medicine();
        updateData.setDrugName("Updated Drug");
        updateData.setManufacturerName("Updated Pharma");
        updateData.setDosageInformation("Updated dosage");
        
        Response response = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .path("1")
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus(), is(200));
        Medicine updated = response.readEntity(Medicine.class);
        assertThat(updated.getDrugName(), is("Updated Drug"));
    }

    // ================================================
    // SECURITY TESTS
    // ================================================

    @Test
    public void test34_user_access_own_certificate() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
            .path("1")
            .request()
            .get();
        
        // Should return 200 if owned by user, or 403 if not owned
        assertTrue(response.getStatus() == 200 || response.getStatus() == 403);
    }

    @Test
    public void test35_content_type_validation() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .get();
        
        assertThat(response.getStatus(), is(200));
        String contentType = response.getHeaderString("Content-Type");
        assertTrue(contentType.contains("application/json"));
    }

    // ================================================
    // INTEGRATION TESTS
    // ================================================

    @Test
    public void test36_complete_physician_workflow() throws JsonMappingException, JsonProcessingException {
        // Create -> Read -> Update workflow
        
        // 1. Create
        Physician newPhysician = new Physician();
        newPhysician.setFirstName("Workflow");
        newPhysician.setLastName("Test");
        
        Response createResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPhysician, MediaType.APPLICATION_JSON));
        
        assertThat(createResponse.getStatus(), is(200));
        Physician created = createResponse.readEntity(Physician.class);
        int physicianId = created.getId();
        
        // 2. Read
        Response readResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .get();
        
        assertThat(readResponse.getStatus(), is(200));
        Physician read = readResponse.readEntity(Physician.class);
        assertThat(read.getFirstName(), is("Workflow"));
        
        // 3. Update
        Physician updateData = new Physician();
        updateData.setFirstName("Updated");
        updateData.setLastName("Workflow");
        
        Response updateResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path(String.valueOf(physicianId))
            .request()
            .put(Entity.entity(updateData, MediaType.APPLICATION_JSON));
        
        assertThat(updateResponse.getStatus(), is(200));
        Physician updated = updateResponse.readEntity(Physician.class);
        assertThat(updated.getFirstName(), is("Updated"));
    }

    @Test
    public void test37_patient_workflow() throws JsonMappingException, JsonProcessingException {
        // Create and verify patient
        Patient newPatient = new Patient();
        newPatient.setFirstName("Integration");
        newPatient.setLastName("TestPatient");
        newPatient.setYear(1990);
        newPatient.setAddress("Integration Test Address");
        newPatient.setHeight(170);
        newPatient.setWeight(70);
        newPatient.setSmoker((byte) 0);
        
        Response createResponse = webTarget
            .register(adminAuth)
            .path(PATIENT_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newPatient, MediaType.APPLICATION_JSON));
        
        assertThat(createResponse.getStatus(), is(200));
        Patient created = createResponse.readEntity(Patient.class);
        assertThat(created.getFirstName(), is("Integration"));
        assertTrue(created.getId() > 0);
    }

    @Test
    public void test38_medicine_workflow() throws JsonMappingException, JsonProcessingException {
        // Create and verify medicine
        Medicine newMedicine = new Medicine();
        newMedicine.setDrugName("Integration Test Drug");
        newMedicine.setManufacturerName("Test Integration Pharma");
        newMedicine.setDosageInformation("Take for integration testing");
        
        Response createResponse = webTarget
            .register(adminAuth)
            .path(MEDICINE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newMedicine, MediaType.APPLICATION_JSON));
        
        assertThat(createResponse.getStatus(), is(200));
        Medicine created = createResponse.readEntity(Medicine.class);
        assertThat(created.getDrugName(), is("Integration Test Drug"));
        assertTrue(created.getId() > 0);
    }

    @Test
    public void test39_prescription_access() throws JsonMappingException, JsonProcessingException {
        // User can access prescription by composite key
        Response response = webTarget
            .register(userAuth)
            .path(PRESCRIPTION_RESOURCE_NAME)
            .path("1")
            .path("1")
            .request()
            .get();
        
        assertThat(response.getStatus(), is(200));
        Prescription prescription = response.readEntity(Prescription.class);
        assertThat(prescription, is(notNullValue()));
    }

    @Test
    public void test40_comprehensive_error_handling() throws JsonMappingException, JsonProcessingException {
        // Test various error scenarios
        
        // 1. Wrong HTTP method
        Response wrongMethodResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .path("1")
            .request()
            .method("PATCH", Entity.entity("{}", MediaType.APPLICATION_JSON));
        
        assertThat(wrongMethodResponse.getStatus(), is(405)); // Method Not Allowed
        
        // 2. Unsupported media type
        Response unsupportedMediaResponse = webTarget
            .register(adminAuth)
            .path(PHYSICIAN_RESOURCE_NAME)
            .request()
            .post(Entity.entity("<xml></xml>", MediaType.APPLICATION_XML));
        
        assertThat(unsupportedMediaResponse.getStatus(), is(415)); // Unsupported Media Type
    }
}
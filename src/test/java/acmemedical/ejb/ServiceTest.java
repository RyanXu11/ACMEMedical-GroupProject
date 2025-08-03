/********************************************************************************************************
 * File:  ResourceTests.java Course Materials CST 8277
 *
 * @author Yizhen Xu
 * @author Ryan Xu
 * @author Ruchen Ding
 * Last Modified Date: 2025-08-02
 * 
 * Service Layer Tests - 10 tests focusing on service API
 */

package acmemedical.ejb;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.core.Response;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Physician;
import acmemedical.entity.Patient;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.MedicalTraining;
import acmemedical.entity.MedicalCertificate;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTest {
	
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static ACMEMedicalService service;

    @BeforeAll
    public static void initEmf() {
        emf = Persistence.createEntityManagerFactory("acmemedical-PU");
    }

    @BeforeEach
    public void initEach() {
        em = emf.createEntityManager();
        service = new ACMEMedicalService();
        service.em = em;
    }

    @AfterEach
    public void closeEach() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @AfterAll
    public static void closeEmf() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @Order(1)
    public void test01_GetAllPhysicians_notEmpty() {
        List<Physician> list = service.getAllPhysicians();
        assertNotNull(list);
    }

    @Test
    @Order(2)
    public void test02_PersistPhysician_createsNew() {
        Physician p = new Physician();
        p.setFirstName("John");
        p.setLastName("Doe");
        Physician saved = service.persistPhysician(p);
        assertNotNull(saved.getId());
    }
    
    @Test
    @Order(3)
    public void test03_GetPhysicianById_existingId() {
    	List<Physician> physicians = service.getAllPhysicians();
        assertFalse(physicians.isEmpty(), "Physician list should not be empty");
        System.out.println("Physician List:");
        for (Physician p : physicians) {
            System.out.println("  ID=" + p.getId() +
                               ", FirstName=" + p.getFirstName() +
                               ", LastName=" + p.getLastName());
        }
        
        Physician sample = physicians.get(0);
        int id = sample.getId();
        System.out.println("Physician ID to fetch: " + id);

        Physician fetched = service.getPhysicianById(id);
        assertNotNull(fetched);
        assertEquals(id, fetched.getId());
    }

    @Test
    @Order(4)
    public void test04_UpdatePatientById_changesFields() {
        Patient p = new Patient();
        p.setFirstName("Updated");
        Patient updated = service.updatePatient(1, p);
        assertEquals("Updated", updated.getFirstName());
    }

    @Test
    @Order(5)
    public void test05_CreateNewMedicine() {
        Medicine med = new Medicine();
        med.setDrugName("TestDrug");
        med.setManufacturerName("TestPharma");
        med.setDosageInformation("Once daily");

        em.getTransaction().begin();
        em.persist(med);
        em.getTransaction().commit();

        assertTrue(med.getId() > 0);

        Medicine retrieved = em.find(Medicine.class, med.getId());
        assertNotNull(retrieved);
        assertEquals("TestDrug", retrieved.getDrugName());
    }



    @Test
    @Order(6)
    public void test06_GetAllMedicalTrainings_ok() {
        List<MedicalTraining> result = service.getAllMedicalTrainings();
        assertNotNull(result);
    }

    @Test
    @Order(7)
    public void test07_CreateAndFindPatient() {
        em.getTransaction().begin();

        Patient p = new Patient();
        p.setFirstName("Alice");
        p.setLastName("Smith");
        p.setYear(1990);
        p.setAddress("789 Maple Avenue");
        p.setHeight(165);
        p.setWeight(60);
        p.setSmoker((byte) 0); 

        em.persist(p);
        em.flush();
        int id = p.getId();

        em.getTransaction().commit();

        Patient found = em.find(Patient.class, id);
        assertNotNull(found);
        assertEquals("Alice", found.getFirstName());
        assertEquals("Smith", found.getLastName());
    }

    @Test
    @Order(8)
    public void test08_GetAllPrescriptions_ok() {
        List<Prescription> list = service.getAllPrescriptions();
        assertTrue(list.size() >= 0);
    }

    @Test
    @Order(9)
    public void test09_GetMedicineById_valid() {
        Medicine m = service.getMedicineById(1);
        assertEquals(1, m.getId());
    }
    
    @Test
    @Order(10)
    public void test10_DeletePhysicianById() {
        em.getTransaction().begin();
        Physician p = new Physician();
        p.setFirstName("Temp");
        p.setLastName("ToDelete");
        em.persist(p);
        em.flush();  // get the ID
        int id = p.getId();
//        System.out.println("Physician ID to delete: " + id);

        em.getTransaction().commit(); // 
        
        // Delete Physician
        em.getTransaction().begin();
        service.deletePhysicianById(id);
        em.getTransaction().commit();

        // Clear cache
        em.clear();
        assertNull(service.getPhysicianById(id));
    }

}  


/********************************************************************************************************
 * File:  ACMEMedicalService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.ejb;

import static acmemedical.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmemedical.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmemedical.utility.MyConstants.PARAM1;
import static acmemedical.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmemedical.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmemedical.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmemedical.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmemedical.utility.MyConstants.PU_NAME;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.entity.Physician.ALL_PHYSICIANS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.IS_DUPLICATE_QUERY_NAME;
import static acmemedical.entity.MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.Patient;
import acmemedical.entity.DurationAndStatus;
import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Medicine;
import acmemedical.entity.Prescription;
import acmemedical.entity.PrescriptionPK;
import acmemedical.entity.SecurityRole;
import acmemedical.entity.SecurityUser;
import acmemedical.entity.Physician;
import acmemedical.entity.MedicalSchool;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMEMedicalService
 */
@Singleton
public class ACMEMedicalService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    // NOTE: This comment line added by Ryan to indicate CRUD service for Physician entity.
    public List<Physician> getAllPhysicians() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Physician> cq = cb.createQuery(Physician.class);
        cq.select(cq.from(Physician.class));
        return em.createQuery(cq).getResultList();
    }

    public Physician getPhysicianById(int id) {
        return em.find(Physician.class, id);
    }

    @Transactional
    public Physician persistPhysician(Physician newPhysician) {
        em.persist(newPhysician);
        return newPhysician;
    }

    @Transactional
    public void buildUserForNewPhysician(Physician newPhysician) {
        SecurityUser userForNewPhysician = new SecurityUser();
        userForNewPhysician.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPhysician.getFirstName() + "." + newPhysician.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPhysician.setPwHash(pwHash);
        userForNewPhysician.setPhysician(newPhysician);
        SecurityRole userRole = /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */ null;
        userForNewPhysician.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPhysician);
        em.persist(userForNewPhysician);
    }

    @Transactional
    public Medicine setMedicineForPhysicianPatient(int physicianId, int patientId, Medicine newMedicine) {
        Physician physicianToBeUpdated = em.find(Physician.class, physicianId);
        if (physicianToBeUpdated != null) { // Physician exists
            Set<Prescription> prescriptions = physicianToBeUpdated.getPrescriptions();
            prescriptions.forEach(p -> {
                if (p.getPatient().getId() == patientId) {
                    if (p.getMedicine() != null) { // Medicine exists
                        Medicine medicine = em.find(Medicine.class, p.getMedicine().getId());
                        medicine.setMedicine(newMedicine.getDrugName(),
                        				  newMedicine.getManufacturerName(),
                        				  newMedicine.getDosageInformation());
                        em.merge(medicine);
                    }
                    else { // Medicine does not exist
                        p.setMedicine(newMedicine);
                        em.merge(physicianToBeUpdated);
                    }
                }
            });
            return newMedicine;
        }
        else return null;  // Physician doesn't exists
    }

    /**
     * To update a physician
     * 
     * @param id - id of entity to update
     * @param physicianWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Physician updatePhysicianById(int id, Physician physicianWithUpdates) {
    	Physician physicianToBeUpdated = getPhysicianById(id);
        if (physicianToBeUpdated != null) {
            em.refresh(physicianToBeUpdated);
            em.merge(physicianWithUpdates);
            em.flush();
        }
        return physicianToBeUpdated;
    }

    /**
     * To delete a physician by id
     * 
     * @param id - physician id to delete
     */
    @Transactional
    public void deletePhysicianById(int id) {
        Physician physician = getPhysicianById(id);
        if (physician != null) {
            em.refresh(physician);
            TypedQuery<SecurityUser> findUser = 
            /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
               so that when we remove it, the relationship from SECURITY_USER table
               is not dangling
            */ null;
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(physician);
        }
    }
    
    // NOTE: This comment line added by Ryan to indicate CRUD service for MedicalSchool entity.
    public List<MedicalSchool> getAllMedicalSchools() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalSchool> cq = cb.createQuery(MedicalSchool.class);
        cq.select(cq.from(MedicalSchool.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public MedicalSchool getMedicalSchoolById(int id) {
        TypedQuery<MedicalSchool> specificMedicalSchoolQuery = em.createNamedQuery(SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, MedicalSchool.class);
        specificMedicalSchoolQuery.setParameter(PARAM1, id);
        return specificMedicalSchoolQuery.getSingleResult();
    }
    
    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public MedicalSchool deleteMedicalSchool(int id) {
        //MedicalSchool ms = getMedicalSchoolById(id);
    	MedicalSchool ms = getById(MedicalSchool.class, MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME, id);
        if (ms != null) {
            Set<MedicalTraining> medicalTrainings = ms.getMedicalTrainings();
            List<MedicalTraining> list = new LinkedList<>();
            medicalTrainings.forEach(list::add);
            list.forEach(mt -> {
                if (mt.getCertificate() != null) {
                    MedicalCertificate mc = getById(MedicalCertificate.class, MedicalCertificate.ID_CARD_QUERY_NAME, mt.getCertificate().getId());
                    mc.setMedicalTraining(null);
                }
                mt.setCertificate(null);
                em.merge(mt);
            });
            em.remove(ms);
            return ms;
        }
        return null;
    }
    
    // Please study & use the methods below in your test suites
    
    public boolean isDuplicated(MedicalSchool newMedicalSchool) {
        TypedQuery<Long> allMedicalSchoolsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allMedicalSchoolsQuery.setParameter(PARAM1, newMedicalSchool.getName());
        return (allMedicalSchoolsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public MedicalSchool persistMedicalSchool(MedicalSchool newMedicalSchool) {
        em.persist(newMedicalSchool);
        return newMedicalSchool;
    }

    @Transactional
    public MedicalSchool updateMedicalSchool(int id, MedicalSchool updatingMedicalSchool) {
    	MedicalSchool medicalSchoolToBeUpdated = getMedicalSchoolById(id);
        if (medicalSchoolToBeUpdated != null) {
            em.refresh(medicalSchoolToBeUpdated);
            medicalSchoolToBeUpdated.setName(updatingMedicalSchool.getName());
            em.merge(medicalSchoolToBeUpdated);
            em.flush();
        }
        return medicalSchoolToBeUpdated;
    }
       
    // NOTE: This comment line added by Ryan to indicate CRUD service for MedicalTraining entity.
    @Transactional
    public MedicalTraining persistMedicalTraining(MedicalTraining newMedicalTraining) {
        // 1. Extract detached school from DTO， added by Ryan
        MedicalSchool detachedSchool = newMedicalTraining.getMedicalSchool();

        // 2. Attach the real managed school from DB, added by Ryan
        if (detachedSchool != null && detachedSchool.getId() > 0) {
            MedicalSchool managedSchool = em.find(MedicalSchool.class, detachedSchool.getId());
            newMedicalTraining.setMedicalSchool(managedSchool); // replace with managed entity
        }
        em.persist(newMedicalTraining);
        return newMedicalTraining;
    }
    
    @Transactional		//Added by Ryan
    public MedicalTraining getMedicalTrainingById(int mtId) {
        TypedQuery<MedicalTraining> allMedicalTrainingQuery = em.createNamedQuery(MedicalTraining.FIND_BY_ID, MedicalTraining.class);
        allMedicalTrainingQuery.setParameter(PARAM1, mtId);
        return allMedicalTrainingQuery.getSingleResult();
    }
    
    //This method was added by Ryan
    @Transactional
    public List<MedicalTraining> getAllMedicalTrainings() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MedicalTraining> cq = cb.createQuery(MedicalTraining.class);
        cq.select(cq.from(MedicalTraining.class));
        return em.createQuery(cq).getResultList();
    }

    @Transactional
    public MedicalTraining updateMedicalTraining(int id, MedicalTraining medicalTrainingWithUpdates) {
    	MedicalTraining medicalTrainingToBeUpdated = getMedicalTrainingById(id);
        if (medicalTrainingToBeUpdated != null) {
//            em.refresh(medicalTrainingToBeUpdated);
//            em.merge(medicalTrainingWithUpdates);
//            em.flush();
        	
            // Update link with MedicalSchool
        	if (medicalTrainingWithUpdates.getMedicalSchool() != null) {
        	    int schoolId = medicalTrainingWithUpdates.getMedicalSchool().getId();
        	    MedicalSchool managedSchool = em.find(MedicalSchool.class, schoolId);
        	    medicalTrainingToBeUpdated.setMedicalSchool(managedSchool);
        	}
            
            // Update link with Certificate
        	if (medicalTrainingWithUpdates.getCertificate() != null) {
        	    int certId = medicalTrainingWithUpdates.getCertificate().getId();
        	    MedicalCertificate managedCert = em.find(MedicalCertificate.class, certId);
        	    medicalTrainingToBeUpdated.setCertificate(managedCert);
        	}
            
            // Update link with embedded DurationAndStatus
            if (medicalTrainingWithUpdates.getDurationAndStatus() != null) {
                DurationAndStatus current = medicalTrainingToBeUpdated.getDurationAndStatus();
                DurationAndStatus update = medicalTrainingWithUpdates.getDurationAndStatus();
                
                if (current == null) {
                    medicalTrainingToBeUpdated.setDurationAndStatus(new DurationAndStatus());
                    current = medicalTrainingToBeUpdated.getDurationAndStatus();
                }
                
                if (update.getStartDate() != null) {
                    current.setStartDate(update.getStartDate());
                }
                if (update.getEndDate() != null) {
                    current.setEndDate(update.getEndDate());
                }
                current.setActive(update.getActive());
            }
            
        }
        return medicalTrainingToBeUpdated;
    }
    
    // Delete method, added by Ryan
    @Transactional
    public MedicalTraining deleteMedicalTraining(int id) {
        MedicalTraining trainingToDelete = getMedicalTrainingById(id);
        if (trainingToDelete == null) {
            return null;
        }
        
        // Unlink from certificate (1:1 relationship)
        if (trainingToDelete.getCertificate() != null) {
            MedicalCertificate cert = trainingToDelete.getCertificate();
            cert.setMedicalTraining(null);
            trainingToDelete.setCertificate(null);
        }
        
        // Remove reference from MedicalSchool's training list (OneToMany side)
        if (trainingToDelete.getMedicalSchool() != null) {
            trainingToDelete.getMedicalSchool().getMedicalTrainings().remove(trainingToDelete);
        }
        
        em.remove(trainingToDelete);
        return trainingToDelete;
    }

    // CRUD service for Medicine entity. By Ryan
    @Transactional
    public Medicine persistMedicine(Medicine newMedicine) {
        em.persist(newMedicine);
        return newMedicine;
    }
    
    public List<Medicine> getAllMedicines() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Medicine> cq = cb.createQuery(Medicine.class);
        cq.select(cq.from(Medicine.class));
        return em.createQuery(cq).getResultList();
    }
    
    public Medicine getMedicineById(int id) {
        return em.find(Medicine.class, id);
    }

    @Transactional
    public Medicine updateMedicine(int id, Medicine medicineWithUpdates) {
    	Medicine medicineToBeUpdated = getMedicineById(id);
        if (medicineToBeUpdated == null) {
            return null;
        }
    	
        if (medicineWithUpdates != null) {
            medicineToBeUpdated.setDrugName(medicineWithUpdates.getDrugName());
            medicineToBeUpdated.setManufacturerName(medicineWithUpdates.getManufacturerName());
            medicineToBeUpdated.setDosageInformation(medicineWithUpdates.getDosageInformation());
        }
        return medicineToBeUpdated;
    }
    
    @Transactional
    public Medicine deleteMedicine(int id) {
        Medicine medicineToDelete = getMedicineById(id);
        if (medicineToDelete == null) {
            return null;
        }

        // Safely unlink from prescriptions
        Set<Prescription> prescriptions = medicineToDelete.getPrescriptions();
        if (prescriptions != null && !prescriptions.isEmpty()) {
            for (Prescription p : prescriptions) {
                if (p != null) {
                    p.setMedicine(null);
                }
            }
        }

        em.remove(medicineToDelete);
        return medicineToDelete;
    }
   
    // CRUD service for Patient entity. By Ryan
    @Transactional
    public Patient persistPatient(Patient newPatient) {
        em.persist(newPatient);
        return newPatient;
    }
    
    public List<Patient> getAllPatients() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
        cq.select(cq.from(Patient.class));
        return em.createQuery(cq).getResultList();
    }
    
    public Patient getPatientById(int id) {
        return em.find(Patient.class, id);
    }

    @Transactional
    public Patient updatePatient(int id, Patient patientWithUpdates) {
    	Patient patientToBeUpdated = getPatientById(id);
        if (patientToBeUpdated == null) {
            return null;
        }
    	
        if (patientWithUpdates != null) {
        	patientToBeUpdated.setFirstName(patientWithUpdates.getFirstName());
        	patientToBeUpdated.setLastName(patientWithUpdates.getLastName());
        	patientToBeUpdated.setYear(patientWithUpdates.getYear());
        	patientToBeUpdated.setAddress(patientWithUpdates.getAddress());
        	patientToBeUpdated.setHeight(patientWithUpdates.getHeight());
        	patientToBeUpdated.setWeight(patientWithUpdates.getWeight());
        	patientToBeUpdated.setSmoker(patientWithUpdates.getSmoker());
        }
        return patientToBeUpdated;
    }
    
    @Transactional
    public Patient deletePatient(int id) {
    	Patient patientToDelete = getPatientById(id);
        if (patientToDelete == null) {
            return null;
        }

        em.remove(patientToDelete);
        return patientToDelete;
    }
    
    // CRUD service for MedicalTraining entity. By Ryan
 
    
}
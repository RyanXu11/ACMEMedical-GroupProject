/********************************************************************************************************
 * File:  EntityValidationUtil.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-22
 * Last Modified Date: 2025-07-22
 * Description: Validation methods for entities.
 */

package acmemedical.utility;

import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import acmemedical.entity.*;
import acmemedical.rest.resource.HttpErrorResponse;

public class EntityValidationUtil {

    public static boolean physicianExists(EntityManager em, int id) {
        return em.find(Physician.class, id) != null;
    }

    public static boolean patientExists(EntityManager em, int id) {
        return em.find(Patient.class, id) != null;
    }

    public static boolean medicineExists(EntityManager em, int id) {
        return em.find(Medicine.class, id) != null;
    }

    public static boolean schoolExists(EntityManager em, int id) {
        return em.find(MedicalSchool.class, id) != null;
    }

    public static boolean trainingExists(EntityManager em, int id) {
        return em.find(MedicalTraining.class, id) != null;
    }

    public static boolean certificateExists(EntityManager em, int id) {
        return em.find(MedicalCertificate.class, id) != null;
    }

    public static boolean prescriptionExists(EntityManager em, int physicianId, int patientId) {
        PrescriptionPK key = new PrescriptionPK(physicianId, patientId);
        return em.find(Prescription.class, key) != null;
    }

    public static Response validateEntityExists(String entityType, int id, boolean exists) {
        if (!exists) {
            HttpErrorResponse error = new HttpErrorResponse(404, entityType + " with ID " + id + " not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return null;  // OK
    }
    
    public static <T> Response validateFound(T entity, String entityName, int id) {
        if (entity == null) {
            return Response.status(Status.NOT_FOUND)
                .entity(new HttpErrorResponse(404, entityName + " with ID " + id + " not found"))
                .build();
        }
        return null;
    }

}

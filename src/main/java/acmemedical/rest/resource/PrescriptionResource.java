/********************************************************************************************************
 * File:  PrescriptionResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-22
 * Last Modified Date: 2025-07-22
 * Description: REST API resource class to expose CRUD operations for Prescription entity.
 */

package acmemedical.rest.resource;

import acmemedical.utility.EntityValidationUtil;
import acmemedical.utility.ErrorResponseUtil;
import acmemedical.ejb.ACMEMedicalService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.PRESCRIPTION_RESOURCE_NAME;
import static acmemedical.utility.MyConstants.PU_NAME;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.Prescription;

@Path(PRESCRIPTION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class PrescriptionResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @GET
    public Response getPrescriptions() {
        LOG.debug("Retrieving all Prescription...");
        List<Prescription> patients = service.getAllPrescriptions();
        LOG.debug("Prescription found = {}", patients);
        Response response = Response.ok(patients).build();
        return response;
    }

    @GET
    @Path("/{physicianId}/{patientId}")
    public Response getPrescriptionByIds(@PathParam("physicianId") int physicianId,
                                         @PathParam("patientId") int patientId) {
        Prescription prescription = service.getPrescriptionByIds(physicianId, patientId);
        if (prescription == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Prescription not found for Physician ID " + physicianId + " and Patient ID " + patientId)
                           .build();
        }
        return Response.ok(prescription).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response createPrescription(Prescription prescription) {
        try {
            // Errors from REST
            if (prescription.getPhysician() != null && prescription.getPatient() != null) {
                int physicianId = prescription.getPhysician().getId();
                int patientId = prescription.getPatient().getId();
                
            	Response validationError = EntityValidationUtil.validateEntityExists("Physician", physicianId, 
            			EntityValidationUtil.physicianExists(em, physicianId));
                	if (validationError != null) return validationError;

                	validationError = EntityValidationUtil.validateEntityExists("Patient", patientId, 
                			EntityValidationUtil.patientExists(em, patientId));  
                	if (validationError != null) return validationError;
                
                if (EntityValidationUtil.prescriptionExists(em, physicianId, patientId)) {
                    String msg = "Prescription already exists for Physician ID " + physicianId + " and Patient ID " + patientId;
                    return ErrorResponseUtil.conflict(msg);
                }
            }
            
            Prescription created = service.persistPrescription(prescription);
            return Response.status(Response.Status.CREATED).entity(created).build();
            
        } catch (PersistenceException e) {
            // Errors from database
            if (e.getCause() instanceof ConstraintViolationException) {
                return ErrorResponseUtil.conflict("A prescription already exists for this physician and patient combination");
            }
            return ErrorResponseUtil.internalServerError("Database error occurred");

        } catch (Exception e) {
            return ErrorResponseUtil.internalServerError("Internal server error");
        }
    }

    @PUT
    @Path("/{physicianId}/{patientId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updatePrescription(@PathParam("physicianId") int physicianId,
            @PathParam("patientId") int patientId, Prescription updatedPs) {
    	Prescription result = service.updatePrescription(physicianId, patientId, updatedPs);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new HttpErrorResponse(404, "Prescription not found"))
                .build();
        }
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{physicianId}/{patientId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deletePrescription(@PathParam("physicianId") int physicianId,
            							@PathParam("patientId") int patientId) {
    	Prescription deleted = service.deletePrescription(physicianId, patientId);
        if (deleted == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "Prescription not found"))
                    .build();
        }
        return Response.ok(deleted).build();
    }
}
 
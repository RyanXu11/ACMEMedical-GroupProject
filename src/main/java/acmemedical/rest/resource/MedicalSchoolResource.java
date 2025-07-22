/********************************************************************************************************
 * File:  MedicalSchoolResource.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package acmemedical.rest.resource;

import java.util.List;

import acmemedical.utility.EntityValidationUtil;
import acmemedical.ejb.ACMEMedicalService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;		//Added by Ryan
import jakarta.persistence.NoResultException;	//Added by Ryan
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import jakarta.ws.rs.core.Response.Status;
import static acmemedical.utility.MyConstants.MEDICAL_SCHOOL_RESOURCE_NAME;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalTraining;
import acmemedical.entity.MedicalSchool;

@Path(MEDICAL_SCHOOL_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalSchoolResource {
    
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMEMedicalService service;

    @Inject
    protected SecurityContext sc;
    
    @GET
    public Response getMedicalSchools() {
        LOG.debug("Retrieving all medical schools...");
        List<MedicalSchool> medicalSchools = service.getAllMedicalSchools();
        LOG.debug("Medical schools found = {}", medicalSchools);
        return Response.ok(medicalSchools).build();
    }
    
    @GET
    // TODO MSR01 - Specify the roles allowed for this method
    @Path("/{medicalSchoolId}")
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getMedicalSchoolById(@PathParam("medicalSchoolId") int medicalSchoolId) {
        LOG.debug("Retrieving medical school with id = {}", medicalSchoolId);
        MedicalSchool result = service.getMedicalSchoolById(medicalSchoolId);
        Response err = EntityValidationUtil.validateEntityExists("MedicalSchool", medicalSchoolId, result != null);
        if (err != null) return err;
        return Response.ok(result).build();
    }

    @DELETE
    // TODO MSR02 - Specify the roles allowed for this method
    @Path("/{medicalSchoolId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicalSchool(@PathParam("medicalSchoolId") int msId) {
        LOG.debug("Deleting medical school with id = {}", msId);
        // try...catch added by Ryan when id is not exist
        try {
            MedicalSchool deleted = service.deleteMedicalSchool(msId);
            return Response.ok(deleted).build();
        } catch (Exception e) {
            return handleException(e, "MedicalSchool", msId);
        }
    }
    
    // Please try to understand and test the below methods:
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalSchool(MedicalSchool newMedicalSchool) {
        LOG.debug("Adding a new medical school = {}", newMedicalSchool);
        if (service.isDuplicated(newMedicalSchool)) {
            HttpErrorResponse err = new HttpErrorResponse(Status.CONFLICT.getStatusCode(), "Entity already exists");
            return Response.status(Status.CONFLICT).entity(err).build();
        }
        else {
            MedicalSchool tempMedicalSchool = service.persistMedicalSchool(newMedicalSchool);
            return Response.ok(tempMedicalSchool).build();
        }
    }

    @POST
    @Path("/{medicalSchoolId}/medicaltraining")
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalTrainingToMedicalSchool(@PathParam("medicalSchoolId") int msId, MedicalTraining newMedicalTraining) {
        LOG.debug("Adding a new MedicalTraining to medical school with id = {}", msId);
        
        MedicalSchool ms = service.getMedicalSchoolById(msId);
        Response err = EntityValidationUtil.validateEntityExists("MedicalSchool", msId, ms != null);
        if (err != null) return err;
        
        newMedicalTraining.setMedicalSchool(ms);
        ms.getMedicalTrainings().add(newMedicalTraining);
        service.updateMedicalSchool(msId, ms);
        
        return Response.ok(newMedicalTraining).build();		//Fix "sc" to "newMedicalTraining", by Ryan
    }

    @PUT
    @Path("/{medicalSchoolId}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicalSchool(@PathParam("medicalSchoolId") int msId, MedicalSchool updatingMedicalSchool) {
        LOG.debug("Updating a specific medical school with id = {}", msId);
        // try...catch added by Ryan when id is not exist
        try {
            MedicalSchool updatedMedicalSchool = service.updateMedicalSchool(msId, updatingMedicalSchool);
            Response err = EntityValidationUtil.validateEntityExists("MedicalSchool", msId, updatedMedicalSchool != null);
            if (err != null) return err;
            return Response.ok(updatedMedicalSchool).build();
        } catch (Exception e) {
            return handleException(e, "MedicalSchool", msId);
        }
    }
    
    private Response handleException(Exception e, String entityType, int id) {
        if (e instanceof EJBException && e.getCause() instanceof NoResultException) {
            return Response.status(Status.NOT_FOUND)
                           .entity(new HttpErrorResponse(404, entityType + " with ID " + id + " not found"))
                           .build();
        }
        LOG.error("Unexpected error: ", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity(new HttpErrorResponse(500, "Unexpected error occurred."))
                       .build();
    }
    
}

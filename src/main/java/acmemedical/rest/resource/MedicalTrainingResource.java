/********************************************************************************************************
 * File:  MedicalTrainingResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-21
 * Last Modified Date: 2025-07-21
 * Description: REST API resource class to expose CRUD operations for MedicalTraining entity.
 */

package acmemedical.rest.resource;

import acmemedical.ejb.ACMEMedicalService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
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
import static acmemedical.utility.MyConstants.MEDICAL_TRAINING_RESOURCE_NAME;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.MedicalTraining;

@Path(MEDICAL_TRAINING_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class MedicalTrainingResource {

    @EJB
    protected ACMEMedicalService service;

    private static final Logger LOG = LogManager.getLogger();
    
    @GET
    public Response getMedicalTrainings() {
        LOG.debug("Retrieving all MedicalTraining...");
        List<MedicalTraining> medicalTrainings = service.getAllMedicalTrainings();
        LOG.debug("MedicalTraining found = {}", medicalTrainings);
        Response response = Response.ok(medicalTrainings).build();
        return response;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getMedicalTrainingById(@PathParam("id") int id) {
        LOG.debug("Fetching MedicalTraining by ID: {}", id);
        MedicalTraining mt = service.getMedicalTrainingById(id);
        if (mt == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "MedicalTraining not found"))
                    .build();
        }
        return Response.ok(mt).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalTraining(MedicalTraining newMt) {
    	MedicalTraining created = service.persistMedicalTraining(newMt);
        return Response.ok(created).build();  // Or Response.status(201)...
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicalTraining(@PathParam("id") int id, MedicalTraining updatedMt) {
    	MedicalTraining result = service.updateMedicalTraining(id, updatedMt);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new HttpErrorResponse(404, "MedicalTraining not found"))
                .build();
        }
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicalTraining(@PathParam("id") int id) {
    	MedicalTraining deleted = service.deleteMedicalTraining(id);
        if (deleted == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "MedicalTraining not found"))
                    .build();
        }
        return Response.ok(deleted).build();
    }
}
 
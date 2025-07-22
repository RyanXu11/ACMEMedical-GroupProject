/********************************************************************************************************
 * File:  PatientResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-21
 * Last Modified Date: 2025-07-21
 * Description: REST API resource class to expose CRUD operations for Patient entity.
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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.ejb.ACMEMedicalService;
import acmemedical.entity.Patient;

@Path("patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class PatientResource {

    @EJB
    protected ACMEMedicalService service;

    private static final Logger LOG = LogManager.getLogger();
    
    @GET
    public Response getPatients() {
        LOG.debug("Retrieving all Patient...");
        List<Patient> patients = service.getAllPatients();
        LOG.debug("Patient found = {}", patients);
        Response response = Response.ok(patients).build();
        return response;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getPatientById(@PathParam("id") int id) {
        LOG.debug("Fetching Patient by ID: {}", id);
        Patient pat = service.getPatientById(id);
        if (pat == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "Patient not found"))
                    .build();
        }
        return Response.ok(pat).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPatient(Patient newPt) {
    	Patient created = service.persistPatient(newPt);
        return Response.ok(created).build();  // Or Response.status(201)...
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updatePatient(@PathParam("id") int id, Patient updatedPat) {
    	Patient result = service.updatePatient(id, updatedPat);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new HttpErrorResponse(404, "Patient not found"))
                .build();
        }
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deletePatient(@PathParam("id") int id) {
    	Patient deleted = service.deletePatient(id);
        if (deleted == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "Patient not found"))
                    .build();
        }
        return Response.ok(deleted).build();
    }
}
 
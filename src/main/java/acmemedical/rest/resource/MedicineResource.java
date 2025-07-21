/********************************************************************************************************
 * File:  MedicineResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-21
 * Last Modified Date: 2025-07-21
 * Description: REST API resource class to expose CRUD operations for Medicine entity.
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
import acmemedical.entity.MedicalSchool;
import acmemedical.entity.Medicine;

@Path("medicines")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class MedicineResource {

    @EJB
    protected ACMEMedicalService service;

    private static final Logger LOG = LogManager.getLogger();
    
    @GET
    public Response getMedicines() {
        LOG.debug("Retrieving all Medicine...");
        List<Medicine> medicines = service.getAllMedicines();
        LOG.debug("Medicine found = {}", medicines);
        Response response = Response.ok(medicines).build();
        return response;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getMedicineById(@PathParam("id") int id) {
        LOG.debug("Fetching Medicine by ID: {}", id);
        Medicine med = service.getMedicineById(id);
        if (med == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "Medicine not found"))
                    .build();
        }
        return Response.ok(med).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicine(Medicine newMed) {
        Medicine created = service.persistMedicine(newMed);
        return Response.ok(created).build();  // Or Response.status(201)...
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicine(@PathParam("id") int id, Medicine updatedMed) {
        Medicine result = service.updateMedicine(id, updatedMed);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new HttpErrorResponse(404, "Medicine not found"))
                .build();
        }
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicine(@PathParam("id") int id) {
        Medicine deleted = service.deleteMedicine(id);
        if (deleted == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new HttpErrorResponse(404, "Medicine not found"))
                    .build();
        }
        return Response.ok(deleted).build();
    }
}
 
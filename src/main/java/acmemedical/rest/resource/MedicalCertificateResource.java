/********************************************************************************************************
 * File:  MedicalCertificateResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-22
 * Last Modified Date: 2025-07-22
 * Description: REST API resource class to expose CRUD operations for MedicalCertificate entity.
 */

package acmemedical.rest.resource;

import acmemedical.utility.EntityValidationUtil;
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
import static acmemedical.utility.MyConstants.MEDICAL_CERTIFICATE_RESOURCE_NAME;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmemedical.entity.MedicalCertificate;

@Path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class MedicalCertificateResource {

    @EJB
    protected ACMEMedicalService service;

    private static final Logger LOG = LogManager.getLogger();
    
    @GET
    public Response getMedicalCertificates() {
        LOG.debug("Retrieving all MedicalCertificate...");
        List<MedicalCertificate> medicalCertificates = service.getAllMedicalCertificates();
        LOG.debug("MedicalCertificate found = {}", medicalCertificates);
        Response response = Response.ok(medicalCertificates).build();
        return response;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getMedicalCertificateById(@PathParam("id") int id) {
        LOG.debug("Fetching MedicalCertificate by ID: {}", id);
        MedicalCertificate result = service.getMedicalCertificateById(id);
        
        Response err = EntityValidationUtil.validateEntityExists("MedicalCertificate", id, result != null);
        if (err != null) return err;
        
        return Response.ok(result).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalCertificate(MedicalCertificate newMc) {
    	MedicalCertificate created = service.persistMedicalCertificate(newMc);
        return Response.ok(created).build();  // Or Response.status(201)...
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicalCertificate(@PathParam("id") int id, MedicalCertificate updatedMc) {
    	MedicalCertificate result = service.updateMedicalCertificate(id, updatedMc);
    	
        Response err = EntityValidationUtil.validateEntityExists("MedicalCertificate", id, result != null);
        if (err != null) return err;
        
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteMedicalCertificate(@PathParam("id") int id) {
    	MedicalCertificate deleted = service.deleteMedicalCertificate(id);
    	
        Response err = EntityValidationUtil.validateEntityExists("MedicalCertificate", id, deleted != null);
        if (err != null) return err;
        
        return Response.ok(deleted).build();
    }
}
 
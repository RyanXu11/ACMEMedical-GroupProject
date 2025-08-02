/********************************************************************************************************
 * File:  MedicalCertificateResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Yizhen Xu
 * @author Ryan Xu
 * @author Ruchen Ding
 * Last Modified Date: 2025-08-02
 * Created Date: 2025-07-22
 * Description: REST API resource class to expose CRUD operations for MedicalCertificate entity.
 */

package acmemedical.rest.resource;

import acmemedical.utility.EntityValidationUtil;
import acmemedical.ejb.ACMEMedicalService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.security.enterprise.SecurityContext;

import static acmemedical.utility.MyConstants.ADMIN_ROLE;
import static acmemedical.utility.MyConstants.USER_ROLE;
import static acmemedical.utility.MyConstants.MEDICAL_CERTIFICATE_RESOURCE_NAME;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmemedical.entity.MedicalCertificate;
import acmemedical.entity.Physician;
import acmemedical.entity.SecurityUser;

@Path(MEDICAL_CERTIFICATE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class MedicalCertificateResource {

    @EJB
    protected ACMEMedicalService service;
    
    @Inject
    protected SecurityContext sc;

    private static final Logger LOG = LogManager.getLogger();
    
    @GET
    @RolesAllowed({ADMIN_ROLE})
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
        
        if (sc.isCallerInRole(USER_ROLE)) {
            // USER_ROLE can only access related certificates
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Physician userPhysician = sUser.getPhysician();
            
            if (userPhysician == null) {
                throw new ForbiddenException("User has no associated physician");
            }
            
            MedicalCertificate result = service.getMedicalCertificateById(id);
            if (result == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new HttpErrorResponse(404, "MedicalCertificate not found"))
                        .build();
            }
            
            if (!result.getOwner().equals(userPhysician)) {
                throw new ForbiddenException("User trying to access certificate not owned by them");
            }
            
            return Response.ok(result).build();
        } else {
            // ADMIN_ROLE can access all certificates
            MedicalCertificate result = service.getMedicalCertificateById(id);
            Response err = EntityValidationUtil.validateEntityExists("MedicalCertificate", id, result != null);
            if (err != null) return err;
            return Response.ok(result).build();
        }
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMedicalCertificate(MedicalCertificate newMc) {
        return service.persistMedicalCertificate(newMc);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateMedicalCertificate(@PathParam("id") int id, MedicalCertificate updatedMc) {
        return service.updateMedicalCertificate(id, updatedMc);
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
 
/********************************************************************************************************
 * File:  MedicalCertificateResource.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-22
 * Last Modified Date: 2025-07-22
 * Description: Methods for error dealing
 */

package acmemedical.utility;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import acmemedical.rest.resource.HttpErrorResponse;

public class ErrorResponseUtil {

    public static Response conflict(String message) {
        return Response.status(Status.CONFLICT)
                       .entity(new HttpErrorResponse(409, message))
                       .build();
    }

    public static Response notFound(String message) {
        return Response.status(Status.NOT_FOUND)
                       .entity(new HttpErrorResponse(404, message))
                       .build();
    }

    public static Response internalServerError(String message) {
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity(new HttpErrorResponse(500, message))
                       .build();
    }

    
}

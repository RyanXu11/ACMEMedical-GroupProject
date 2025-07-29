/********************************************************************************************************
 * File:  NotAuthorizedToClientErrorMapper.java
 * Course Materials CST 8277
 * 
 * @author Ryan Xu
 * @date 2025-07-26
 *
 * This class maps NotAuthorizedException to a standard JSON error response.
 */
package acmemedical.rest;

import jakarta.ws.rs.NotAuthorizedException;
import acmemedical.rest.resource.HttpErrorResponse;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;

/**
 * Converts 401 Unauthorized exception into JSON using HttpErrorResponse.
 */
@Provider
public class NotAuthorizedToClientErrorMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        HttpErrorResponse error = new HttpErrorResponse(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            Response.Status.UNAUTHORIZED.getReasonPhrase()
        );
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity(error)
                       .build();
    }
}

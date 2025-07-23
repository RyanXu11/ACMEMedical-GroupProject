/********************************************************************************************************
 * File:  EntityOperationResponse.java
 * Course: CST8277
 * Professor: Teddy Yap
 * @author Ryan Xu
 * Created Date: 2025-07-22
 * Last Modified Date: 2025-07-22
 * Description: Validation methods for entities.
 */

package acmemedical.utility;

import java.io.Serializable;

/**
 * Generic response wrapper for service-layer operations.
 * Used to combine a message and the affected entity as a JSON response body.
 *
 * @param <T> the type of the entity being returned
 */
public class EntityOperationResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private T entity;

    public EntityOperationResponse() {
    }

    public EntityOperationResponse(String message, T entity) {
        this.message = message;
        this.entity = entity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}

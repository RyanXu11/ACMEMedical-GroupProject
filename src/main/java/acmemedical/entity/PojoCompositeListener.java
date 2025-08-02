/********************************************************************************************************
 * File:  PojoCompositeListener.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author Yizhen Xu
 * @author Ryan Xu
 * @author Ruchen Ding
 * Last Modified Date: 2025-08-02
 * 
 */
package acmemedical.entity;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@SuppressWarnings("unused")

public class PojoCompositeListener {

	// TODO PCL01 - What annotation is used when we want to do something just before object is INSERT'd into database?
    @PrePersist // PCL01 - Called just before INSERT
	public void setCreatedOnDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		LocalDateTime now = LocalDateTime.now();
		// TODO PCL02 - What member field(s) do we wish to alter just before object is INSERT'd in the database?
		pojoBaseComposite.setCreated(now); // PCL02 - Set both created and updated timestamps on insert
        pojoBaseComposite.setUpdated(now); // PCL02 - Set both created and updated timestamps on insert
    }

	// TODO PCL03 - What annotation is used when we want to do something just before object is UPDATE'd into database?
    @PreUpdate  // PCL03 - Called just before UPDATE
    public void setUpdatedDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		// TODO PCL04 - What member field(s) do we wish to alter just before object is UPDATE'd in the database?
    	pojoBaseComposite.setUpdated(LocalDateTime.now()); // PCL04 - Only update 'updated' timestamp on update
    }

}

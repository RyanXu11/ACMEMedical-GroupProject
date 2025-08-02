/********************************************************************************************************
 * File:  PojoListener.java Course Materials CST 8277
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

public class PojoListener {

	// TODO PL01 - What annotation is used when we want to do something just before object is INSERT'd in the database?
	 @PrePersist // PL01 - Called before INSERT 
	public void setCreatedOnDate(PojoBase pojoBase) {
		LocalDateTime now = LocalDateTime.now();
		// TODO PL02 - What member field(s) do we wish to alter just before object is INSERT'd in the database?
		pojoBase.setCreated(now); // PL02 - Set both timestamps on insert
	    pojoBase.setUpdated(now); // PL02 - Set both timestamps on insert
	 }

	// TODO PL03 - What annotation is used when we want to do something just before object is UPDATE'd in the database?
	 @PreUpdate // PL03 - Called before UPDATE
	 public void setUpdatedDate(PojoBase pojoBase) {
		// TODO PL04 - What member field(s) do we wish to alter just before object is UPDATE'd in the database?
	    pojoBase.setUpdated(LocalDateTime.now()); // PL04 - Only update 'updated' timestamp
	 }

}

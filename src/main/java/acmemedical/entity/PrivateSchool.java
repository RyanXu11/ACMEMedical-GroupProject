/********************************************************************************************************
 * File:  PrivateSchool.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Yizhen Xu
 * @author Ryan Xu
 * @author Ruchen Ding
 * Last Modified Date: 2025-08-02
 * 
 */
package acmemedical.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue; // added by Ruchen
import jakarta.persistence.Entity; // added by Ruchen

//TODO PRSC01 - Add missing annotations, please see Week 9 slides page 15.  Value 1 is public and value 0 is private.
@Entity  // PRSC01
@DiscriminatorValue("0")  // PRSC01, Modified "private" to "1"
//TODO PRSC02 - Is a JSON annotation needed here?
// no annotation neede (PRSC02)
public class PrivateSchool extends MedicalSchool implements Serializable {
	private static final long serialVersionUID = 1L;

	public PrivateSchool() {
		super(false);

	}
}
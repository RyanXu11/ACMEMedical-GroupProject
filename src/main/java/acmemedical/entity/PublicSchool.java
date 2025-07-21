/********************************************************************************************************
 * File:  PublicSchool.java Course materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author Ruchen Ding
 * 
 */
package acmemedical.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue; // added by Ruchen - start
import jakarta.persistence.Entity; // added by Ruchen - end

//TODO PUSC01 - Add missing annotations, please see Week 9 slides page 15.  Value 1 is public and value 0 is private.

@Entity  // PUSC01
@DiscriminatorValue("1")  // PUSC01, Modified "public" to "1"
//TODO PUSC02 - Is a JSON annotation needed here?
// no annotation needed (PUSC02)
public class PublicSchool extends MedicalSchool implements Serializable {
	private static final long serialVersionUID = 1L;

	public PublicSchool() {
		super(true);
	}
}

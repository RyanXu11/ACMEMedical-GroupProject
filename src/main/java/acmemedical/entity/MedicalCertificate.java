/********************************************************************************************************
 * File:  MedicalCertificate.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Ruchen Ding
 * 
 */
package acmemedical.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Access; // added by Ruchen - start
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table; // added by Ruchen - end
import jakarta.persistence.AttributeOverride; // added by Ryan
import jakarta.persistence.CascadeType;

@SuppressWarnings("unused")

/**
 * The persistent class for the medical_certificate database table.
 */
//TODO MC01 - Add the missing annotations.
@Entity  // MC01
@Table(name = "medical_certificate")  // MC01
@Access(AccessType.FIELD)  // MC01
//TODO MC02 - Do we need a mapped super class?  If so, which one?
@AttributeOverride(name = "id", column = @Column(name = "certificate_id")) // Added by Ryan
@NamedQuery(
	    name = MedicalCertificate.ID_CARD_QUERY_NAME,
	    query = "SELECT mc FROM MedicalCertificate mc WHERE mc.id = :param1"
	)	//Added by Ryan
public class MedicalCertificate extends PojoBase implements Serializable { // MC02
	private static final long serialVersionUID = 1L;
	
	public static final String ID_CARD_QUERY_NAME = "MedicalCertificate.findById";	//Added by Ryan

	// TODO MC03 - Add annotations for 1:1 mapping.  What should be the cascade and fetch types?
//	@OneToOne(mappedBy = "certificate", fetch = FetchType.LAZY) // MC03
	@OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})	//Added by Ryan
	@JoinColumn(name = "training_id", referencedColumnName = "training_id", nullable = true)	//Added by Ryan
	@JsonIgnore 	//To prevent infinte-loop, Added by Ryan
	private MedicalTraining medicalTraining;

	// TODO MC04 - Add annotations for M:1 mapping.  What should be the cascade and fetch types?
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "physician_id", referencedColumnName = "id", nullable = false)  // MC04
	@JsonIgnore 	//To prevent infinte-loop, Added by Ryan
	private Physician owner;

	// TODO MC05 - Add annotations.
	@Column(name = "signed", nullable = false)  // MC05
	private byte signed;

	public MedicalCertificate() {
		super();
	}
	
	public MedicalCertificate(MedicalTraining medicalTraining, Physician owner, byte signed) {
		this();
		this.medicalTraining = medicalTraining;
		this.owner = owner;
		this.signed = signed;
	}

	public MedicalTraining getMedicalTraining() {
		return medicalTraining;
	}

	public void setMedicalTraining(MedicalTraining medicalTraining) {
		this.medicalTraining = medicalTraining;
	}

	public Physician getOwner() {
		return owner;
	}

	public void setOwner(Physician owner) {
		this.owner = owner;
	}

	public byte getSigned() {
		return signed;
	}

	public void setSigned(byte signed) {
		this.signed = signed;
	}

	public void setSigned(boolean signed) {
		this.signed = (byte) (signed ? 0b0001 : 0b0000);
	}
	
	//Inherited hashCode/equals is sufficient for this entity class

}
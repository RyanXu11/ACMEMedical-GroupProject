/********************************************************************************************************
 * File:  Patient.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Ruchen Ding
 * 
 */
package acmemedical.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access; // added by Ruchen - start
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table; // added by Ruchen - end

@SuppressWarnings("unused")

/**
 * The persistent class for the patient database table.
 */
//TODO PA01 - Add the missing annotations.
@Entity  // PA01
@Table(name = "patient")  // PA01
@Access(AccessType.FIELD) // PA01
//TODO PA02 - Do we need a mapped super class?  If so, which one?
@AttributeOverride(name = "id", column = @Column(name = "patient_id"))  //Added by Ryan Xu
public class Patient extends PojoBase implements Serializable { // PA02 - Yes. Patient extends PojoBase which is a mapped superclass.
	private static final long serialVersionUID = 1L;
	
	// TODO PA03 - Add missing annotations.
	@Column(name = "first_name", nullable = false, length = 50) // PA03
	private String firstName;

	// TODO PA04 - Add missing annotations.
	@Column(name = "last_name", nullable = false, length = 50)  // PA04
	private String lastName;

	// TODO PA05 - Add missing annotations.
	@Column(name = "year_of_birth", nullable = false)  // PA05, column named fixed by Ryan
	private int year;

	// TODO PA06 - Add missing annotations.
	@Column(name = "home_address", length = 100)  // PA06, column named fixed by Ryan
	private String address;

	// TODO PA07 - Add missing annotations.
	@Column(name = "height_cm")  // PA07, column named fixed by Ryan
	private int height;

	// TODO PA08 - Add missing annotations.
	@Column(name = "weight_kg")  // PA08, column named fixed by Ryan
	private int weight;

	// TODO PA09 - Add missing annotations.
	@Column(name = "smoker")  // PA09
	private byte smoker;

	// TODO PA10 - Add annotations for 1:M relation.  What should be the cascade and fetch types?
	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)  // PA10
	private Set<Prescription> prescriptions = new HashSet<>();

	public Patient() {
		super();
	}

	public Patient(String firstName, String lastName, int year, String address, int height, int weight, byte smoker) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
		this.year = year;
		this.address = address;
		this.height = height;
		this.weight = weight;
		this.smoker = smoker;
	}

	public Patient setPatient(String firstName, String lastName, int year, String address, int height, int weight, byte smoker) {
		setFirstName(firstName);
		setLastName(lastName);
		setYear(year);
		setAddress(address);
		setHeight(height);
		setWeight(weight);
		setSmoker(smoker);
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public byte getSmoker() {
		return smoker;
	}

	public void setSmoker(byte smoker) {
		this.smoker = smoker;
	}
	
	public Set<Prescription> getPrescriptions() {
		return prescriptions;
	}

	public void setPrescription(Set<Prescription> prescriptions) {
		this.prescriptions = prescriptions;
	}

	//Inherited hashCode/equals is sufficient for this Entity class

}

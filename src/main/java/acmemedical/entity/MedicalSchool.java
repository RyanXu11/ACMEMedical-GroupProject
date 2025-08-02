/********************************************************************************************************
 * File:  MedicalSchool.java Course Materials CST 8277
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes; // added by Ruchen - start
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table; // added by Ruchen - end
import jakarta.persistence.NamedQueries; //Added by Ryan
import jakarta.persistence.NamedQuery;	//Added by Ryan


/**
 * The persistent class for the medical_school database table.
 */
//TODO MS01 - Add the missing annotations.
//TODO MS02 - MedicalSchool has subclasses PublicSchool and PrivateSchool.  Look at Week 9 slides for InheritanceType.
//TODO MS03 - Do we need a mapped super class?  If so, which one?
//TODO MS04 - Add in JSON annotations to indicate different sub-classes of MedicalSchool
@Entity  // MS01
@Table(name = "medical_school")  // MS01
@Access(AccessType.FIELD)  // MS01
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // MS02
@DiscriminatorColumn(name = "public", discriminatorType = DiscriminatorType.INTEGER)  // MS02, fixed the name from "school_type" to "public" by Ryan
@AttributeOverride(name = "id", column = @Column(name = "school_id"))		//MS03 Added by Ryan Xu
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "schoolType")  // MS04
@JsonSubTypes({
    @JsonSubTypes.Type(value = PublicSchool.class, name = "public"),
    @JsonSubTypes.Type(value = PrivateSchool.class, name = "private")
})  // MS04
// @NamedQueries were necessary for service layer, added by Ryan
@NamedQueries({
    @NamedQuery(
        name = MedicalSchool.ALL_MEDICAL_SCHOOLS_QUERY_NAME,
        query = "SELECT ms FROM MedicalSchool ms"
    ),
    @NamedQuery(
        name = MedicalSchool.SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME,
        query = "SELECT ms FROM MedicalSchool ms WHERE ms.id = :param1"
    ),
    @NamedQuery(
        name = MedicalSchool.IS_DUPLICATE_QUERY_NAME,
        query = "SELECT COUNT(ms) FROM MedicalSchool ms WHERE ms.name = :param1"
    )
})
public abstract class MedicalSchool extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Following 3 lines were added by Ryan
	public static final String ALL_MEDICAL_SCHOOLS_QUERY_NAME = "MedicalSchool.findAll";
	public static final String SPECIFIC_MEDICAL_SCHOOL_QUERY_NAME = "MedicalSchool.findById";
	public static final String IS_DUPLICATE_QUERY_NAME = "MedicalSchool.isDuplicate";

	
	// TODO MS05 - Add the missing annotations.
	@Column(name = "name", nullable = false, unique = true)  // MS05
	private String name;

	// TODO MS06 - Add the 1:M annotation.  What should be the cascade and fetch types?
	@OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)  // MS06
	@JsonIgnore 	//To prevent infinte-loop, Added by Ryan
	private Set<MedicalTraining> medicalTrainings = new HashSet<>();

	// TODO MS07 - Add missing annotation.
	@Column(name = "public", nullable = false, insertable = false, updatable = false)  // MS07，Modified "is_public" to "public" by Ryan
	private boolean isPublic;

	public MedicalSchool() {
		super();
	}

    public MedicalSchool(boolean isPublic) {
        this();
        this.isPublic = isPublic;
    }

	// TODO MS08 - Is an annotation needed here?
	public Set<MedicalTraining> getMedicalTrainings() {  // MS08
		return medicalTrainings;
	}

	public void setMedicalTrainings(Set<MedicalTraining> medicalTrainings) {
		this.medicalTrainings = medicalTrainings;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	//Inherited hashCode/equals is NOT sufficient for this entity class
	
	/**
	 * Very important:  Use getter's for member variables because JPA sometimes needs to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		
		// The database schema for the MEDICAL_SCHOOL table has a UNIQUE constraint for the NAME column,
		// so we should include that in the hash/equals calculations
		
		return prime * result + Objects.hash(getId(), getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof MedicalSchool otherMedicalSchool) {
			// See comment (above) in hashCode():  Compare using only member variables that are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherMedicalSchool.getId()) &&
				Objects.equals(this.getName(), otherMedicalSchool.getName());
		}
		return false;
	}
}

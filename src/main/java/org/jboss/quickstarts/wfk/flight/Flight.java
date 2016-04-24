package org.jboss.quickstarts.wfk.flight;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.util.CompareStrings;
import org.jboss.quickstarts.wfk.util.StringComparisonMode;


/**
 * <p>This is the Domain object. The Flight class represents how flight resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how flights are retrieved from the database (with @NamedQueries), and acceptable values
 * for Flight fields (with @NotNull, @Pattern etc...)<p/>
 *
 * @author Jakub Gawron
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Flight.FIND_ALL, query = "SELECT f FROM Flight f ORDER BY f.number ASC"),
        @NamedQuery(name = Flight.FIND_BY_NUMBER, query = "SELECT f FROM Flight f WHERE f.number = :number")
})
@XmlRootElement
@Table(name = "flight", uniqueConstraints = @UniqueConstraint(columnNames = "number"))
@CompareStrings(propertyNames={"departure", "destination"}, 
				matchMode=StringComparisonMode.NOT_EQUAL_IGNORE_CASE, 
				message="The departure and destination must be different")
public class Flight implements Serializable {
	private static final long serialVersionUID = 1L;
	
    public static final String FIND_ALL = "Flight.findAll";
    public static final String FIND_BY_NUMBER = "Flight.findByNumber";
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
    @NotEmpty
    @NotNull
    @Size(min = 5, max = 5)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Flight number must be a non-empty alpha-numerical string which is 5 characters in length")
	private String number; /* This is the primary key */
	
    @NotEmpty
    @NotNull
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]+$", message = "Flight departure must be a non-empty alphabetical string, which is upper case and 3 characters in length")
    @Column(name = "departure")
	private String departure;

    @NotNull
    @NotEmpty
    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Z]+$", message = "Flight destination must be a non-empty alphabetical string, which is upper case and 3 characters in length")
    @Column(name = "destination")
	private String destination;
    
    @JsonIgnore
    @OneToMany(mappedBy="flight", orphanRemoval=true)
    private Set<Booking> bookings = new HashSet<Booking>();
    
    public Flight() { }
    
    public Flight(Long id, String number, String departure, String destination) {
    	this.id = id;
    	this.number = number;
    	this.departure = departure;
    	this.destination = destination;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNumber() {
    	return number;
    }
    
    public void setNumber(String number) {
    	this.number = number;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Set<Booking> getBookings() {
    	return bookings;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight flight = (Flight) o;
        if (!number.equalsIgnoreCase(flight.getNumber())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }
    
    @Override
    public String toString() {
    	return String.format("Flight[%s]: %s from %s to %s", id, number, departure, destination);
    }
}

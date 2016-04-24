package org.jboss.quickstarts.wfk.customer;

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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.travelagent.TravelAgentBooking;

/**
 * <p>This is a the Domain object. The Customer class represents how customer resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how a customer are retrieved from the database (with @NamedQueries), and acceptable values
 * for Customer fields (with @NotNull, @Pattern etc...)<p/>
 *
 * @author Jakub Gawron
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c ORDER BY c.name ASC"),
        @NamedQuery(name = Customer.FIND_BY_EMAIL, query = "SELECT c FROM Customer c WHERE c.email = :email")
})
@XmlRootElement
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;
	
    public static final String FIND_ALL = "Customer.findAll";
    public static final String FIND_BY_EMAIL = "Customer.findByEmail";
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = "[A-Za-z-' ]+", message = "Please use a name without numbers or specials")
    @Column(name = "name")
	private String name;
	
    @NotNull
    @NotEmpty
    @Email(message = "The email address must be in the format of name@domain.com")
	private String email; /* This is the primary key */
	
    @NotNull
    @Pattern(regexp = "^[0]{1}[0-9]{10}$")
    @Column(name = "phone_number")
	private String phoneNumber;
    
    @JsonIgnore
    @OneToMany(mappedBy="customer", orphanRemoval=true)
    private Set<Booking> bookings = new HashSet<Booking>();
    
    @OneToMany(mappedBy="customer", orphanRemoval=true)
    private Set<TravelAgentBooking> travelAgentBookings = new HashSet<TravelAgentBooking>();
    
    public Customer() {}
    
    public Customer(Long id, String name, String email, String phoneNumber) {
    	this.id = id;
    	this.name = name;
    	this.email = email;
    	this.phoneNumber = phoneNumber;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Set<Booking> getBookings() {
    	return bookings;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        if (!email.equals(customer.getEmail())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
    
    @Override
    public String toString() {
    	return String.format("Customer[%s]: %s", id, name);
    }
}

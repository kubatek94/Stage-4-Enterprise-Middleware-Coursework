package org.jboss.quickstarts.wfk.booking;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.travelagent.TravelAgentBooking;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>This is a the Domain object. The Booking class represents how customer resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how a bookings are retrieved from the database (with @NamedQueries), and acceptable values
 * for Booking fields (with @NotNull, @Pattern etc...)<p/>
 *
 * @author Jakub Gawron
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Booking.FIND_ALL, query = "SELECT b FROM Booking b"),
        @NamedQuery(name = Booking.FIND_BY_FLIGHT, query = "SELECT b FROM Booking b where b.flight.id = :flight_id AND b.bookingDate = :bookingDate"),
        @NamedQuery(name = Booking.FIND_BY_CUSTOMER, query= "SELECT b FROM Booking b where b.customer.id = :customer_id")
})
@XmlRootElement
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "booking_date"}))
public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String FIND_ALL = "Booking.findAll";
	public static final String FIND_BY_FLIGHT = "Booking.findByFlight";
	public static final String FIND_BY_CUSTOMER = "Booking.findByCustomer";

    @ApiModelProperty(hidden=true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    
    @ManyToOne
    @JoinColumn(name="customer_id")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name="flight_id")
	private Flight flight;
	
    @NotNull
    @Temporal(TemporalType.DATE)
    @Future
    @Column(name="booking_date", nullable=false)
	private Date bookingDate;
    
    @OneToOne(mappedBy="flightBooking", optional=true, orphanRemoval=true)
    private TravelAgentBooking travelAgentBooking;
    
    public Booking() {}
    
    public Booking(Customer customer, Flight flight, Date date) {
    	this.customer = customer;
    	this.flight = flight;
    	this.bookingDate = date;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        
        return (
        		bookingDate.equals(booking.getBookingDate()) && 
        		flight.equals(booking.getFlight())
        );
    }

    @Override
    public int hashCode() {
    	int hash = 17;
        hash = hash * 31 + bookingDate.hashCode();
        hash = hash * 31 + flight.hashCode();
    	return hash;
    }
}

package org.jboss.quickstarts.wfk.travelagent;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
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
import javax.persistence.Transient;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.hotel.Hotel;
import org.jboss.quickstarts.wfk.taxi.Taxi;

import io.swagger.annotations.ApiModelProperty;

@Entity
@NamedQueries({
	@NamedQuery(name = TravelAgentBooking.FIND_ALL, query = "SELECT b FROM TravelAgentBooking b"),
	@NamedQuery(name = TravelAgentBooking.FIND_BY_CUSTOMER, query = "SELECT b FROM TravelAgentBooking b WHERE b.customer.id = :customer_id")
})
@XmlRootElement
@Table(name = "travelagentbooking")
public class TravelAgentBooking {
	public static final String FIND_ALL = "TravelAgentBooking.findAll";
	public static final String FIND_BY_CUSTOMER = "TravelAgentBooking.findByCustomer";
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @ApiModelProperty(hidden=true)
	@JsonIgnore
	@Column(name="hotel_booking_id")
	@NotNull
	private Long hotelBookingId;
	
    @ApiModelProperty(hidden=true)
	@JsonIgnore
	@Column(name="taxi_booking_id")
	@NotNull
	private Long taxiBookingId;
    
    @ApiModelProperty(hidden=true)
    @JsonIgnore
    @OneToOne(optional=false, orphanRemoval=true)
    @JoinColumn(name="flight_booking_id", unique=true, nullable=false, updatable=false)
    @NotNull
    private Booking flightBooking;
    
    @ManyToOne
    @JoinColumn(name="customer_id")
    @NotNull
	private Customer customer;
	
	@Embedded
	private Hotel hotel;
	
	@Embedded
	private Taxi taxi;
	
	//flight is extracted and used to make Booking object
	@Transient
	private Flight flight;
    
    @NotNull
    @Temporal(TemporalType.DATE)
    @Future
    @Column(name="booking_date", nullable=false)
	private Date bookingDate;
    
    
    public TravelAgentBooking() {}
    
    public TravelAgentBooking(Customer customer, Flight flight, Hotel hotel, Taxi taxi, Date bookingDate) {
    	this.customer = customer;
    	this.flight = flight;
    	this.hotel = hotel;
    	this.taxi = taxi;
    	this.bookingDate = bookingDate;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getHotelBookingId() {
		return hotelBookingId;
	}

	public void setHotelBookingId(Long hotelBookingId) {
		this.hotelBookingId = hotelBookingId;
	}

	public Long getTaxiBookingId() {
		return taxiBookingId;
	}

	public void setTaxiBookingId(Long taxiBookingId) {
		this.taxiBookingId = taxiBookingId;
	}
	
	public Booking getFlightBooking() {
		return flightBooking;
	}
	
	public void setFlightBooking(Booking flightBooking) {
		this.flightBooking = flightBooking;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public Taxi getTaxi() {
		return taxi;
	}

	public void setTaxi(Taxi taxi) {
		this.taxi = taxi;
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
        if (!(o instanceof TravelAgentBooking)) return false;
        TravelAgentBooking travelBooking = (TravelAgentBooking) o;
        
        return (
        		travelBooking.getBookingDate().equals(bookingDate) &&
        		travelBooking.getHotel().equals(hotel) &&
        		travelBooking.getTaxi().equals(taxi) &&
        		travelBooking.getFlight().equals(flight) &&
        		travelBooking.getCustomer().equals(customer)
        );
    }

    @Override
    public int hashCode() {
    	int hash = 17;
        hash = hash * 31 + hotel.hashCode();
        hash = hash * 31 + taxi.hashCode();
        hash = hash * 31 + bookingDate.hashCode();
        hash = hash * 31 + customer.hashCode();
        hash = hash * 31 + flight.hashCode();
    	return hash;
    }
}

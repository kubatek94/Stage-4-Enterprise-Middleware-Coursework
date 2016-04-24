package org.jboss.quickstarts.wfk.taxi;

import java.util.Date;

import org.jboss.quickstarts.wfk.customer.Customer;


/*
 * 
	{
	  "customer": {
	    "id": 0,
	    "name": "string",
	    "email": "string",
	    "phoneNumber": "string"
	  },
	  "taxi": {
	    "id": 0,
	    "reg": "string",
	    "seats": 0
	  },
	  "bookingDate": "2015-11-24"
	}
 * 
 */

public class TaxiBooking {
	Long id;
	Customer customer;
	Taxi taxi;
	Date bookingDate;
	
	public TaxiBooking() {}
	
	public TaxiBooking(Customer customer, Taxi taxi, Date date) {
		this.customer = customer;
		this.taxi = taxi;
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
	public Taxi getTaxi() {
		return taxi;
	}
	public void setTaxi(Taxi taxi) {
		this.taxi = taxi;
	}
	public Date getBookingDate() {
		return bookingDate;
	}
	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}
	
	public String toString() {
		return String.format("TaxiBooking[%s]: %s for %s, booked on %s", id, taxi, customer, bookingDate);
	}
}

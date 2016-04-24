package org.jboss.quickstarts.wfk.hotel;

import java.util.Date;

import org.jboss.quickstarts.wfk.customer.Customer;

/*
 *
 	{
	  "id": 0,
	  "customer": {
	    "id": 0,
	    "name": "string",
	    "email": "string",
	    "phoneNumber": "string"
	  },
	  "hotel": {
	    "id": 0,
	    "name": "string",
	    "phoneNumber": "string",
	    "postcode": "string"
	  },
	  "date": "2015-11-25"
	}
 *
 */
public class HotelBooking {
	Long id;
	Customer customer;
	Hotel hotel;
	Date date;
	
	public HotelBooking() {}
	
	public HotelBooking(Customer customer, Hotel hotel, Date date) {
		this.customer = customer;
		this.hotel = hotel;
		this.date = date;
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
	public Hotel getHotel() {
		return hotel;
	}
	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String toString() {
		return String.format("HotelBooking[%s]: %s for %s, booked on %s", id, hotel, customer, date);
	}
}

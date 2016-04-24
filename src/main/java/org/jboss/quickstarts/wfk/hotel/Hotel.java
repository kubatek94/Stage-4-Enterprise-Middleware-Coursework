package org.jboss.quickstarts.wfk.hotel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Hotel {
	
	@Column(name="hotel_id")
    private Long id;
	
	@Column(name="hotel_name")
	private String name;
	
	@Column(name="hotel_phonenumber")
	private String phoneNumber;
	
	@Column(name="hotel_postcode")
	private String postcode;
	
	public Hotel() {}
	
	public Hotel(Long id, String name, String phoneNumber, String postcode) {
		this.id = id;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.postcode = postcode;
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
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;
        Hotel hotel = (Hotel) o;
        
        return (
        	hotel.getName().equals(name) && 
        	hotel.getPhoneNumber().equals(phoneNumber) &&
        	hotel.getPostcode().equals(postcode)
        );
    }

    @Override
    public int hashCode() {
    	int hash = 17;
        hash = hash * 31 + name.hashCode();
        hash = hash * 31 + phoneNumber.hashCode();
        hash = hash * 31 + postcode.hashCode();
    	return hash;
    }
	
	public String toString() {
		return String.format("Hotel[%s]: %s", id, name);
	}
}

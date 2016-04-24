package org.jboss.quickstarts.wfk.taxi;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Taxi {
	
	@Column(name="taxi_id")
    private Long id;
	
	@Column(name="taxi_reg")
	private String reg;
	
	@Column(name="taxi_seats")
	private Integer seats;
	
	public Taxi() {}
	
	public Taxi(Long id, String reg, Integer seats) {
		this.id = id;
		this.reg = reg;
		this.seats = seats;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getReg() {
		return reg;
	}
	public void setReg(String reg) {
		this.reg = reg;
	}
	public Integer getSeats() {
		return seats;
	}
	public void setSeats(Integer seats) {
		this.seats = seats;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Taxi)) return false;
        Taxi taxi = (Taxi) o;
        
        return taxi.getReg().equals(reg);
    }

    @Override
    public int hashCode() {
    	return reg.hashCode();
    }
	
	public String toString() {
		return String.format("Taxi[%s]: %s", id, reg);
	}
}

package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.flight.FlightService;

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 */
public class BookingValidator{
	@Inject
	private Validator validator;
	
    @Inject
    private @Named("logger") Logger log;

    @Inject
    private BookingRepository crud;
    
    @Inject
    private CustomerService customerService;
    
    @Inject
    private FlightService flightService;
    
    
    void validate(Booking booking) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
        
        if (customerIdInvalid(booking)) {
        	throw new CustomerInvalidException("Customer with given ID does not exist");
        }
        
        if (flightIdInvalid(booking)) {
        	throw new FlightInvalidException("Flight with given ID does not exist");
        }
        
        if (bookingAlreadyExists(booking)) {
            throw new BookingExistsException("Booking already exists");
        }
    }
    
    /** 
     * <p> checks if the customer already exists in the database, based on customer id </p>
     * */
    boolean customerIdInvalid(Booking booking) {
    	Long customerId = booking.getCustomer().getId();
    	return customerId == null ? true : customerService.findById(customerId) == null; 
    }
    
    /** 
     * <p> checks if the flight already exists in the database, based on flight id </p>
     * */
    boolean flightIdInvalid(Booking booking) {
    	Long flightId = booking.getFlight().getId();
    	return flightId == null ? true : flightService.findById(flightId) == null;
    }
    
    /** 
     * <p> checks if the booking already exists in the database, based on the flight information </p>
     * */
    boolean bookingAlreadyExists(Booking booking) {
    	if(booking == null) {
    		return false;
    	}
    	
    	Booking fromDb = null;
    	
    	Flight flight = booking.getFlight();
    	Date bookingDate = booking.getBookingDate();

    	try{
    		fromDb = crud.findByFlight(flight, bookingDate);
    		
    		//if booking is found and the objects are equal
    		return fromDb != null && fromDb.equals(booking);
    	} catch (NonUniqueResultException e) {
    		log.warning("Duplicate booking found in the database: " + booking);
    		return true;
    	}
    }
}
package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;

public class BookingService {
	@Inject
	BookingRepository crud;
	
	@Inject
	BookingValidator validator;
	
    /**
     * <p>Returns a List of all persisted {@link Booking} objects, sorted alphabetically by booking number.<p/>
     *
     * @return List of Booking objects
     */
    public List<Booking> findAll() {
        return crud.findAll();
    }
    
    /**
     * <p>Returns a single Booking object, specified by a Long id.</p>
     *
     * @param id The id field of the Booking to be returned
     * @return The Booking with the specified id
     */
    public Booking findById(Long id) {
    	return crud.findById(id);
    }
    
    /**
     * <p>Returns a List of all persisted {@link Booking} objects, made by the specified Customer.</p>
     *
     * @return List of Booking objects
     */
    public List<Booking> findByCustomer(Customer customer) {
        return crud.findByCustomer(customer);
    }
    
    /**
     * <p>Returns a single Booking object, specified by a flight and date.</p>
     *
     * <p>If there is more than one Booking with the specified criteria or no bookings found, exception is thrown.<p/>
     *
     * @param flight The flight field of the Booking to be returned
     * @param flightDate The flightDate field of the Booking to be returned
     * @return The Booking with the specified criteria
     */
    public Booking findByFlight(Flight flight, Date flightDate) throws NonUniqueResultException {
        return crud.findByFlight(flight, flightDate);
    }
    
    
    /**
     * <p>Writes the provided Booking object to the application database.<p/>
     *
     * <p>Validates the data in the provided Booking object using a {@link BookingValidator} object.<p/>
     *
     * @param booking The Booking object to be written to the database using a {@link BookingRepository} object
     * @return The Booking object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Booking create(Booking booking) throws ConstraintViolationException, ValidationException, Exception {
    	//validate booking
    	validator.validate(booking);
    	
        // Write the booking to the database.
        return crud.create(booking);
    }
    
    
    /**
     * <p>Deletes the provided Booking object from the application database if found there</p>
     *
     * @param booking The Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    public Booking delete(Booking booking) throws Exception {
        return crud.delete(booking);
    }
}

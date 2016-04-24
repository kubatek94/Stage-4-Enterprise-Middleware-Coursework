package org.jboss.quickstarts.wfk.travelagent;

import java.util.List;
import javax.inject.Inject;
import org.jboss.quickstarts.wfk.customer.Customer;

public class TravelAgentService {
	@Inject
	TravelAgentRepository crud;
	
	/**
	 * <p>requests information about flight so we have meaningful output</p>
	 * @param booking TravelAgentBooking
	 */
	private void loadBooking(TravelAgentBooking booking) {
		booking.setFlight(booking.getFlightBooking().getFlight());
	}
	
    /**
     * <p>Returns a List of all persisted {@link TravelAgentBooking} objects, sorted alphabetically by booking number.<p/>
     *
     * @return List of TravelAgentBooking objects
     */
    public List<TravelAgentBooking> findAll() {
        List<TravelAgentBooking> bookings = crud.findAll();
        for(TravelAgentBooking booking : bookings) {
        	loadBooking(booking);
        }
        return bookings;
    }
    
    /**
     * <p>Returns a single TravelAgentBooking object, specified by a Long id.</p>
     *
     * @param id The id field of the TravelAgentBooking to be returned
     * @return The TravelAgentBooking with the specified id
     */
    public TravelAgentBooking findById(Long id) {
    	TravelAgentBooking booking = crud.findById(id);
    	
    	if(booking != null) {
    		loadBooking(booking);
    	}
    	
    	return booking;
    }
    
    /**
     * <p>Returns a List of all persisted {@link TravelAgentBooking} objects, made by the specified Customer.</p>
     *
     * @return List of TravelAgentBooking objects
     */
    public List<TravelAgentBooking> findByCustomer(Customer customer) {
        List<TravelAgentBooking> bookings = crud.findByCustomer(customer);
        for(TravelAgentBooking booking : bookings) {
        	loadBooking(booking);
        }
        return bookings;
    }
    
    
    /**
     * <p>Writes the provided TravelAgentBooking object to the application database.<p/>

     * @param booking The TravelAgentBooking object to be written to the database using a {@link TravelAgentRepository} object
     * @return The TravelAgentBooking object that has been successfully written to the application database
     * @throws Exception
     */
    public TravelAgentBooking create(TravelAgentBooking booking) throws Exception {
        // Write the booking to the database.
        return crud.create(booking);
    }
    
    /**
     * <p>Deletes the provided TravelAgentBooking object from the application database if found there</p>
     *
     * @param booking The TravelAgentBooking object to be removed from the application database
     * @return The TravelAgentBooking object that has been successfully removed from the application database; or null
     */
    public TravelAgentBooking delete(TravelAgentBooking booking) {
        return crud.delete(booking);
    }
}

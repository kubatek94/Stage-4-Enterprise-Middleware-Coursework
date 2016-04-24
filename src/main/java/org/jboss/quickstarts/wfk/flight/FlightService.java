package org.jboss.quickstarts.wfk.flight;

import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

public class FlightService {
	@Inject
	FlightRepository crud;
	
	@Inject
	FlightValidator validator;
	
    /**
     * <p>Returns a List of all persisted {@link Flight} objects, sorted alphabetically by flight number.<p/>
     *
     * @return List of Flight objects
     */
    public List<Flight> findAll() {
        return crud.findAll();
    }
    
    /**
     * <p>Returns a single Flight object, specified by a Long id.</p>
     *
     * @param id The id field of the Flight to be returned
     * @return The Flight with the specified id
     */
    public Flight findById(Long id) {
    	return crud.findById(id);
    }
    
    
    /**
     * <p>Writes the provided Flight object to the application database.<p/>
     *
     * <p>Validates the data in the provided Flight object using a {@link FlightValidator} object.<p/>
     *
     * @param flight The Flight object to be written to the database using a {@link FlightRepository} object
     * @return The Flight object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Flight create(Flight flight) throws ConstraintViolationException, ValidationException, Exception {
    	//validate flight
    	validator.validate(flight);
    	
        // Write the flight to the database.
        return crud.create(flight);
    }
    
    /**
     * <p>Deletes the provided Flight object from the application database if found there</p>
     *
     * @param flight The Flight object to be removed from the application database
     * @return The Flight object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    public Flight delete(Flight flight) throws Exception {
        return crud.delete(flight);
    }
}

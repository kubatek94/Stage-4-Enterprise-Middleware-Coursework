package org.jboss.quickstarts.wfk.flight;

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

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 */
public class FlightValidator{
	@Inject
	private Validator validator;
	
    @Inject
    private @Named("logger") Logger log;

    @Inject
    private FlightRepository crud;
    
    void validate(Flight flight) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Flight>> violations = validator.validate(flight);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        if (flightAlreadyExists(flight)) {
            throw new FlightExistsException("Flight number is not unique");
        }
    }
    
    /** 
     * <p> checks if the flight already exists in the database, based on the flight number, which should be unique </p>
     * */
    boolean flightAlreadyExists(Flight flight) {
    	if(flight == null) {
    		return false;
    	}
    	
    	Flight fromDb = null;
    	String number = flight.getNumber();

    	try{
    		fromDb = crud.findByNumber(number);
    		//if flight is found and the objects are equal
    		return fromDb != null && fromDb.equals(flight);
    	} catch (NonUniqueResultException e) {
    		log.warning("Duplicate flight number found in the database: " + flight);
    		return true;
    	}
    }
}
package org.jboss.quickstarts.wfk.customer;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NonUniqueResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 */
public class CustomerValidator{
	@Inject
	private Validator validator;
	
    @Inject
    private @Named("logger") Logger log;

    @Inject
    private CustomerRepository crud;
    
    private void commonValidate(Customer customer) throws ConstraintViolationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }
    }
    
    void validate(Customer customer) throws ConstraintViolationException, CustomerExistsException {
    	commonValidate(customer);

        if (emailAlreadyExists(customer)) {
            throw new CustomerExistsException("Customer email is not unique");
        }
    }
    
    void validateExisting(Customer customer) throws ConstraintViolationException, CustomerExistsException {
        commonValidate(customer);
        
        // if customer is already in the database, then check if the new email provided (if changed), doesn't exists in the database yet
        Customer fromDb = crud.findByEmail(customer.getEmail());
        
        //if email is used and it is used by someone else
        if(fromDb != null && (!fromDb.getId().equals(customer.getId()))) {
        	throw new CustomerExistsException("Customer email is not unique");
        }
    }
    
    /** 
     * <p> checks if the customer already exists in the database, based on the email address, which should be unique </p>
     * */
    boolean emailAlreadyExists(Customer customer) {
    	if(customer == null) {
    		return false;
    	}
    	
    	Customer fromDb = null;
    	String email = customer.getEmail();

    	try{
    		fromDb = crud.findByEmail(email);
    		//if customer is found and the ID are equal
    		return fromDb != null && fromDb.equals(customer);
    	} catch (NonUniqueResultException e) {
    		log.warning("Duplicate email found in the database: " + email);
    		return true;
    	}
    }
}
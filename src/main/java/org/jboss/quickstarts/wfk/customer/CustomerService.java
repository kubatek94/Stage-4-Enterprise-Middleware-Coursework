package org.jboss.quickstarts.wfk.customer;

import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

public class CustomerService {
	
	@Inject
	CustomerValidator validator;

	@Inject
	CustomerRepository crud;
	
    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by last name.<p/>
     *
     * @return List of Customer objects
     */
    public List<Customer> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }
    
    /**
     * <p>Returns single Customer object which matches the id.<p/>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Customer findById(Long id) {
        return crud.findById(id);
    }
    
    
    /**
     * <p>Writes the provided Customer object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link CustomerValidator} object.<p/>
     *
     * @param customer The Customer object to be written to the database using a {@link CustomerRepository} object
     * @return The Customer object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Customer create(Customer customer) throws ConstraintViolationException, CustomerExistsException {
        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validate(customer);

        // Write the contact to the database.
        return crud.create(customer);
    }
    
    /**
     * <p>Updates the provided Customer object in the database if found there</p>
     *
     * @param customer The Customer object to be updated in the database
     * @return The Customer object that has been successfully updated in the database; or null
     * @throws Exception
     */
    public Customer update(Customer customer) throws ConstraintViolationException, CustomerExistsException {
        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validateExisting(customer);
        
        return crud.update(customer);
    }
    
    /**
     * <p>Deletes the provided Customer object from the application database if found there</p>
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     */
    public Customer delete(Customer customer) {
        return crud.delete(customer);
    }
}

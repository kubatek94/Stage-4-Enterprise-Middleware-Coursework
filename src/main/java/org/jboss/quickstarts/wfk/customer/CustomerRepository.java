package org.jboss.quickstarts.wfk.customer;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;


public class CustomerRepository {
    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by last name.</p>
     *
     * @return List of Customer objects
     */
    List<Customer> findAllOrderedByName() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();
    }
    
    
    /**
     * <p>Returns a single Customer object, specified by a String email, or null if not found</p>
     *
     * <p>If there is more than one Customer with the specified email exception is thrown.<p/>
     *
     * @param email The email field of the Customer to be returned
     * @return The Customer with the specified email
     */
    Customer findByEmail(String email) throws NonUniqueResultException {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class).setParameter("email", email);
        Customer result = null;
        
        try{
        	result = query.getSingleResult();
        } catch (NoResultException e) {}
        
        return result;
    }
    
    /**
     * <p>Returns a single Customer object, specified by a Long id.</p>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    Customer findById(Long id) {
    	return em.find(Customer.class, id);
    }
    
    
    /**
     * <p>Writes the provided Customer object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link CustomerValidator} object.<p/>
     *
     * @param customer The Customer object to be written to the database using a {@link CustomerRepository} object
     * @return The Customer object that has been successfully written to the application database
     * @throws EntityExistsException
     */
    Customer create(Customer customer) throws EntityExistsException {
        // Write the customer to the database.
        em.persist(customer);

        return customer;
    }
    
    /**
     * <p>Updates the provided Customer object in the database if found there</p>
     *
     * @param customer The Customer object to be updated in the database
     * @return The Customer object that has been successfully updated in the database; or null
     */
    public Customer update(Customer customer) {
    	// Either update the customer or add it if it can't be found.
        em.merge(customer);
        
        return customer;
    }
    
    
    /**
     * <p>Deletes the provided Customer object from the application database if found there</p>
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     */
    Customer delete(Customer customer) {
    	
        if (customer.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(), 
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent 
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it. 
             * 
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database 
             * to reattach it. 
             * 
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument. 
             * You first need an object in a persistent state to be able to delete it.
             * 
             * Therefore we merge first and then we can remove it.
             */
        	try {
        		em.remove(em.merge(customer));
        	} catch (IllegalArgumentException e) {
        		return null;
        	}
        }

        return customer;
    }
}

package org.jboss.quickstarts.wfk.flight;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

public class FlightRepository {
    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link Flight} objects, sorted alphabetically by flight number.</p>
     *
     * @return List of Flight objects
     */
    List<Flight> findAll() {
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_ALL, Flight.class);
        return query.getResultList();
    }
    
    /**
     * <p>Returns a single Flight object, specified by a flight number.</p>
     *
     * <p>If there is more than one Flight with the specified number or flight not found, exception is thrown.<p/>
     *
     * @param number The number field of the Flight to be returned
     * @return The Flight with the specified number
     */
    Flight findByNumber(String number) throws NonUniqueResultException {
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_BY_NUMBER, Flight.class).setParameter("number", number);
        Flight result = null;
        
        try{
        	result = query.getSingleResult();
        } catch (NoResultException e) {}
        
        return result;
    }
    
    /**
     * <p>Returns a single Flight object, specified by a Long id.</p>
     *
     * @param id The id field of the Flight to be returned
     * @return The Flight with the specified id
     */
    Flight findById(Long id) {
    	return em.find(Flight.class, id);
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
    Flight create(Flight flight) throws ConstraintViolationException, ValidationException, Exception {
        // Write the flight to the database.
        em.persist(flight);

        return flight;
    }
    
    /**
     * <p>Deletes the provided Flight object from the application database if found there</p>
     *
     * @param flight The Flight object to be removed from the application database
     * @return The Flight object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Flight delete(Flight flight) throws Exception {
    	
        if (flight.getId() != null) {
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
            em.remove(em.merge(flight));
        }

        return flight;
    }
}

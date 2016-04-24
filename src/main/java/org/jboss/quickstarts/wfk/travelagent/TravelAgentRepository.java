package org.jboss.quickstarts.wfk.travelagent;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.jboss.quickstarts.wfk.customer.Customer;

public class TravelAgentRepository {
    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link TravelAgentBooking} objects</p>
     *
     * @return List of TravelAgentBooking objects
     */
    List<TravelAgentBooking> findAll() {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_ALL, TravelAgentBooking.class);
        return query.getResultList();
    }
    
    /**
     * <p>Returns a single TravelAgentBooking object, specified by a Long id.</p>
     *
     * @param id The id field of the TravelAgentBooking to be returned
     * @return The TravelAgentBooking with the specified id
     */
    TravelAgentBooking findById(Long id) {
    	return em.find(TravelAgentBooking.class, id);
    }
    
    /**
     * <p>Returns a List of all persisted {@link TravelAgentBooking} objects, made by the specified Customer.</p>
     *
     * @return List of TravelAgentBooking objects
     */
    List<TravelAgentBooking> findByCustomer(Customer customer) {
        TypedQuery<TravelAgentBooking> query = em.createNamedQuery(TravelAgentBooking.FIND_BY_CUSTOMER, TravelAgentBooking.class).setParameter("customer_id", customer.getId());
        return query.getResultList();
    }
    
    
    /**
     * <p>Writes the provided TravelAgentBooking object to the application database.<p/>
     *
     * <p>Validates the data in the provided TravelAgentBooking object using a {@link TravelAgentBookingValidator} object.<p/>
     *
     * @param flight The TravelAgentBooking object to be written to the database
     * @return The TravelAgentBooking object that has been successfully written to the application database
     * @throws Exception
     */
    TravelAgentBooking create(TravelAgentBooking booking) throws Exception {
        em.persist(booking);

        return booking;
    }
    
    /**
     * <p>Deletes the provided TravelAgentBooking object from the application database if found there</p>
     *
     * @param flight The TravelAgentBooking object to be removed from the application database
     * @return The TravelAgentBooking object that has been successfully removed from the application database; or null
     */
    TravelAgentBooking delete(TravelAgentBooking booking) {
    	
        if (booking.getId() != null) {
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
        		em.remove(em.merge(booking));
        	} catch (IllegalArgumentException e) {
        		return null;
        	}
        }

        return booking;
    }
}

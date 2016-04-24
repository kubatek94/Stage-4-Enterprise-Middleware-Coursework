package org.jboss.quickstarts.wfk.booking;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.flight.Flight;

public class BookingRepository {
    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link Booking} objects, sorted alphabetically by booking number.</p>
     *
     * @return List of Booking objects
     */
    List<Booking> findAll() {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_ALL, Booking.class);
        return query.getResultList();
    }
    
    /**
     * <p>Returns a single Booking object, specified by a Long id.</p>
     *
     * @param id The id field of the Booking to be returned
     * @return The Booking with the specified id
     */
    Booking findById(Long id) {
    	return em.find(Booking.class, id);
    }
    
    
    /**
     * <p>Returns a List of all persisted {@link Booking} objects, made by the specified Customer.</p>
     *
     * @return List of Booking objects
     */
    List<Booking> findByCustomer(Customer customer) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_CUSTOMER, Booking.class).setParameter("customer_id", customer.getId());
        return query.getResultList();
    }
    
    
    /**
     * <p>Returns a single Booking object, specified by a flight and date.</p>
     *
     * <p>If there is more than one Booking with the specified criteria or no bookings found, exception is thrown.<p/>
     *
     * @param flight The flight field of the Booking to be returned
     * @param bookingDate The bookingDate field of the Booking to be returned
     * @return The Booking with the specified criteria
     */
    Booking findByFlight(Flight flight, Date bookingDate) throws NonUniqueResultException {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_FLIGHT, Booking.class).setParameter("flight_id", flight.getId()).setParameter("bookingDate", bookingDate);
        Booking result = null;
        
        try{
        	result = query.getSingleResult();
        } catch (NoResultException e) {}
        
        return result;
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
    Booking create(Booking booking) throws ConstraintViolationException, ValidationException, Exception {
        // Write the booking to the database.
        em.persist(booking);

        return booking;
    }
    
    /**
     * <p>Deletes the provided Booking object from the application database if found there</p>
     *
     * @param booking The Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Booking delete(Booking booking) throws Exception {
    	
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
            em.remove(em.merge(booking));
        }

        return booking;
    }
}

package org.jboss.quickstarts.wfk.booking;

import static org.junit.Assert.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.flight.FlightRestService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BookingTest {
	
    /**
     * <p>Compiles an Archive using Shrinkwrap, containing those external dependencies necessary to run the tests.</p>
     *
     * <p>Note: This code will be needed at the start of each Arquillian test, but should not need to be edited, except
     * to pass *.class values to .addClasses(...) which are appropriate to the functionality you are trying to test.</p>
     *
     * @return Micro test war to be deployed and executed.
     */
    @Deployment
    public static Archive<?> createTestArchive() {
        //HttpComponents and org.JSON are required by ContactService
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.codehaus.jackson:jackson-core-asl:1.9.9",
                "org.codehaus.jackson:jackson-mapper-asl:1.9.9",
                "org.codehaus.jackson:jackson-jaxrs:1.9.9"
        ).withTransitivity().asFile();

        Archive<?> archive = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.quickstarts.wfk")
                .addAsLibraries(libs)
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("arquillian-ds.xml")
                .addClasses(Booking.class, Flight.class, Customer.class, BookingRestService.class, FlightRestService.class, CustomerRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    @Inject
    BookingRestService bookingRestService;
    
    @Inject
    FlightRestService flightRestService;
    
    @Inject
    CustomerRestService customerRestService;
    
    @Inject
    GuestBookingRestService guestBookingRestService;
    
    
    /**
     * Create valid booking and check that it is persisted
     */
    @Test
    @InSequence(1)
    public void createValidBooking() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Jakub Gawron", "booking1@newcastle.ac.uk", "07706133212")),
    			addFlight(new Flight(null, "BK001", "NRL", "DNE")),
    			sdf.parse("2016-03-01")
    	);
    	
		Response response = bookingRestService.createBooking(booking);
		assertEquals("Unexpected response status", 201, response.getStatus());
		
    	/*try {
    		
    	} catch (RestServiceException e) {
    		Map<String, String> reasons = e.getReasons();
    		Set<String> keys = reasons.keySet();
    		for(String key : keys){
    			System.err.println(key + " => " + reasons.get(key));
    		}
    	}*/
    }
    
    /**
     * Create invalid bookings and check if exception is thrown
     */
    @Test
    @InSequence(2)
    public void createInvalidBookings() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking validBooking = new Booking(
    			addCustomer(new Customer(null, "Jakub Gawron", "booking6@newcastle.ac.uk", "07706133212")),
    			addFlight(new Flight(null, "BK006", "NRL", "DNE")),
    			sdf.parse("2016-03-01")
    	);
    	
    	validBooking = (Booking) bookingRestService.createBooking(validBooking).getEntity();
    	
    	Booking[] bookings = new Booking[] {
			new Booking( //customer id not in database
	    			new Customer(new Long(0), "Jakub Gawron", "booking2@newcastle.ac.uk", "07706133212"),
	    			addFlight(new Flight(null, "BK002", "NRL", "DNE")),
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //customer not in database
					new Customer(null, "Jakub Gawron", "booking2@newcastle.ac.uk", "07706133212"),
	    			addFlight(new Flight(null, "BK003", "NRL", "DNE")),
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //invalid customer
	    			null,
	    			addFlight(new Flight(null, "BK004", "NRL", "DNE")),
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //flight id not in database
	    			addCustomer(new Customer(null, "Jakub Gawron", "booking2@newcastle.ac.uk", "07706133212")),
	    			new Flight(new Long(0), "BK005", "NRL", "DNE"),
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //flight not in database
	    			addCustomer(new Customer(null, "Jakub Gawron", "booking3@newcastle.ac.uk", "07706133212")),
	    			new Flight(null, "BK005", "NRL", "DNE"),
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //invalid flight
	    			addCustomer(new Customer(null, "Jakub Gawron", "booking4@newcastle.ac.uk", "07706133212")),
	    			null,
	    			sdf.parse("2016-03-01")
	    	),
			new Booking( //date in the past
	    			addCustomer(new Customer(null, "Jakub Gawron", "booking5@newcastle.ac.uk", "07706133212")),
	    			addFlight(new Flight(null, "BK005", "NRL", "DNE")),
	    			sdf.parse("2014-03-01")
	    	),
			new Booking( //the same flight + customer + date as existing booking
	    			validBooking.getCustomer(),
	    			validBooking.getFlight(),
	    			validBooking.getBookingDate()
	    	)
    	};
    	
    	for(Booking booking : bookings) {
    		try {
    			bookingRestService.createBooking(booking);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {
    			assertFalse("Internal server error shouldn't really happen", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		}
    	}
    }
    
    /**
     * Create duplicate bookings and check if exception is thrown
     */
    @Test
    @InSequence(3)
    public void createDuplicateBookings() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Jakub Gawron", "booking30@newcastle.ac.uk", "07706133212")),
    			addFlight(new Flight(null, "BK030", "NRL", "DNE")),
    			sdf.parse("2016-03-01")
    	);
    	
		booking = (Booking) bookingRestService.createBooking(booking).getEntity();
		
		booking.setId(null);
    	try {
    		bookingRestService.createBooking(booking);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    }
    
    /**
     * Create booking, then try to delete it
     */
    @Test
    @InSequence(4)
    public void deleteValidBooking() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Booking booking = new Booking(
    			addCustomer(new Customer(null, "Jakub Gawron", "booking40@newcastle.ac.uk", "07706133212")),
    			addFlight(new Flight(null, "BK040", "NRL", "DNE")),
    			sdf.parse("2016-03-01")
    	);
    	
		booking = (Booking) bookingRestService.createBooking(booking).getEntity();
		
		Response response = bookingRestService.deleteBooking(booking.getId());
		assertEquals("Unexpected response status", 204, response.getStatus());
    }
    
    /**
     * Delete booking that doesn't exist and check if exception is thrown
     */
    @Test
    @InSequence(5)
    public void deleteInvalidBooking() {
    	try {
    		bookingRestService.deleteBooking(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	try {
    		bookingRestService.deleteBooking(new Long(0));
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
    
    /**
     * Create a booking, then delete it, and try to add exactly the same one. Should succeed.
     */
    @Test
    @InSequence(6)
    public void cancelAndRemakeBooking() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Customer customer = addCustomer(new Customer(null, "Jakub Gawron", "booking60@newcastle.ac.uk", "07706133212"));
    	Flight flight = addFlight(new Flight(null, "BK060", "NRL", "DNE"));
    	
    	Booking booking = new Booking(
    			customer,
    			flight,
    			sdf.parse("2016-03-01")
    	);
    	
		booking = (Booking) bookingRestService.createBooking(booking).getEntity();
		assertFalse("Booking didn't get ID", booking.getId() == null);
		
		Response response = bookingRestService.deleteBooking(booking.getId());
		assertEquals("Unexpected response status", 204, response.getStatus());
		
		booking.setId(null);
		
		try {
			bookingRestService.createBooking(booking);
			assertFalse("Booking didn't get ID", booking.getId() == null);
		} catch (RestServiceException e) {
			fail("Booking should be created again with no exceptions thrown");
		}
    }
    
    
    /**
     * If you delete a Commodity, are all their associated bookings also deleted?
     * @throws ParseException
     */
    @Test
    @InSequence(7)
    @SuppressWarnings("unchecked")
    public void deleteCommodityCascade() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Customer customer1 = addCustomer(new Customer(null, "Jakub Gawron", "booking70@newcastle.ac.uk", "07706133212"));
    	Customer customer2 = addCustomer(new Customer(null, "Jakub Gawron", "booking71@newcastle.ac.uk", "07706133212"));
    	Flight flight = addFlight(new Flight(null, "BK070", "NRL", "DNE"));
    	
    	Booking[] bookings = new Booking[] {
			new Booking(
	    			customer1,
	    			flight,
	    			sdf.parse("2016-03-01")
	    	),
			new Booking(
	    			customer1,
	    			flight,
	    			sdf.parse("2016-03-02")
	    	),
			new Booking(
	    			customer2,
	    			flight,
	    			sdf.parse("2016-03-03")
	    	)
    	};
    	
    	//create bookings for given flight for different customers
    	for(Booking booking : bookings) {
    		Booking result = (Booking) bookingRestService.createBooking(booking).getEntity();
    		assertFalse("Booking didn't get ID", result.getId() == null);
    	}
    	
    	//check if all bookings for given customer are in database
		List<Booking> customer1Bookings;
		customer1Bookings = (List<Booking>) bookingRestService.getAllBookings(customer1.getId()).getEntity();
    	assertEquals("Customer1 does not have all the bookings", customer1Bookings.size(), 2);
    	
    	
		List<Booking> customer2Bookings;
		customer2Bookings = (List<Booking>) bookingRestService.getAllBookings(customer2.getId()).getEntity();
    	assertEquals("Customer2 does not have all the bookings", customer2Bookings.size(), 1);
    	
    	
    	//delete the commodity and check again, this time, the booking list should be empty
    	flightRestService.deleteFlight(flight.getId());
    	
		customer1Bookings = (List<Booking>) bookingRestService.getAllBookings(customer1.getId()).getEntity();
    	assertEquals("Customer1 still has some bookings", customer1Bookings.size(), 0);
    	
    	
		customer2Bookings = (List<Booking>) bookingRestService.getAllBookings(customer2.getId()).getEntity();
    	assertEquals("Customer2 still has some bookings", customer2Bookings.size(), 0);
    }
    
    /**
     * If you delete a Customer, are all their associated bookings also deleted?
     * @throws ParseException
     */
    @Test
    @InSequence(8)
    @SuppressWarnings("unchecked")
    public void deleteCustomerCascade() throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	Customer customer = addCustomer(new Customer(null, "Jakub Gawron", "booking80@newcastle.ac.uk", "07706133212"));
    	Flight flight1 = addFlight(new Flight(null, "BK080", "NRL", "DNE"));
    	Flight flight2 = addFlight(new Flight(null, "BK081", "NRL", "DNE"));
    	
    	Booking[] bookings = new Booking[] {
			new Booking(
	    			customer,
	    			flight1,
	    			sdf.parse("2016-11-01")
	    	),
			new Booking(
	    			customer,
	    			flight2,
	    			sdf.parse("2016-05-02")
	    	),
			new Booking(
	    			customer,
	    			flight1,
	    			sdf.parse("2016-04-03")
	    	)
    	};
    	
    	//create bookings for given customer for different flights/dates
    	for(Booking booking : bookings) {
    		Booking result = (Booking) bookingRestService.createBooking(booking).getEntity();
    		assertFalse("Booking didn't get ID", result.getId() == null);
    	}
    	
    	//check if all bookings for customer are in database
		List<Booking> customerBookings;
		customerBookings = (List<Booking>) bookingRestService.getAllBookings(customer.getId()).getEntity();
    	assertEquals("Customer does not have all the bookings", customerBookings.size(), 3);

    	//save number of all the bookings
    	customerBookings = (List<Booking>) bookingRestService.getAllBookings(null).getEntity();
    	int oldCustomerBookingsSize = customerBookings.size();
    	
    	//delete the customer and check again, this time, number of all the bookings should decrease by 3
    	customerRestService.deleteCustomer(customer.getId());
    	
    	//check number of all the bookings
    	customerBookings = (List<Booking>) bookingRestService.getAllBookings(null).getEntity();
    	int newCustomerBookingsSize = customerBookings.size();
    	
    	assertEquals("Number of bookings did not decrease by 3", oldCustomerBookingsSize - 3, newCustomerBookingsSize);
    }
    
    /**
     * If you provide a valid Customer but an invalid Booking to your GuestBooking endpoint, is the Customer present in the databse?
     */
    @SuppressWarnings("unchecked")
	@Test
    @InSequence(9)
    public void guestBookingTest() throws ParseException {
    	
    	//check how many customers are stored in the database, before adding invalid guestbooking
    	int nCustomersBefore = ((List<Customer>) customerRestService.getAllCustomers().getEntity()).size();
    	
    	Booking booking = new Booking(
    			new Customer(null, "Jakub Gawron", "booking90@newcastle.ac.uk", "07706133212"),
    			new Flight(null, "BK090", "NRL", "DNE"), //use flight, which does not exist in the database, making the booking invalid
    			new SimpleDateFormat("yyyy-MM-dd").parse("2016-03-01")
    	);
    	
    	GuestBooking guestBooking = new GuestBooking();
    	guestBooking.setBooking(booking);

    	try {
    		guestBookingRestService.createGuestBooking(guestBooking);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertFalse("Internal server error shouldn't really happen", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		
    		//compare the number of customers in the database after creating invalid guestbooking
    		int nCustomersAfter = ((List<Customer>) customerRestService.getAllCustomers().getEntity()).size();
    		assertEquals("Number of customers is not the same", nCustomersBefore, nCustomersAfter);
    	}
    }
    
    private Customer addCustomer(Customer c) {
    	Response response = customerRestService.createCustomer(c);
    	return (Customer) response.getEntity();
    }
    
    private Flight addFlight(Flight f) {
    	Response response = flightRestService.createFlight(f);
    	return (Flight) response.getEntity();
    }
}

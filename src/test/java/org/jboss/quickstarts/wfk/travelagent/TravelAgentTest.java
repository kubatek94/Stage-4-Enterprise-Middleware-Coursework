package org.jboss.quickstarts.wfk.travelagent;

import static org.junit.Assert.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.booking.BookingRestService;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.flight.FlightRestService;
import org.jboss.quickstarts.wfk.hotel.Hotel;
import org.jboss.quickstarts.wfk.hotel.HotelBooking;
import org.jboss.quickstarts.wfk.hotel.HotelBookingService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiBooking;
import org.jboss.quickstarts.wfk.taxi.TaxiBookingService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TravelAgentTest {
	
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
                .addClasses(TravelAgentBooking.class, TravelAgentRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    @Inject
    TravelAgentRestService travelAgent;
    
    @Inject
    CustomerRestService customerService;
    
    @Inject
    FlightRestService flightService;
    
    @Inject
    BookingRestService bookingService;
    
    
    /**
     * Can you create a TravelAgent booking?
     */
    @SuppressWarnings("unchecked")
	@Test
    @InSequence(1)
    public void createTravelAgentBooking() throws ParseException {
    	//Use valid commodities
    	Flight flight = addFlight(new Flight(null, "DW101", "DUB", "WAW"));
    	
    	//these commodities should exist on external services for the tests to pass
    	Taxi taxi = new Taxi(new Long(10002), "GZ45ASD", 4);
    	Hotel hotel = new Hotel(new Long(103), "Hilton", "01234567890", "SE193A");
    	
    	//Create valid customer and add it to database
    	Customer customer = addCustomer(new Customer(null, "Jakub Gawron", "test1@newcastle.ac.uk", "07706133212"));
    	
    	TravelAgentBooking booking = new TravelAgentBooking(customer, flight, hotel, taxi, new SimpleDateFormat("yyyy-MM-dd").parse("2016-03-01"));
    	
    	//create the booking
		Response response = travelAgent.createBooking(booking);
		assertEquals("Unexpected response status", 201, response.getStatus());
		booking = (TravelAgentBooking) response.getEntity();
		
		//check if actual bookings have been made locally and in external services
		
		int nFlightBookings = ((List<Booking>) bookingService.getAllBookings(customer.getId()).getEntity()).size();
		boolean foundHotelBooking = false;
		boolean foundTaxiBooking = false;
		
    	//Create client service instance to make REST requests to upstream service
        HotelBookingService hotelBookingService = ProxyFactory.create(HotelBookingService.class, TravelAgentRestService.HOTEL_SERVICE_URL);
        ClientResponse<List<HotelBooking>> hotelResponse = hotelBookingService.getBookings();
        List<HotelBooking> hotelBookings = hotelResponse.getEntity();
        
        for(HotelBooking hBooking : hotelBookings) {
        	if(hBooking.getCustomer().equals(TravelAgentRestService.HOTEL_CUSTOMER)) {
        		if(hBooking.getHotel().equals(booking.getHotel()) && hBooking.getDate().equals(booking.getBookingDate())) {
            		assertEquals("HotelBooking id is different that set locally", hBooking.getId(), booking.getHotelBookingId());
            		foundHotelBooking = true;
        		}
        	}
        }
        
        TaxiBookingService taxiBookingService = ProxyFactory.create(TaxiBookingService.class, TravelAgentRestService.TAXI_SERVICE_URL);
        ClientResponse<List<TaxiBooking>> taxiResponse = taxiBookingService.getBookings();
        List<TaxiBooking> taxiBookings = taxiResponse.getEntity();
        
        for(TaxiBooking tBooking : taxiBookings) {
        	if(tBooking.getCustomer().equals(TravelAgentRestService.TAXI_CUSTOMER)) {
        		if(tBooking.getTaxi().equals(booking.getTaxi()) && tBooking.getBookingDate().equals(booking.getBookingDate())) {
            		assertEquals("TaxiBooking id is different that set locally", tBooking.getId(), booking.getTaxiBookingId());
            		foundTaxiBooking = true;
        		}
        	}
        }
		
        assertEquals("Flight booking does not exist", 1, nFlightBookings);
        assertEquals("Hotel booking does not exist", true, foundHotelBooking);
        assertEquals("Taxi booking does not exist", true, foundTaxiBooking);
        
        //delete booking so that the test can be repeated without manually removing bookings at external services
        //not really a part of this test, so not checking for any conditions
		travelAgent.deleteBooking(booking.getId());
    }
    
    /**
     * Can you retrieve a list of TravelAgent bookings for a given Customer?
     * Can you delete TravelAgent booking for a given Customer on all services?
     */
    @SuppressWarnings("unchecked")
	@Test
    @InSequence(2)
    public void getTravelAgentBookingsForCustomer() throws ParseException {
    	//Use valid commodities
    	Flight flight = addFlight(new Flight(null, "DW102", "DUB", "WAW"));
    	
    	//these commodities should exist on external services for the tests to pass
    	Taxi taxi = new Taxi(new Long(10002), "GZ45ASD", 4);
    	Hotel hotel = new Hotel(new Long(103), "Hilton", "01234567890", "SE193A");
    	
    	//Create valid customer and add it to database
    	Customer customer = addCustomer(new Customer(null, "Jakub Gawron", "test2@newcastle.ac.uk", "07706133212"));
    	
    	TravelAgentBooking[] bookings = new TravelAgentBooking[] {
    			new TravelAgentBooking(customer, flight, hotel, taxi, new SimpleDateFormat("yyyy-MM-dd").parse("2016-06-01")),
    			new TravelAgentBooking(customer, flight, hotel, taxi, new SimpleDateFormat("yyyy-MM-dd").parse("2016-04-01")),
    			new TravelAgentBooking(customer, flight, hotel, taxi, new SimpleDateFormat("yyyy-MM-dd").parse("2016-05-01"))
    	};

    	//create some valid bookings first
    	for(int i = 0, length = bookings.length; i < length; i++){
    		Response response = travelAgent.createBooking(bookings[i]);
    		assertEquals("Unexpected response status", 201, response.getStatus());
    		bookings[i] = (TravelAgentBooking) response.getEntity();
    	}
    	
    	//now get all bookings for our customer
    	List<TravelAgentBooking> retrievedBookings = (List<TravelAgentBooking>) travelAgent.getBookings(customer.getId()).getEntity();
    	
    	//check if retrieved bookings contain the bookings we created
    	for(TravelAgentBooking booking : bookings) {
    		assertEquals("TravelAgentBooking does not appear to be stored in the database", true, retrievedBookings.contains(booking));
    	}
    	
    	//try to remove the bookings and check if successfully removed from local and external services
    	for(int i = 0, length = bookings.length; i < length; i++){
    		Response response = travelAgent.deleteBooking(bookings[i].getId());
    		assertEquals("Unexpected response status", 204, response.getStatus());
    	}
    	
    	//retrieve number of local bookings for the customer
    	int nFlightBookings = ((List<Booking>) bookingService.getAllBookings(customer.getId()).getEntity()).size();
    	assertEquals("Flight booking still exist", 0, nFlightBookings);
		
    	//Create client service instance to make REST requests to upstream service
        HotelBookingService hotelBookingService = ProxyFactory.create(HotelBookingService.class, TravelAgentRestService.HOTEL_SERVICE_URL);
        ClientResponse<List<HotelBooking>> hotelResponse = hotelBookingService.getBookings();
        List<HotelBooking> hotelBookings = hotelResponse.getEntity();
        
        for(HotelBooking hBooking : hotelBookings) {
        	if(hBooking.getCustomer().equals(TravelAgentRestService.HOTEL_CUSTOMER)) {
        		Hotel h = hBooking.getHotel();
        		Date date = hBooking.getDate();
        		for(TravelAgentBooking booking : bookings) {
        			assertFalse("Still found hotel booking in external bookings", h.equals(booking.getHotel()) && date.equals(booking.getBookingDate()));
        		}
        	}
        }
        
        TaxiBookingService taxiBookingService = ProxyFactory.create(TaxiBookingService.class, TravelAgentRestService.TAXI_SERVICE_URL);
        ClientResponse<List<TaxiBooking>> taxiResponse = taxiBookingService.getBookings();
        List<TaxiBooking> taxiBookings = taxiResponse.getEntity();
        
        for(TaxiBooking tBooking : taxiBookings) {
        	if(tBooking.getCustomer().equals(TravelAgentRestService.TAXI_CUSTOMER)) {
        		Taxi t = tBooking.getTaxi();
        		Date date = tBooking.getBookingDate();
        		for(TravelAgentBooking booking : bookings) {
        			assertFalse("Still found taxi booking in external bookings", t.equals(booking.getTaxi()) && date.equals(booking.getBookingDate()));
        		}
        	}
        }
    }
    
    /**
     * If a booking is invalid for just one of the base commodities,
     * are the bookings made with other remote services successfully removed?
     */
    @SuppressWarnings("unchecked")
	@Test
    @InSequence(3)
    public void cascadingRemoveBookings() throws ParseException {
    	//Create valid customer and add it to database
    	Customer customer = addCustomer(new Customer(null, "Jakub Gawron", "test3@newcastle.ac.uk", "07706133212"));
    	
    	//Create valid flight and add it to database
    	Flight flight = addFlight(new Flight(null, "DW103", "DUB", "WAW"));
    	
    	//one of the three commodities is invalid
    	TravelAgentBooking[] bookings = new TravelAgentBooking[] {
    			new TravelAgentBooking(
    					customer, 
    					new Flight(null, "DW104", "DUB", "WAW"), //flight does not exist
    					new Hotel(new Long(103), "Hilton", "01234567890", "SE193A"), 
    					new Taxi(new Long(10002), "GZ45ASD", 4), 
    					new SimpleDateFormat("yyyy-MM-dd").parse("2016-07-01")
    			),
    			new TravelAgentBooking(
    					customer, 
    					flight,
    					new Hotel(null, "Hilton", "01234567890", "SE193A"), //hotel does not exist
    					new Taxi(new Long(10002), "GZ45ASD", 4), 
    					new SimpleDateFormat("yyyy-MM-dd").parse("2016-08-01")
    			),
    			new TravelAgentBooking(
    					customer, 
    					flight,
    					new Hotel(new Long(103), "Hilton", "01234567890", "SE193A"), 
    					new Taxi(null, "GZ45ASD", 4), //taxi does not exist
    					new SimpleDateFormat("yyyy-MM-dd").parse("2016-09-01")
    			)
    	};

    	//try to create the travelAgentBookings
    	for(int i = 0, length = bookings.length; i < length; i++){
    		try {
    			travelAgent.createBooking(bookings[i]);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {}
    	}
    	
    	//make sure that no bookings have actually been made
    	//retrieve number of local bookings for the customer
    	int nFlightBookings = ((List<Booking>) bookingService.getAllBookings(customer.getId()).getEntity()).size();
    	assertEquals("Flight booking has been made", 0, nFlightBookings);
    	
    	//Create client service instance to make REST requests to upstream service
        HotelBookingService hotelBookingService = ProxyFactory.create(HotelBookingService.class, TravelAgentRestService.HOTEL_SERVICE_URL);
        ClientResponse<List<HotelBooking>> hotelResponse = hotelBookingService.getBookings();
        List<HotelBooking> hotelBookings = hotelResponse.getEntity();
        
        for(HotelBooking hBooking : hotelBookings) {
        	if(hBooking.getCustomer().equals(TravelAgentRestService.HOTEL_CUSTOMER)) {
        		Hotel h = hBooking.getHotel();
        		Date date = hBooking.getDate();
        		for(TravelAgentBooking booking : bookings) {
        			assertFalse("Found hotel booking in external bookings", h.equals(booking.getHotel()) && date.equals(booking.getBookingDate()));
        		}
        	}
        }
        
        TaxiBookingService taxiBookingService = ProxyFactory.create(TaxiBookingService.class, TravelAgentRestService.TAXI_SERVICE_URL);
        ClientResponse<List<TaxiBooking>> taxiResponse = taxiBookingService.getBookings();
        List<TaxiBooking> taxiBookings = taxiResponse.getEntity();
        
        for(TaxiBooking tBooking : taxiBookings) {
        	if(tBooking.getCustomer().equals(TravelAgentRestService.TAXI_CUSTOMER)) {
        		Taxi t = tBooking.getTaxi();
        		Date date = tBooking.getBookingDate();
        		for(TravelAgentBooking booking : bookings) {
        			assertFalse("Found taxi booking in external bookings", t.equals(booking.getTaxi()) && date.equals(booking.getBookingDate()));
        		}
        	}
        }
    }
    
    private Customer addCustomer(Customer c) {
    	Response response = customerService.createCustomer(c);
    	return (Customer) response.getEntity();
    }
    
    private Flight addFlight(Flight f) {
    	Response response = flightService.createFlight(f);
    	return (Flight) response.getEntity();
    }
}

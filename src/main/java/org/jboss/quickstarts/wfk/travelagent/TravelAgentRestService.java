package org.jboss.quickstarts.wfk.travelagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.booking.Booking;
import org.jboss.quickstarts.wfk.booking.BookingExistsException;
import org.jboss.quickstarts.wfk.booking.BookingService;
import org.jboss.quickstarts.wfk.booking.CustomerInvalidException;
import org.jboss.quickstarts.wfk.booking.FlightInvalidException;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.flight.FlightRestService;
import org.jboss.quickstarts.wfk.hotel.Hotel;
import org.jboss.quickstarts.wfk.hotel.HotelBooking;
import org.jboss.quickstarts.wfk.hotel.HotelBookingService;
import org.jboss.quickstarts.wfk.hotel.HotelService;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiBooking;
import org.jboss.quickstarts.wfk.taxi.TaxiBookingService;
import org.jboss.quickstarts.wfk.taxi.TaxiService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.ProxyFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;



@Path("/travelagent")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/travelagent")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TravelAgentRestService {
	//Niamh Gohil
	public static Long HOTEL_CUSTOMER_ID = new Long(5);
	public static final String HOTEL_SERVICE_URL = "http://contacts-120232109.rhcloud.com";
	public static Customer HOTEL_CUSTOMER = null;
	
	//Maciej Sokolowski
	public static Long TAXI_CUSTOMER_ID = new Long(30003);
	public static final String TAXI_SERVICE_URL = "http://contacts-120357332.rhcloud.com";
	public static Customer TAXI_CUSTOMER = null;
	
	@Inject
	BookingService flightBookingService;
	
	@Inject
	FlightRestService flightService;  
	
	@Inject
	TravelAgentService travelAgentService;
	
	@Inject
	CustomerService customerService;
	
	@Resource
	UserTransaction userTransaction;
	
	HotelBookingService hotelBookingService = null;
	TaxiBookingService taxiBookingService = null;
    
	public TravelAgentRestService() {
        //Set customer id for external services, if set in environment
		try {
			//read environment variables
			String hotelCustomer = System.getenv("OPENSHIFT_HOTELCUSTOMER_ID");
			String taxiCustomer = System.getenv("OPENSHIFT_TAXICUSTOMER_ID");
			
			//if they're set, then parse as Long and set relevant IDs
			if(hotelCustomer != null) {
				HOTEL_CUSTOMER_ID = Long.parseLong(hotelCustomer, 10);
			}
			if(taxiCustomer != null) {
				TAXI_CUSTOMER_ID = Long.parseLong(taxiCustomer, 10);
			}
		} catch (SecurityException e) {}
		
		//create Customer objects for external services
		HOTEL_CUSTOMER = new Customer(HOTEL_CUSTOMER_ID, "Jakub Gawron", "j.gawron@newcastle.ac.uk", "01234567858");
		TAXI_CUSTOMER = new Customer(TAXI_CUSTOMER_ID, "Jakub Gawron", "j.gawron@newcastle.ac.uk", "01234567858");
	}
	
	
    @GET
    @Path("/taxis")
    @ApiOperation(value = "Fetch all Taxis", notes = "Returns a JSON array of all stored Taxi objects.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Taxis retrieved successfuly"),
            @ApiResponse(code = 400, message = "Problems with external taxi booking service")
    })
	public Response getTaxis() {
    	//Create client service instance to make REST requests to upstream service
        TaxiService taxiService = ProxyFactory.create(TaxiService.class, TAXI_SERVICE_URL);
        ClientResponse<List<Taxi>> response = taxiService.getTaxis();
        
        try {
        	List<Taxi> taxis = response.getEntity();
        	return Response.ok(taxis).build();
        } catch (ClientResponseFailure e) {
        	throw new RestServiceException("External Taxi resource is currently unavailable", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
	}
    
    @GET
    @Path("/hotels")
    @ApiOperation(value = "Fetch all Hotels", notes = "Returns a JSON array of all stored Hotel objects.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Hotels retrieved successfuly"),
            @ApiResponse(code = 400, message = "Problems with external hotel booking service")
    })
	public Response getHotels() {
    	//Create client service instance to make REST requests to upstream service
        HotelService hotelService = ProxyFactory.create(HotelService.class, HOTEL_SERVICE_URL);
        ClientResponse<List<Hotel>> response = hotelService.getHotels();
        
        try {
        	List<Hotel> hotels = response.getEntity();
        	return Response.ok(hotels).build();
        } catch (ClientResponseFailure e) {
        	throw new RestServiceException("External Hotel resource is currently unavailable", Response.Status.INTERNAL_SERVER_ERROR, e);
        }
	}
    
    @GET
    @Path("/flights")
    @ApiOperation(value = "Fetch all Flights", notes = "Returns a JSON array of all stored Flight objects.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Flights retrieved successfuly")
    })
	public Response getFlights() {
    	return flightService.getAllFlights();
	}
    
    @GET
    @Path("/bookings")
    @ApiOperation(value = "Fetch all TravelAgentBookings", notes = "Returns a JSON array of all stored TravelAgentBooking objects.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Bookings retrieved successfuly"),
            @ApiResponse(code = 404, message = "Specified customer not found")
    })
	public Response getBookings(@ApiParam(value = "id of customer", allowableValues = "range[0, infinity]", required = false)
	@QueryParam("customer") Long customerId) {
    	List<TravelAgentBooking> bookings = new ArrayList<TravelAgentBooking>();
    	
    	if(customerId != null) {
    		Customer customer = customerService.findById(customerId);
    		if(customer != null) {
    			bookings = travelAgentService.findByCustomer(customer);
    		} else {
    			throw new RestServiceException("Customer not found!", Response.Status.NOT_FOUND);
    		}
    	} else {
    		bookings = travelAgentService.findAll();
    	}
    	
		return Response.ok(bookings).build();
	}
    
    @GET
    @Path("/bookings/{id:[0-9]+}")
    @ApiOperation(value = "Fetch TravelAgentBooking by id", notes = "Returns a TravelAgentBooking object with specified id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message ="TravelAgentBooking found"),
            @ApiResponse(code = 404, message = "TravelAgentBooking with id not found")
    })
	public Response getBookingsById(
            @ApiParam(value = "Id of TravelAgentBooking to be fetched", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {
    	
    	TravelAgentBooking booking;
    	
    	if(id == null || (booking = travelAgentService.findById(id)) == null) {
    		throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
    	}
    	
		return Response.ok(booking).build();
	}
    
    @DELETE
    @Path("/bookings/{id:[0-9]+}")
    @ApiOperation(value = "Delete TravelAgentBooking by id", notes = "Removes a TravelAgentBooking object with specified id from database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "TravelAgentBooking successfuly deleted"),
            @ApiResponse(code = 400, message = "Could not delete TravelAgentBooking, either local or remote issue"),
            @ApiResponse(code = 404, message = "TravelAgentBooking with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteBooking(
    		@ApiParam(value = "Id of TravelAgentBooking to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {
    	
    	Response.ResponseBuilder builder;
    	
    	//if local booking exists
    	TravelAgentBooking travelAgentBooking = null;
    	if(id != null && (travelAgentBooking = travelAgentService.findById(id)) != null) {
    		try {
	    		try {
	    			userTransaction.begin();
	    			
	    			//try to remove local bookings first
	    			//if this fails, we will rollback transaction and remote bookings will still be intact
	    			if(travelAgentService.delete(travelAgentBooking) == null) {
	    				throw new RestServiceException("Flight booking could not be deleted");
	    			}
	    			
	    			//next try to remove remote bookings
	    			//if this fails, we will recover local transaction
	    			hotelBookingService = ProxyFactory.create(HotelBookingService.class, HOTEL_SERVICE_URL);
	    			taxiBookingService = ProxyFactory.create(TaxiBookingService.class, TAXI_SERVICE_URL);
	    			
	    			deleteExternalBookings(travelAgentBooking.getHotelBookingId(), travelAgentBooking.getTaxiBookingId());
	    			
	    			builder = Response.noContent();
	    			
	    			userTransaction.commit();
	    		} catch (RestServiceException e) {
	    			
	    			//rollback and rethrow the exception
					userTransaction.rollback();
	    			throw e;
	    			
	    		} catch (Exception e) { //this will catch any exceptions related to userTransaction.begin() and userTransaction.commit()
	    			userTransaction.rollback();
					throw new RestServiceException(e);
				}
    		} catch (SystemException e) { //this will catch exceptions related to userTransaction.rollback()
    			throw new RestServiceException(e);
    		}
    	} else {
    		throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
    	}
    	
    	return builder.build();
    }
    
    @POST
    @Path("/bookings")
    @ApiOperation(value = "Add a new TravelAgentBooking to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "TravelAgentBooking created successfully."),
            @ApiResponse(code = 400, message = "Invalid information supplied in request body"),
            @ApiResponse(code = 409, message = "Resource conflicts with another existing resource"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createBooking(@ApiParam(value = "JSON representation of TravelAgentBooking object to be added to the database", required = true) 
    	TravelAgentBooking booking) {
    	
    	Response.ResponseBuilder builder;
    	
    	HotelBooking hotelBooking = null;
    	TaxiBooking taxiBooking = null;
    	
    	if(booking == null || booking.getFlight() == null || booking.getHotel() == null || booking.getTaxi() == null) {
    		throw new RestServiceException("Invalid/incomplete TravelAgentBooking supplied in request body", Response.Status.BAD_REQUEST);
    	}
    	 
    	
    	try {
        	try {
    			userTransaction.begin();
    			
    			//try to create local flight booking first
    			Booking flightBooking = new Booking(booking.getCustomer(), booking.getFlight(), booking.getBookingDate());
    			flightBooking = flightBookingService.create(flightBooking);
    			
    			//now try to make hotel booking at external service
    			hotelBooking = new HotelBooking(HOTEL_CUSTOMER, booking.getHotel(), booking.getBookingDate());
    			hotelBookingService = ProxyFactory.create(HotelBookingService.class, HOTEL_SERVICE_URL);
    			ClientResponse<HotelBooking> hotelBookingResponse = hotelBookingService.createBooking(hotelBooking);
    			
    			//check for exceptions based on the response code
    			//this will throw corresponding exception, based on the status code
    			parseResponse(hotelBookingResponse.getResponseStatus(), "Hotel", true);
    			
    			//no exceptions thrown yet, so extract an entity
    			hotelBooking = hotelBookingResponse.getEntity();
    			
    			//finally try to make taxi booking at external service
    			taxiBooking = new TaxiBooking(TAXI_CUSTOMER, booking.getTaxi(), booking.getBookingDate());
    			taxiBookingService = ProxyFactory.create(TaxiBookingService.class, TAXI_SERVICE_URL);
    			ClientResponse<TaxiBooking> taxiBookingResponse = taxiBookingService.createBooking(taxiBooking);
    			
    			//check for exceptions based on the response code
    			//this will throw corresponding exception, based on the status code
    			parseResponse(taxiBookingResponse.getResponseStatus(), "Taxi", true);
    			
    			//no exceptions thrown yet, so extract an entity
    			taxiBooking = taxiBookingResponse.getEntity();
    			
    			//if it seems like all the commodities have been successfully booked, as they all have id set
    			if(flightBooking != null && hotelBooking != null && taxiBooking != null &&
    				flightBooking.getId() != null && hotelBooking.getId() != null && taxiBooking.getId() != null) {
    			
    				//just to make sure that the copy cached in the database is exactly the same as the one returned from external provider
    				booking.setHotel(hotelBooking.getHotel());
    				booking.setTaxi(taxiBooking.getTaxi());
					
					//just save those ids in our own database
					booking.setFlightBooking(flightBooking);
					booking.setHotelBookingId(hotelBooking.getId());
					booking.setTaxiBookingId(taxiBooking.getId());
					
					//add travelagentbooking to database
					booking = travelAgentService.create(booking);
					builder = Response.status(Response.Status.CREATED).entity(booking);
					
					//commit transaction
					userTransaction.commit();
    			} else {
    				throw new RestServiceException("Unexpected error occured");
    			}
        	} catch (CustomerInvalidException | FlightInvalidException e) { //customer or flight id are not in the database, so can't make booking for them
        		
        		// No external bookings shound be made yet, as this exception can happen for internal validations
        		userTransaction.rollback();
        		throw new RestServiceException(e.getMessage(), Response.Status.BAD_REQUEST, e);
        		
        	} catch (BookingExistsException e) {
        		
        		// No external bookings shound be made yet, as this exception can happen only for internal validations
        		userTransaction.rollback();
        		throw new RestServiceException(e.getMessage(), Response.Status.CONFLICT, e);
        		
        	} catch (ConstraintViolationException e) {
        		// No external bookings shound be made yet, as this exception can happen only for internal validations
        		
                //Handle bean validation issues
                Map<String, String> responseObj = new HashMap<>();
                for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                    responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
                }
                
                //roll back transaction and return with exception
                userTransaction.rollback();
                throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, e);
                
        	} catch (ClientResponseFailure e) {
        		//delete other booked services here
        		try {
        			deleteExternalBookings(hotelBooking == null ? null : hotelBooking.getId(), taxiBooking == null ? null : taxiBooking.getId());
        			userTransaction.rollback();
        		} catch (RestServiceException re) { 
            		//roll back transaction and rethrow exception
        			userTransaction.rollback();
            		throw re;
        		}
        		
        		throw new RestServiceException(e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR, e);
        	} catch (RestServiceException e) { //this is the exception thrown by parseResponse method, can just be rethrown as it already has the response status and message set
        		
        		//delete other booked services here
        		try {
        			deleteExternalBookings(hotelBooking == null ? null : hotelBooking.getId(), taxiBooking == null ? null : taxiBooking.getId());
        			userTransaction.rollback();
        		} catch (RestServiceException re) { 
            		//roll back transaction and rethrow exception
        			userTransaction.rollback();
            		throw re;
        		}
        		
        		throw e;
        		
        	} catch (Exception e) { //this will catch any exceptions related to userTransaction.begin() and userTransaction.commit()
        		
        		//delete other booked services here
        		try {
        			deleteExternalBookings(hotelBooking == null ? null : hotelBooking.getId(), taxiBooking == null ? null : taxiBooking.getId());
        			userTransaction.rollback();
        		} catch (RestServiceException re) { 
            		//roll back transaction and rethrow exception
        			userTransaction.rollback();
            		throw re;
        		}
        		
        		//roll back transaction and return with exception
                userTransaction.rollback();
        		throw new RestServiceException(e);
			}
    	} catch (SystemException e) {
    		throw new RestServiceException(e);
    	}
    	
    	return builder.build();
    }
    
    private void parseResponse(Response.Status status, String type, boolean create) throws RestServiceException {
		//check for exceptions based on the response code
		switch(status){
		case CREATED: //returned when booking created with success
		case NO_CONTENT: //returned when booking deleted with success
		case OK:
			return;
			
		case BAD_REQUEST:
			throw new RestServiceException(
				(create ? ("Invalid " + type + "Booking supplied in request body") : ("Invalid " + type + "Booking ID specified")),
				Response.Status.BAD_REQUEST
			);
			
		case CONFLICT:
			throw new RestServiceException(type + "Booking supplied in request body conflicts with an existing " + type + "Booking", Response.Status.CONFLICT);
			
		case NOT_FOUND:
			throw new RestServiceException(type + "Booking with specified ID not found", Response.Status.NOT_FOUND);
			
		case INTERNAL_SERVER_ERROR:
			throw new RestServiceException("An unexpected error occurred whilst processing the request for " + type + "Booking", Response.Status.INTERNAL_SERVER_ERROR);
			
		default:
			throw new RestServiceException(
				(create ? ("Unexpected exception while booking " + type + " with code: " + status) : ("Unexpected exception while removing " + type + "Booking with code: " + status)),
				Response.Status.INTERNAL_SERVER_ERROR
			);	
		}
    }
    
    private void deleteExternalBookings(Long hotelBookingId, Long taxiBookingId) throws RestServiceException {
    	if(hotelBookingId != null && hotelBookingService != null) {
    		ClientResponse<Void> response = hotelBookingService.deleteBookingById(hotelBookingId);
    		parseResponse(response.getResponseStatus(), "Hotel", false);
    	}
    	
    	if(taxiBookingId != null && taxiBookingService != null) {
    		ClientResponse<Void> response = taxiBookingService.deleteBookingById(taxiBookingId);
    		parseResponse(response.getResponseStatus(), "Hotel", false);
    	}
    }
}

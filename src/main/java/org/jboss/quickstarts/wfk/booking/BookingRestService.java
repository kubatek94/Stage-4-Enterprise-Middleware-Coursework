package org.jboss.quickstarts.wfk.booking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
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

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/bookings")
@Stateless
public class BookingRestService {
	@Inject
	BookingService service;
	
	@Inject
	CustomerService customerService;
	
    @Inject
    private @Named("logger") Logger log;
    
    
    /**
     * <p>Return all the Bookings</p>
     *
     * <p>The url may optionally include query parameter specifying a customer id</p>
     *
     * <p>Examples: <pre>GET api/bookings?customerid=123</pre></p>
     *
     * @return A Response containing a list of Bookings
     */
    @GET
    @ApiOperation(value = "Fetch all Bookings", notes = "Returns a JSON array of all stored Booking objects.")
	public Response getAllBookings(
			@ApiParam(value = "id of customer", allowableValues = "range[0, infinity]", required = false)
			@QueryParam("customerId") Long customerId) {
    	List<Booking> bookings;
    	
    	if(customerId == null) {
    		bookings = service.findAll();
    	} else {
        	Customer customer = customerService.findById(customerId);
        	
        	if(customer != null) {
        		bookings = service.findByCustomer(customer);
        	} else {
        		bookings = new ArrayList<Booking>(0);
        	}
    	}
		
		return Response.ok(bookings).build();
	}
    
    @POST
    @ApiOperation(value = "Add a new Booking to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Booking created successfully."),
            @ApiResponse(code = 400, message = "Invalid Booking supplied in request body"),
            @ApiResponse(code = 409, message = "Booking supplied in request body conflicts with an existing Booking"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
	public Response createBooking(
		@ApiParam(value = "JSON representation of Booking object to be added to the database", required = true) 
		Booking booking) {
    	
        if (booking == null || booking.getCustomer() == null || booking.getFlight() == null || booking.getBookingDate() == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            // Go add the new Booking.
            service.create(booking);
            

            // Create a "Resource Created" 201 Response and pass the booking back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(booking);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (CustomerInvalidException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("customer", "That customer id does not exist, please use existing customer id");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
        } catch (FlightInvalidException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("flight", "That flight id does not exist, please use existing flight id");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
        } catch (BookingExistsException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("number", "That flight is already used, please choose a different date/flight id combination");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
        	log.warning("Caught internal exception when adding a booking: " + e.getMessage());
            throw new RestServiceException(e);
		}
    	
        return builder.build();
	}
    
    
    /**
     * <p>Deletes a booking using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Booking to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Booking from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Booking deleted successfuly"),
            @ApiResponse(code = 404, message = "Booking with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteBooking(
            @ApiParam(value = "Id of Booking to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {

        Response.ResponseBuilder builder;

        Booking booking;
        
        if (id == null || (booking = service.findById(id)) == null) {
            // Verify that the booking exists. Return 404, if not present.
            throw new RestServiceException("No Booking with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(booking);
            builder = Response.noContent();
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }

}

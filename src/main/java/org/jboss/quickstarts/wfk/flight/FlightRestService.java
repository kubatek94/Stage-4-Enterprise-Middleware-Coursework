package org.jboss.quickstarts.wfk.flight;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.quickstarts.wfk.util.RestServiceException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Path("/flights")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/flights")
@Stateless
public class FlightRestService {
	@Inject
	FlightService service;
	
    @Inject
    private @Named("logger") Logger log;
    
	
    @GET
    @ApiOperation(value = "Fetch all flights", notes = "Returns a JSON array of all stored Flight objects.")
	public Response getAllFlights() {
		List<Flight> flights = service.findAll();
		return Response.ok(flights).build();
	}
    
    @POST
    @ApiOperation(value = "Add a new Flight to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Flight created successfully."),
            @ApiResponse(code = 400, message = "Invalid Flight supplied in request body"),
            @ApiResponse(code = 409, message = "Flight supplied in request body conflicts with an existing Flight"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
	public Response createFlight(
		@ApiParam(value = "JSON representation of Flight object to be added to the database", required = true) 
		Flight flight) {
    	
        if (flight == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            // Go add the new Flight.
            service.create(flight);

            // Create a "Resource Created" 201 Response and pass the flight back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(flight);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
        } catch (FlightExistsException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("number", "That flight number is already used, please use a unique flight number");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
        	log.warning("Caught internal exception when adding a flight: " + e.getMessage());
            throw new RestServiceException(e);
        }
    	
        return builder.build();
	}
    
    
    /**
     * <p>Deletes a flight using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Flight to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Flight from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Flight deleted successfuly"),
            @ApiResponse(code = 400, message = "Invalid Flight id supplied"),
            @ApiResponse(code = 404, message = "Flight with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteFlight(
            @ApiParam(value = "Id of Flight to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {

        Response.ResponseBuilder builder;

        Flight flight;
        
        if (id == null || (flight = service.findById(id)) == null) {
            // Verify that the flight exists. Return 404, if not present.
            throw new RestServiceException("No Flight with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }

        try {
            service.delete(flight);
            builder = Response.noContent();
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }

}

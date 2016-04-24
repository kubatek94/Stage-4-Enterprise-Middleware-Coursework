package org.jboss.quickstarts.wfk.customer;

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
import javax.ws.rs.PUT;
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


@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/customers")
@Stateless
public class CustomerRestService {
	@Inject
	CustomerService service;
	
    @Inject
    private @Named("logger") Logger log;
    
	
    @GET
    @ApiOperation(value = "Fetch all customers", notes = "Returns a JSON array of all stored Customer objects.")
	public Response getAllCustomers() {
		List<Customer> customers = service.findAllOrderedByName();
		return Response.ok(customers).build();
	}
    
    @GET
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Fetch information about customer with id", notes = "Returns a Customer object")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Customer found"),
            @ApiResponse(code = 400, message = "Invalid Customer id supplied"),
            @ApiResponse(code = 404, message = "Customer with id not found")
    })
	public Response getCustomer(@PathParam("id") Long id) {
    	
    	Customer customer = null;
    	
    	if(id == null || (customer = service.findById(id)) == null) {
    		throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
    	}
    	
		return Response.ok(customer).build();
	}
    
    
    @POST
    @ApiOperation(value = "Add a new Customer to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Customer created successfully."),
            @ApiResponse(code = 400, message = "Invalid Customer supplied in request body"),
            @ApiResponse(code = 409, message = "Customer supplied in request body conflicts with an existing Customer"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
	public Response createCustomer(
		@ApiParam(value = "JSON representation of Customer object to be added to the database", required = true) 
		Customer customer) {
    	
        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder;

        try {
            // Go add the new Customer.
            service.create(customer);

            // Create a "Resource Created" 201 Response and pass the customer back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(customer);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (CustomerExistsException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
        	log.warning("Caught internal exception when adding a customer: " + e.getMessage());
        	e.printStackTrace();
            throw new RestServiceException(e);
        }
    	
        return builder.build();
	}
    
    
    @PUT
    @Path("/{id:[0-9]+}")
    @ApiOperation(value= "Update Customer information in the database")
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "Customer updated successfuly"),
    		@ApiResponse(code = 400, message = "Invalid Customer id supplied"),
    		@ApiResponse(code = 404, message = "Customer with that id not found"),
    		@ApiResponse(code = 409, message = "Customer email is not unique")
    })
    public Response updateCustomer(
    	@ApiParam(value = "Id of Customer to be updated", allowableValues = "range[0, infinity]", required = true)
    	@PathParam("id") Long id,
    	@ApiParam(value = "JSON representation of Customer object to be updated in the database", required = true)
    	Customer updatedCustomer) {
    	
    	if(updatedCustomer == null) {
    		throw new RestServiceException("Invalid Customer supplied in request body", Response.Status.BAD_REQUEST);
    	}
    	
    	if (updatedCustomer.getId() != null && !updatedCustomer.getId().equals(id)) {
            // The client attempted to update the read-only Id. This is not permitted.
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Customer ID in the request body must match that of the Customer being updated");
            throw new RestServiceException("Customer details supplied in request body conflict with another Customer",
                    responseObj, Response.Status.NOT_FOUND);
    	}
    	
    	if(service.findById(id) == null) {
    		// Verify that the customer exists. Return 404, if not present.
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
    	}
        
        Response.ResponseBuilder builder;

        try {
            //Update the customer
            service.update(updatedCustomer);
            builder = Response.ok(updatedCustomer);

        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (CustomerExistsException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "That email is already used, please use a unique email");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
        	log.warning("Caught internal exception when updating a customer: " + e.getMessage());
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }
    
    
    /**
     * <p>Deletes a customer using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
     *
     * @param id The Long parameter value provided as the id of the Customer to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @ApiOperation(value = "Delete a Customer from the database")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Customer deleted successfuly"),
            @ApiResponse(code = 404, message = "Customer with id not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response deleteCustomer(
            @ApiParam(value = "Id of Customer to be deleted", allowableValues = "range[0, infinity]", required = true)
            @PathParam("id")
            Long id) {

        Response.ResponseBuilder builder;

    	Customer customer = null;
    	
    	if(id == null || (customer = service.findById(id)) == null) {
    		throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
    	}

        try {
            service.delete(customer);
            builder = Response.noContent();
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }
        
        return builder.build();
    }

}

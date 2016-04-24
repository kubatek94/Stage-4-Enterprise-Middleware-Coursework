package org.jboss.quickstarts.wfk.hotel;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

@Path("/api/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HotelBookingService {
	@GET
	ClientResponse<List<HotelBooking>> getBookings();
	
    @GET
    @Path("/{id:[0-9]+}")
    ClientResponse<HotelBooking> getBookingById(@PathParam("id") Long id);
    
    @POST
    ClientResponse<HotelBooking> createBooking(HotelBooking hotelBooking);
    
    @DELETE
    @Path("/{id:[0-9]+}")
    ClientResponse<Void> deleteBookingById(@PathParam("id") Long id);
}
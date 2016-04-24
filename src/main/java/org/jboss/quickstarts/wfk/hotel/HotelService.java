package org.jboss.quickstarts.wfk.hotel;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

@Path("/api/hotels")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HotelService {
	@GET
	ClientResponse<List<Hotel>> getHotels();
	
    @GET
    @Path("/{id:[0-9]+}")
    ClientResponse<Hotel> getHotelById(@PathParam("id") Long id);
    
    @POST
    ClientResponse<HotelBooking> create(HotelBooking hotelBooking);
}

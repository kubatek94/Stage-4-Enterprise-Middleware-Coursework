package org.jboss.quickstarts.wfk.taxi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

@Path("/api/taxis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TaxiService {
	@GET
	ClientResponse<List<Taxi>> getTaxis();
	
    @GET
    @Path("/{id:[0-9]+}")
    ClientResponse<Taxi> getTaxiById(@PathParam("id") Long id);
}

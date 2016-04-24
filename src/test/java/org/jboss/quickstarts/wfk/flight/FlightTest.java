package org.jboss.quickstarts.wfk.flight;

import static org.junit.Assert.*;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.flight.Flight;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class FlightTest {
	
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
                .addClasses(Flight.class, FlightRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    
    @Inject
    FlightRestService flightRestService;
    
    
    /**
     * Create valid flight and check that it is persisted
     */
    @Test
    @InSequence(1)
    public void createValidFlight() {
    	Flight flight = new Flight(null, "DW100", "DUB", "WAW");
    	Response response = flightRestService.createFlight(flight);
    	assertEquals("Unexpected response status", 201, response.getStatus());
    }
    
    /**
     * Create invalid flights and check if exception is thrown
     */
    @Test
    @InSequence(2)
    public void createInvalidFlights() {
    	Flight[] flights = new Flight[] {
    		new Flight(null, "", "DUB", "WAW"), //test number length
    		new Flight(null, "ABC!2", "DUB", "WAW"), //test number regex
    		new Flight(null, "DW100", "DUBZZ", "WAW"), //test departure length
    		new Flight(null, "DW100", "d12", "WAW"), //test departure regex
    		new Flight(null, "DW100", "DUB", "WAWZZ"), //test destination length
    		new Flight(null, "DW100", "DUB", "w#$"), //test destination regex
    		new Flight(null, "DW100", "DUB", "DUB") //test destination != departure
    	};
    	
    	for(Flight flight : flights) {
    		try {
    			flightRestService.createFlight(flight);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {
    			assertFalse("Internal server error shouldn't really happen", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		}
    	}
    }
    
    /**
     * Create duplicate flights and check if exception is thrown
     */
    @Test
    @InSequence(3)
    public void createDuplicateFlights() {
    	Flight flightCopy = new Flight(null, "DW100", "DUB", "WAW");
    	
    	try {
    		flightRestService.createFlight(flightCopy);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    }
    
    /**
     * Create flight, then try to delete it
     */
    @Test
    @InSequence(4)
    public void deleteValidFlight() {
    	Flight flight = new Flight(null, "DW104", "DUB", "WAW");
    	Response response;
    	
    	response = flightRestService.createFlight(flight);
    	flight = (Flight) response.getEntity();
    	
    	response = flightRestService.deleteFlight(flight.getId());
    	assertEquals("Unexpected response status", 204, response.getStatus());
    }
    
    /**
     * Delete flight that doesn't exist and check if exception is thrown
     */
    @Test
    @InSequence(5)
    public void deleteInvalidFlight() {
    	
    	try {
    		flightRestService.deleteFlight(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	try {
    		flightRestService.deleteFlight(new Long(0));
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
}

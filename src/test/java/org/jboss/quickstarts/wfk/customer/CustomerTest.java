package org.jboss.quickstarts.wfk.customer;

import static org.junit.Assert.*;

import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRestService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CustomerTest {
	
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
                .addClasses(Customer.class, CustomerRestService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }
    
    
    @Inject
    CustomerRestService customerRestService;
    
    /**
     * Create valid customer and check that it is persisted
     */
    @Test
    @InSequence(1)
    public void createValidCustomer() {
    	Customer customer = new Customer(null, "Jakub Gawron", "j.gawron@newcastle.ac.uk", "07706133212");
    	
    	Response response = customerRestService.createCustomer(customer);
    	assertEquals("Unexpected response status", 201, response.getStatus());
    }
    
    /**
     * Create invalid customers and check if exception is thrown
     */
    @Test
    @InSequence(2)
    public void createInvalidCustomers() {
    	Customer[] customers = new Customer[]{
    		//test name regex and length
    		new Customer(null, "Jakub --./sdGawron..................................asddddddddddddddddddddddddddddddddddddddaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "j.gawron@newcastle.ac.uk", "07706133212"),
    		
    		new Customer(null, "Jakub Gawron", "j.gawron(at)newcastle.ac.uk", "07706133212"), //test email regex
    		new Customer(null, "Jakub Gawron", "j.gawron@newcastle.ac.uk", "+37706133212") //test phone number format
    	};
    	
    	for(Customer customer : customers) {
    		try {
    			customerRestService.createCustomer(customer);
    			fail("Expected a RestServiceException to be thrown");
    		} catch (RestServiceException e) {
    			assertFalse("Internal server error shouldn't really happen", e.getStatus() == Response.Status.INTERNAL_SERVER_ERROR);
    		}
    	}
    }
    
    /**
     * Create duplicate customers and check if exception is thrown
     */
    @Test
    @InSequence(3)
    public void createDuplicateCustomers() {
    	Customer customerCopy = new Customer(null, "Jakub Gawron", "j.gawron@newcastle.ac.uk", "07706133212");
    	
    	try {
    		customerRestService.createCustomer(customerCopy);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    }
    
    /**
     * Persist customer first and check that it can be retrieved by id
     */
    @Test
    @InSequence(4)
    public void getCustomerById() {
    	Customer customer = new Customer(null, "Jakub Gawron", "test4@newcastle.ac.uk", "07706133212");
    	
    	try {
    		Response response = customerRestService.createCustomer(customer);
    		Customer persisted = (Customer) response.getEntity();
    		
    		assertFalse("Customer didn't get ID", persisted.getId() == null);
    		
    		Response getById = customerRestService.getCustomer(persisted.getId());
    		Customer returned = (Customer) getById.getEntity();
    		
    		assertTrue("Customer returned is not what was created", returned.equals(persisted));
    	} catch (RestServiceException e) {
    		fail("Unexpected RestServiceException");
    	}
    }
    
    /**
     * Pass invalid id and check if exception is thrown
     */
    @Test
    @InSequence(5)
    public void getCustomerByInvalidId() {
    	try {
    		customerRestService.getCustomer(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
    
    /**
     * Update customer information, providing valid information
     */
    @Test
    @InSequence(6)
    public void updateValidCustomer() {
    	Customer customer = new Customer(null, "Jakub Gawron", "test6@newcastle.ac.uk", "07706133212");
    	Response response;
    	
    	response = customerRestService.createCustomer(customer);
    	customer = (Customer) response.getEntity();
    	
    	assertFalse("Customer did not get ID", customer.getId() == null);
    	
    	customer.setName("Test Customer");
    	response = customerRestService.updateCustomer(customer.getId(), customer);
    	
    	customer = (Customer) response.getEntity();
    	assertTrue("Customer information was not updated", customer.getName().equals("Test Customer"));
    }
    
    /**
     * Update customer information, providing invalid information and check if exception is thrown
     */
    @Test
    @InSequence(7)
    public void updateInvalidCustomer() {
    	Customer customer = new Customer(null, "Jakub Gawron", "test7@newcastle.ac.uk", "07706133212");
    	Response response;
    	
    	response = customerRestService.createCustomer(customer);
    	customer = (Customer) response.getEntity();
    	
    	try {
    		customerRestService.updateCustomer(null, null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
    	}
    	
    	try {
    		customerRestService.updateCustomer(null, customer);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	try {
    		customerRestService.updateCustomer(customer.getId() + 5, customer);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	//duplicate email test
    	try {
    		customer.setEmail("j.gawron@newcastle.ac.uk");
    		customerRestService.updateCustomer(customer.getId(), customer);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
    	}
    	
    	//invalid bean test
    	try {
    		customer.setName("Jakub ...@#$as.dasd Gawron");
    		customerRestService.updateCustomer(customer.getId(), customer);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
    	}
    }
    
    /**
     * Create customer, then try to delete it
     */
    @Test
    @InSequence(8)
    public void deleteValidCustomer() {
    	Customer customer = new Customer(null, "Jakub Gawron", "test8@newcastle.ac.uk", "07706133212");
    	Response response;
    	
    	response = customerRestService.createCustomer(customer);
    	customer = (Customer) response.getEntity();
    	
    	response = customerRestService.deleteCustomer(customer.getId());
    	assertEquals("Unexpected response status", 204, response.getStatus());
    }
    
    /**
     * Delete customer that doesn't exist and check if exception is thrown
     */
    @Test
    @InSequence(9)
    public void deleteInvalidCustomer() {
    	
    	try {
    		customerRestService.deleteCustomer(null);
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    	
    	try {
    		customerRestService.deleteCustomer(new Long(0));
    		fail("Expected a RestServiceException to be thrown");
    	} catch (RestServiceException e) {
    		assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
    	}
    }
}

package org.jboss.quickstarts.wfk.customer;
import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Customer's email address conflicts with that of another Customer.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 */

public class CustomerExistsException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public CustomerExistsException(String message) {
		super(message);
	}
	
	public CustomerExistsException(String message, Throwable e) {
		super(message, e);
	}
	
	public CustomerExistsException(Throwable e) {
		super(e);
	}
}
package org.jboss.quickstarts.wfk.booking;

import javax.validation.ValidationException;

public class CustomerInvalidException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public CustomerInvalidException(String message) {
		super(message);
	}
	
	public CustomerInvalidException(String message, Throwable e) {
		super(message, e);
	}
	
	public CustomerInvalidException(Throwable e) {
		super(e);
	}
}

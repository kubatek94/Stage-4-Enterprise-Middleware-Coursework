package org.jboss.quickstarts.wfk.flight;

import javax.validation.ValidationException;

public class FlightExistsException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public FlightExistsException(String message) {
		super(message);
	}
	
	public FlightExistsException(String message, Throwable e) {
		super(message, e);
	}
	
	public FlightExistsException(Throwable e) {
		super(e);
	}
}

package org.jboss.quickstarts.wfk.booking;

import javax.validation.ValidationException;

public class FlightInvalidException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public FlightInvalidException(String message) {
		super(message);
	}
	
	public FlightInvalidException(String message, Throwable e) {
		super(message, e);
	}
	
	public FlightInvalidException(Throwable e) {
		super(e);
	}
}

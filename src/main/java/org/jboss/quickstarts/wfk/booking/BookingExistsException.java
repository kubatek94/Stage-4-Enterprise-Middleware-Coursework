package org.jboss.quickstarts.wfk.booking;

import javax.validation.ValidationException;

public class BookingExistsException extends ValidationException {
	private static final long serialVersionUID = 1L;
	
	public BookingExistsException(String message) {
		super(message);
	}
	
	public BookingExistsException(String message, Throwable e) {
		super(message, e);
	}
	
	public BookingExistsException(Throwable e) {
		super(e);
	}
}


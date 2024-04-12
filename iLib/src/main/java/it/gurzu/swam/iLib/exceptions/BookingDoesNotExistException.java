package it.gurzu.swam.iLib.exceptions;

public class BookingDoesNotExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BookingDoesNotExistException(String message, Throwable err) {
		super(message, err);
	}
	
	public BookingDoesNotExistException(String message) {
		super(message);
	}
}

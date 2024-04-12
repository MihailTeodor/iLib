package it.gurzu.swam.iLib.exceptions;

public class UserDoesNotExistException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UserDoesNotExistException(String message, Throwable err) {
		super(message, err);
	}
	
	public UserDoesNotExistException(String message) {
		super(message);
	}

}

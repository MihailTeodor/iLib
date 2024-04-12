package it.gurzu.swam.iLib.exceptions;

public class LoanDoesNotExistException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LoanDoesNotExistException(String message, Throwable err) {
		super(message, err);
	}
	
	public LoanDoesNotExistException(String message) {
		super(message);
	}
}

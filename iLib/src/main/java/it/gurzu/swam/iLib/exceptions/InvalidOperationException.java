package it.gurzu.swam.iLib.exceptions;

public class InvalidOperationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidOperationException(String message, Throwable err) {
		super(message, err);
	}
	
	public InvalidOperationException(String message) {
		super(message);
	}
}

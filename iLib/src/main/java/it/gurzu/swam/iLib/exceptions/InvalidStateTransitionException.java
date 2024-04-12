package it.gurzu.swam.iLib.exceptions;

public class InvalidStateTransitionException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public InvalidStateTransitionException(String message, Throwable err) {
		super(message, err);
	}
	
	public InvalidStateTransitionException(String message) {
		super(message);
	}
}

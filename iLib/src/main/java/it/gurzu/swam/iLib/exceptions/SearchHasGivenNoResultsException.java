package it.gurzu.swam.iLib.exceptions;

public class SearchHasGivenNoResultsException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public SearchHasGivenNoResultsException(String message, Throwable err) {
		super(message, err);
	}
	
	public SearchHasGivenNoResultsException(String message) {
		super(message);
	}
}

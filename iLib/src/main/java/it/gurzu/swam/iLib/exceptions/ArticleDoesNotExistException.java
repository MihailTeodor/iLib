package it.gurzu.swam.iLib.exceptions;

public class ArticleDoesNotExistException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ArticleDoesNotExistException(String message, Throwable err) {
		super(message, err);
	}
	
	public ArticleDoesNotExistException(String message) {
		super(message);
	}
}

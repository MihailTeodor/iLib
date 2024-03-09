package it.gurzu.swam.iLib.model;

public class Book extends Article {
	
	private String author;
	private String isbn;
	
	Book(){ }
	
	public Book(String uuid) {
		super(uuid);
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getIsbn() {
		return isbn;
	}
	
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
}
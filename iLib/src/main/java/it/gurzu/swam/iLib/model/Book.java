package it.gurzu.swam.iLib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "books")
public class Book extends Article {
	
	@NotNull
	private String author;
	@NotNull
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
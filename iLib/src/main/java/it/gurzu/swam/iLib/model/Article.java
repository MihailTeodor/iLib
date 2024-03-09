package it.gurzu.swam.iLib.model;

import java.sql.Date;

public abstract class Article extends BaseEntity {

	private String location;
	private String title;
	private Date yearEdition;
	private String publisher;
	private String genre;
	private String description;
	private State state;
	
	Article(){ }
	
	public Article(String uuid) {
		super(uuid);
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getYearEdition() {
		return yearEdition;
	}
	
	public void setYearEdition(Date yearEdition) {
		this.yearEdition = yearEdition;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public String getGenre() {
		return genre;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public State getState() {
		return state;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
}
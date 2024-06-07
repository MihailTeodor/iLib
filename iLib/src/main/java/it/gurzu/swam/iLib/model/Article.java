package it.gurzu.swam.iLib.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Article extends BaseEntity {

	private String location;
	private String title;
	private LocalDate yearEdition;
	private String publisher;
	private String genre;
	private String description;
	
	@Enumerated(EnumType.STRING)
	private ArticleState state;
	
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
	
	public LocalDate getYearEdition() {
		return yearEdition;
	}
	
	public void setYearEdition(LocalDate yearEdition2) {
		this.yearEdition = yearEdition2;
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
	
	public ArticleState getState() {
		return state;
	}
	
	public void setState(ArticleState state) {
		this.state = state;
	}
	
}
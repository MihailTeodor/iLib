package it.gurzu.swam.iLib.dto;

import java.time.LocalDate;

import it.gurzu.swam.iLib.model.ArticleState;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class ArticleDTO {

    private Long id;
    
    @NotNull(message = "Type is required")
    private ArticleType type;

    @NotEmpty(message = "Location is required")
    private String location;
  
    @NotEmpty(message = "Title is required")
    private String title;
    
    @NotNull(message = "Year of edition is required")
    @PastOrPresent(message = "Year of edition cannot be in the future")
    private LocalDate yearEdition;
    
    @NotEmpty(message = "Publisher is required")
    private String publisher;
    
    @NotEmpty(message = "Genre is required")
    private String genre;
    
    private String description;
    private ArticleState state;

    private String author;
    private String isbn;
    private Integer issueNumber;
    private String issn;
    private String director;
    private String isan;
    private LocalDate loanDueDate;
    private LocalDate bookingEndDate;
    		
    public ArticleDTO() {}
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public ArticleType getType() {
		return type;
	}
	public void setType(ArticleType type) {
		this.type = type;
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
	public void setYearEdition(LocalDate yearEdition) {
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
	public ArticleState getState() {
		return state;
	}
	public void setState(ArticleState state) {
		this.state = state;
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
	public Integer getIssueNumber() {
		return issueNumber;
	}
	public void setIssueNumber(Integer issueNumber) {
		this.issueNumber = issueNumber;
	}
	public String getIssn() {
		return issn;
	}
	public void setIssn(String issn) {
		this.issn = issn;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getIsan() {
		return isan;
	}
	public void setIsan(String isan) {
		this.isan = isan;
	}
	
    public LocalDate getLoanDueDate() {
		return loanDueDate;
	}

	public void setLoanDueDate(LocalDate loanDueDate) {
		this.loanDueDate = loanDueDate;
	}

	public LocalDate getBookingEndDate() {
		return bookingEndDate;
	}

	public void setBookingEndDate(LocalDate bookingEndDate) {
		this.bookingEndDate = bookingEndDate;
	}
	
}

package it.gurzu.swam.iLib.model;

import java.sql.Date;

public class Booking extends BaseEntity {

	private Article bookedArticle;
	private User bookingUser;
	private Date bookingEndDate; 

	Booking() { }
	
	public Booking(String uuid) {
		super(uuid);
	}
	
	public Article getBookedArticle() {
		return bookedArticle;
	}
	
	public void setBookedArticle(Article bookedArticle) {
		this.bookedArticle = bookedArticle;
	}
	
	public User getBookingUser() {
		return bookingUser;
	}
	
	public void setBookingUser(User bookingUser) {
		this.bookingUser = bookingUser;
	}
	
	public Date getBookingEndDate() {
		return bookingEndDate;
	}
	
	public void setBookingEndDate(Date bookingEndDate) {
		this.bookingEndDate = bookingEndDate;
	}
	
}
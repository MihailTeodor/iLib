package it.gurzu.swam.iLib.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

	@ManyToOne
	private Article bookedArticle;
	
	@ManyToOne 
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
	
	/**
	 * Validates the state of the Article related to a given Booking. 
	 * If the end date of the Booking has passed, the state of the Article is set to AVAILABLE, thus it can be booked by other users.
	 * @param booking the Booking relative to the Article to be validated.
	 */
	public static void validateState(Booking booking) {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		int comparisonResult = booking.getBookingEndDate().compareTo(today); 
		if(comparisonResult < 0) {
			booking.getBookedArticle().setState(State.AVAILABLE);
		}
	}
	
}
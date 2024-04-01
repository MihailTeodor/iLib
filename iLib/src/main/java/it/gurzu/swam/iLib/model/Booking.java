package it.gurzu.swam.iLib.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

	@ManyToOne
	private Article bookedArticle;
	
	@ManyToOne 
	private User bookingUser;
	
	private Date bookingDate;
	
	private Date bookingEndDate; 
	
	@Enumerated(EnumType.STRING)
	private BookingState state;

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
	
	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}
	
	public BookingState getState() {
		return state;
	}

	public void setState(BookingState state) {
		this.state = state;
	}

	/**
	 * Validates the state of the Article related to a given ACTIVE Booking. 
	 * If the end date of the Booking has passed, the state of the Article is set to AVAILABLE, thus it can be booked by other users.
	 * In this case, the state of the booking is set to CANCELLED.
	 * If the passed booking state is not ACTIVE, throws an IllegalArgumentException.
	 * @param booking the Booking relative to the Article to be validated.
	 */
	public static void validateState(Booking booking) {
		if(booking.state == BookingState.ACTIVE) {
			long millis = System.currentTimeMillis();
			Date today = new Date(millis);
			int comparisonResult = booking.getBookingEndDate().compareTo(today); 
			if(comparisonResult < 0) {
				booking.getBookedArticle().setState(ArticleState.AVAILABLE);
				booking.setState(BookingState.CANCELLED);
			}			
		}else
			throw new IllegalArgumentException("The Booking state is not ACTIVE!");
	}
}
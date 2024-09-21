package it.gurzu.swam.iLib.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER) 
	private Article bookedArticle;
	
	@ManyToOne(fetch = FetchType.EAGER) 
	private User bookingUser;
	
	@Temporal(value = TemporalType.DATE)
	private LocalDate bookingDate;
	
	@Temporal(value = TemporalType.DATE)
	private LocalDate bookingEndDate; 
	
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
	
	public LocalDate getBookingEndDate() {
		return bookingEndDate;
	}
	
	public void setBookingEndDate(LocalDate bookingEndDate) {
		this.bookingEndDate = bookingEndDate;
	}
	
	public LocalDate getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(LocalDate bookingDate) {
		this.bookingDate = bookingDate;
	}
	
	public BookingState getState() {
		return state;
	}

	public void setState(BookingState state) {
		this.state = state;
	}

	/**
	 * Validates the state of the Article related to the specific Booking. 
	 * If the end date of the Booking has passed, the state of the Article is set to AVAILABLE, thus it can be booked by other users.
	 * In this case, the state of the booking is set to CANCELLED.
	 * If the specific booking state is not ACTIVE, throws an IllegalArgumentException.
	 */
	public void validateState() {
		if(this.state == BookingState.ACTIVE) {
			LocalDate today = LocalDate.now();
			if(this.getBookingEndDate().isBefore(today)) {
				this.setState(BookingState.EXPIRED);
				// at this point, the state of the booked article can be only one of {UNAVAILABLE, BOOKED}
				if(this.getBookedArticle().getState() == ArticleState.BOOKED) {
					this.getBookedArticle().setState(ArticleState.AVAILABLE);
				}
			}			
		}else
			throw new IllegalArgumentException("The Booking state is not ACTIVE!");
	}
}
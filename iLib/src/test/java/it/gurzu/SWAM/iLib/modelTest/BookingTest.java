package it.gurzu.SWAM.iLib.modelTest;

import java.sql.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.State;
import it.gurzu.swam.iLib.model.User;

public class BookingTest {
	private User user;
	private Article article;
	private Booking booking;
	
	@BeforeEach
	public void setup() {
		user = ModelFactory.user();
		article = ModelFactory.book();
		booking = ModelFactory.booking();
		booking.setBookingUser(user);
		booking.setBookedArticle(article);
	}
	
	@Test
	public void testValidateStateWhenBookingEndDatePassed() {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		booking.setBookingEndDate(Date.valueOf(today.toLocalDate().plusDays(-1)));
		
		Booking.validateState(booking);
		
		Assertions.assertEquals(State.AVAILABLE, booking.getBookedArticle().getState());
	}
	
	@Test
	public void testValidateStateWhenBookingEndDateNotPassed() {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		booking.setBookingEndDate(Date.valueOf(today.toLocalDate().plusDays(1)));
		booking.getBookedArticle().setState(State.BOOKED);
		
		Booking.validateState(booking);
		
		Assertions.assertEquals(State.BOOKED, booking.getBookedArticle().getState());
	}
}

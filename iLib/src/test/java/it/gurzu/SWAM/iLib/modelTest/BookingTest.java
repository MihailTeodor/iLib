package it.gurzu.SWAM.iLib.modelTest;

import java.sql.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.ArticleState;
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
	public void testValidateStateWhenBookingStateIsNotActive() {
		booking.setState(BookingState.COMPLETED);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		booking.setBookingEndDate(Date.valueOf(today.toLocalDate().plusDays(1)));

		Exception thrownException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Booking.validateState(booking);
		});
		Assertions.assertEquals("The Booking state is not ACTIVE!", thrownException.getMessage());
	}
	
	@Test
	public void testValidateStateWhenBookingEndDatePassed() {
		booking.setState(BookingState.ACTIVE);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		booking.setBookingEndDate(Date.valueOf(today.toLocalDate().plusDays(-1)));
		
		try {
			Booking.validateState(booking);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Assertions.assertEquals(ArticleState.AVAILABLE, booking.getBookedArticle().getState());
	}
	
	@Test
	public void testValidateStateWhenBookingEndDateNotPassed() {
		booking.setState(BookingState.ACTIVE);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		booking.setBookingEndDate(Date.valueOf(today.toLocalDate().plusDays(1)));
		booking.getBookedArticle().setState(ArticleState.BOOKED);
		
		try {
			Booking.validateState(booking);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Assertions.assertEquals(ArticleState.BOOKED, booking.getBookedArticle().getState());
	}
}

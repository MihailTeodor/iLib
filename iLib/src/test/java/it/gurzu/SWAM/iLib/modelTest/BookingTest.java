package it.gurzu.SWAM.iLib.modelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

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
	public void testValidateState_WhenBookingStateIsNotActive_ThrowsIllegalArgumentException() {
		booking.setState(BookingState.COMPLETED);
		LocalDate today = LocalDate.now();
		booking.setBookingEndDate(today.plusDays(1));

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			booking.validateState();
		});
		assertEquals("The Booking state is not ACTIVE!", thrownException.getMessage());
	}
	
	@Test
	public void testValidateState_WhenBookingEndDatePassed_ChangesArticleAndBookingStateAccordingly() {
		booking.setState(BookingState.ACTIVE);
		LocalDate today = LocalDate.now();
		booking.setBookingEndDate(today.plusDays(-1));
		
		try {
			booking.validateState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(ArticleState.AVAILABLE, booking.getBookedArticle().getState());
		assertEquals(BookingState.CANCELLED, booking.getState());
	}
	
	@Test
	public void testValidateState_WhenBookingEndDateNotPassed_DoesNotChangeState() {
		booking.setState(BookingState.ACTIVE);
		LocalDate today = LocalDate.now();
		booking.setBookingEndDate(today.plusDays(1));
		booking.getBookedArticle().setState(ArticleState.BOOKED);
		
		try {
			booking.validateState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(ArticleState.BOOKED, booking.getBookedArticle().getState());
		assertEquals(BookingState.ACTIVE, booking.getState());

	}
}

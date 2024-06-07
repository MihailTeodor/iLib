package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;

public class BookingDaoTest extends JPATest {

	private Booking booking;
	private Article article;
	private User user;
	private BookingDao bookingDao;
	
	@Override
	protected void init() throws IllegalAccessException {
		user = ModelFactory.user();
		
		article = ModelFactory.book();
		
		booking = ModelFactory.booking();
		booking.setBookedArticle(article);
		booking.setBookingUser(user);
		booking.setBookingEndDate(LocalDate.of(2024, 01, 01));
		booking.setState(BookingState.COMPLETED);
		
		em.persist(article);
		em.persist(user);
		em.persist(booking);
		
		bookingDao = new BookingDao();
		FieldUtils.writeField(bookingDao, "em", em, true);
	}
	
	@Test
	public void testGetUserFromBooking() {
		User retrievedUser = bookingDao.getUserFromBooking(booking);
		assertEquals(user, retrievedUser);
	}
	
	@Test
	public void testGetArticleFromBooking() {
		Article retrievedArticle = bookingDao.getArticleFromBooking(booking);
		assertEquals(article, retrievedArticle);
	}
		
	@Test 
	public void testSearchBookings() {
		Article article2 = ModelFactory.magazine();
		Booking booking2 = ModelFactory.booking();
		booking2.setBookingUser(user);
		booking2.setBookedArticle(article2);
		booking2.setBookingEndDate(LocalDate.of(2024, 03, 01));
		booking2.setState(BookingState.COMPLETED);
		em.persist(article2);
		em.persist(booking2);
		
		// test search by user
		List<Booking> retrievedBookings = bookingDao.searchBookings(user, null, 0, 10);
		
		assertEquals(2, retrievedBookings.size());
		assertEquals(booking2, retrievedBookings.get(0));
		assertEquals(booking, retrievedBookings.get(1));
				
		User user2 = ModelFactory.user();
		em.persist(user2);
		
		Booking booking3 = ModelFactory.booking();
		booking3.setBookingUser(user2);
		booking3.setBookedArticle(article2);
		booking3.setBookingEndDate(LocalDate.of(2024, 05, 01));
		booking3.setState(BookingState.ACTIVE);
		em.persist(booking3);
		
		// test search by article
		retrievedBookings = bookingDao.searchBookings(null, article2, 0, 10);
		
		assertEquals(2, retrievedBookings.size());
		assertEquals(booking3, retrievedBookings.get(0));
		assertEquals(booking2, retrievedBookings.get(1));
		
		// test search by user and article
		retrievedBookings = bookingDao.searchBookings(user2, article2, 0, 10);
		
		assertEquals(1, retrievedBookings.size());
		assertEquals(booking3, retrievedBookings.get(0));
		
		// test pagination and ordering
		List<Booking> retrievedBookingsFirstPage = bookingDao.searchBookings(null, null, 0, 2);
		List<Booking> retrievedBookingsSecondPage = bookingDao.searchBookings(null, null, 2, 1);
		
		assertEquals(booking3, retrievedBookingsFirstPage.get(0));
		assertEquals(booking2, retrievedBookingsFirstPage.get(1));
		assertEquals(booking, retrievedBookingsSecondPage.get(0));
	}
	
	@Test
	public void testCountBookings() {
		Article article2 = ModelFactory.magazine();
		Booking booking2 = ModelFactory.booking();
		booking2.setBookingUser(user);
		booking2.setBookedArticle(article2);
		booking2.setBookingEndDate(LocalDate.of(2024, 03, 01));
		booking2.setState(BookingState.COMPLETED);
		em.persist(article2);
		em.persist(booking2);

		User user2 = ModelFactory.user();
		em.persist(user2);

		Booking booking3 = ModelFactory.booking();
		booking3.setBookingUser(user2);
		booking3.setBookedArticle(article2);
		booking3.setBookingEndDate(LocalDate.of(2024, 05, 01));
		booking3.setState(BookingState.ACTIVE);
		em.persist(booking3);

		Long resultsNumber = bookingDao.countBookings(null, null);
		
		assertEquals(3, resultsNumber);
	}
	
}

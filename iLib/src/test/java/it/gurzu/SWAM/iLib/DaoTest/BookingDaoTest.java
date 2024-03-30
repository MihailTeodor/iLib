package it.gurzu.SWAM.iLib.DaoTest;

import java.sql.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
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
		booking.setBookingEndDate(Date.valueOf("2024-01-01"));
		
		em.persist(article);
		em.persist(user);
		em.persist(booking);
		
		bookingDao = new BookingDao();
		FieldUtils.writeField(bookingDao, "em", em, true);
	}
	
	@Test
	public void testGetUserFromBooking() {
		User retrievedUser = bookingDao.getUserFromBooking(booking);
		Assertions.assertEquals(user, retrievedUser);
	}
	
	@Test
	public void testGetArticleFromBooking() {
		Article retrievedArticle = bookingDao.getArticleFromBooking(booking);
		Assertions.assertEquals(article, retrievedArticle);
	}
		
	@Test void testSearchBookings() {
		Article article2 = ModelFactory.magazine();
		Booking booking2 = ModelFactory.booking();
		booking2.setBookingUser(user);
		booking2.setBookedArticle(article2);
		booking2.setBookingEndDate(Date.valueOf("2024-03-01"));
		em.persist(article2);
		em.persist(booking2);
		
		List<Booking> retrievedBookings = bookingDao.searchBookings(user, null);
		
		Assertions.assertEquals(2, retrievedBookings.size());
		Assertions.assertEquals(booking2, retrievedBookings.get(0));
		Assertions.assertEquals(booking, retrievedBookings.get(1));
				
		User user2 = ModelFactory.user();
		em.persist(user2);
		
		Booking booking3 = ModelFactory.booking();
		booking3.setBookingUser(user2);
		booking3.setBookedArticle(article2);
		booking3.setBookingEndDate(Date.valueOf("2024-05-01"));
		em.persist(booking3);
		
		retrievedBookings = bookingDao.searchBookings(null, article2);
		
		Assertions.assertEquals(2, retrievedBookings.size());
		Assertions.assertEquals(booking3, retrievedBookings.get(0));
		Assertions.assertEquals(booking2, retrievedBookings.get(1));
	}
}

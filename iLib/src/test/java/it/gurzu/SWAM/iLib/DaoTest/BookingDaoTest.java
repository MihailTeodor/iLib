package it.gurzu.SWAM.iLib.DaoTest;

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
		user .setName("Mihail");
		
		article = ModelFactory.book();
		article.setTitle("Cujo");
		
		booking = ModelFactory.booking();
		booking.setBookedArticle(article);
		booking.setBookingUser(user);
		
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
	
	@Test
	public void testFindBookingsByUser() {
		List<Booking> retrievedBookings = bookingDao.findBookingsByUser(user);
		Assertions.assertEquals(1, retrievedBookings.size());
		Assertions.assertEquals(true, retrievedBookings.contains(booking));
	}

	@Test
	public void testFindBookingsByArticle() {
		List<Booking> retrievedBookings = bookingDao.findBookingsByArticle(article);
		Assertions.assertEquals(1, retrievedBookings.size());
		Assertions.assertEquals(true, retrievedBookings.contains(booking));		
	}
}

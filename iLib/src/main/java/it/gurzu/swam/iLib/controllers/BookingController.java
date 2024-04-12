package it.gurzu.swam.iLib.controllers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.BookingDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import jakarta.inject.Inject;

public class BookingController {
	
	@Inject
	private BookingDao bookingDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private ArticleDao articleDao;
	
	private final LocalDate today = LocalDate.now();

	public void registerBooking(Long userId, Long articleId) {
		Booking bookingToRegister = null;
		if(userId == null)
			throw new IllegalArgumentException("Cannot register Booking, User not specified!");
		if(articleId == null)
			throw new IllegalArgumentException("Cannot register Booking, Article not specified!");
		
		User bookingUser = userDao.findById(userId);
		Article bookedArticle = articleDao.findById(articleId);
		if(bookingUser == null)
			throw new UserDoesNotExistException("Cannot register Booking, specified User not present in the system!");
		if(bookedArticle == null)
			throw new ArticleDoesNotExistException("Cannot register Booking, specified Article not present in catalogue!");
		
		bookingToRegister = ModelFactory.booking();
		
		switch(bookedArticle.getState()) {
		case BOOKED:
			throw new InvalidOperationException("Cannot register Booking, specified Article is already booked!");
		case ONLOANBOOKED:
			throw new InvalidOperationException("Cannot register Booking, specified Article is already booked!");
		case UNAVAILABLE:
			throw new InvalidOperationException("Cannot register Booking, specified Article is UNAVAILABLE!");
		case AVAILABLE:
			bookingToRegister.setBookingEndDate(Date.valueOf(today.plusDays(3)));
			bookedArticle.setState(ArticleState.BOOKED);
			break;
		case ONLOAN:
			bookedArticle.setState(ArticleState.ONLOANBOOKED);
			break;
		}
		
		bookingToRegister.setBookingUser(bookingUser);
		bookingToRegister.setBookedArticle(bookedArticle);
		bookingToRegister.setBookingDate(Date.valueOf(today));
		
		bookingToRegister.setState(BookingState.ACTIVE);
		
		bookingDao.save(bookingToRegister);
	}
	
	public Booking getBookingInfo(Long bookingId) {
		Booking booking = bookingDao.findById(bookingId);
		
		if(booking == null)
			throw new BookingDoesNotExistException("Specified Booking not registered in the system!");
		
		if(booking.getState() == BookingState.ACTIVE)
			booking.validateState();
		
		return booking;
	}

	public void cancelBooking(Long bookingId) {
		Booking bookingToCancel = bookingDao.findById(bookingId);
		
		if(bookingToCancel == null)
			throw new BookingDoesNotExistException("Cannot cancel Booking. Specified Booking not registered in the system!");
		
		if(bookingToCancel.getState() != BookingState.ACTIVE)
			throw new InvalidOperationException("Cannot cancel Booking. Specified Booking is not active!");
		
		bookingToCancel.getBookedArticle().setState(ArticleState.AVAILABLE);
		bookingToCancel.setState(BookingState.CANCELLED);
	}
	
	public List<Booking> getBookedArticlesByUser(Long userId) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");
		
		List<Booking> userBookings = bookingDao.searchBookings(user, null);
		
		if(userBookings.isEmpty())
			throw new SearchHasGivenNoResultsException("No bookings relative to the specified user found!");
		
		for(Booking booking : userBookings)
			if(booking.getState() == BookingState.ACTIVE)
				booking.validateState();
		return userBookings;
	}
}

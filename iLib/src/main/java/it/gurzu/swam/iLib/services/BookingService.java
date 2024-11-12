package it.gurzu.swam.iLib.services;

import java.time.LocalDate;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.BookingDTO;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.BookingDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@RequestScoped
@Transactional
public class BookingService {
	
	@Inject
	private BookingDao bookingDao;
	
	@Inject
	private LoanDao loanDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private ArticleDao articleDao;
	
	private final LocalDate today = LocalDate.now();

	public Long registerBooking(Long userId, Long articleId) {
		Booking bookingToRegister = null;
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
			bookingToRegister.setBookingEndDate(today.plusDays(3));
			bookedArticle.setState(ArticleState.BOOKED);
			break;
		case ONLOAN:
			List<Loan> existingLoans = loanDao.searchLoans(bookingUser, bookedArticle, 0, 1);
			if (!(existingLoans.isEmpty()) && (existingLoans.get(0).getState() == LoanState.ACTIVE || existingLoans.get(0).getState() == LoanState.OVERDUE))
				throw new InvalidOperationException("Cannot register Booking, selected user has selected Article currently on loan!");
			bookedArticle.setState(ArticleState.ONLOANBOOKED);
			bookingToRegister.setBookingEndDate(loanDao.searchLoans(null, bookedArticle, 0, 1).get(0).getDueDate().plusDays(3));
			break;
		}
		
		bookingToRegister.setBookingUser(bookingUser);
		bookingToRegister.setBookedArticle(bookedArticle);
		bookingToRegister.setBookingDate(today);
		
		bookingToRegister.setState(BookingState.ACTIVE);
		
		bookingDao.save(bookingToRegister);
		
		return bookingToRegister.getId();
	}
	
	public BookingDTO getBookingInfo(Long bookingId) {
		Booking booking = bookingDao.findById(bookingId);
		
		if(booking == null)
			throw new BookingDoesNotExistException("Specified Booking not registered in the system!");
		
		if(booking.getState() == BookingState.ACTIVE) {
			booking.validateState();
			bookingDao.save(booking);
		}
		
		return new BookingDTO(booking);
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
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public List<Booking> getBookingsByUser(Long userId, int fromIndex, int limit) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");
		
		List<Booking> userBookings = bookingDao.searchBookings(user, null, fromIndex, limit);
		
		if(userBookings.isEmpty())
			throw new SearchHasGivenNoResultsException("No bookings relative to the specified user found!");
		
		for(Booking booking : userBookings)
			if(booking.getState() == BookingState.ACTIVE) {
				booking.validateState();
				bookingDao.save(booking);
			}
		return userBookings;
	}
	
	public Long countBookingsByUser(Long userId) {
		User user = userDao.findById(userId);

		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");

			return bookingDao.countBookings(user, null);
	}
}

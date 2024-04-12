package it.gurzu.swam.iLib.controllers;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.exceptions.ArticleDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.InvalidOperationException;
import it.gurzu.swam.iLib.exceptions.LoanDoesNotExistException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import jakarta.inject.Inject;

public class LoanController {

	@Inject
	private UserDao userDao;
	
	@Inject
	private ArticleDao articleDao;
	
	@Inject 
	private LoanDao loanDao;
	
	@Inject
	private BookingDao bookingDao;

	private final LocalDate today = LocalDate.now();

	
	public void registerLoan(Long userId, Long articleId) {
		Loan loanToRegister = null;
		if(userId == null)
			throw new IllegalArgumentException("Cannot register Loan, User not specified!");
		if(articleId == null)
			throw new IllegalArgumentException("Cannot register Loan, Article not specified!");

		User loaningUser = userDao.findById(userId);
		Article loanedArticle = articleDao.findById(articleId);
		
		if(loaningUser == null)
			throw new UserDoesNotExistException("Cannot register Loan, specified User not present in the system!");
		if(loanedArticle == null)
			throw new ArticleDoesNotExistException("Cannot register Loan, specified Article not present in catalogue!");

		switch(loanedArticle.getState()) {
		case BOOKED:
			List<Booking> bookings = bookingDao.searchBookings(null, loanedArticle);
			if(bookings.get(0).getBookingUser() != loaningUser)
				throw new InvalidOperationException("Cannot register Loan, specified Article is booked by another user!");
			else
				bookings.get(0).setState(BookingState.COMPLETED);
				break;
		case ONLOAN:
			throw new InvalidOperationException("Cannot register Loan, specified Article is already on loan!");
		case ONLOANBOOKED:
			throw new InvalidOperationException("Cannot register Loan, specified Article is already on loan!");
		case UNAVAILABLE:
			throw new InvalidOperationException("Cannot register Loan, specified Article is UNAVAILABLE!");
		default:
			break;
		}
		
		loanToRegister = ModelFactory.loan();
		loanToRegister.setArticleOnLoan(loanedArticle);
		loanToRegister.setLoaningUser(loaningUser);
		loanToRegister.setLoanDate(Date.valueOf(today));
		loanToRegister.setDueDate(Date.valueOf(today.plusMonths(1)));
		loanToRegister.setRenewed(false);
		
		loanedArticle.setState(ArticleState.ONLOAN);
		loanToRegister.setState(LoanState.ACTIVE);
		
		loanDao.save(loanToRegister);
	} 
	
	public void registerReturn(Long loanId) {
		Loan loanToReturn = loanDao.findById(loanId);
		
		if(loanToReturn == null)
			throw new LoanDoesNotExistException("Cannot return article! Loan not registered!");
		
		if(loanToReturn.getState() == LoanState.RETURNED)
			throw new InvalidOperationException("Cannot return article! Loan has already been returned!");
		
		Article loanArticle = loanToReturn.getArticleOnLoan();
		
		switch(loanArticle.getState()) {
		case ONLOAN:
			loanArticle.setState(ArticleState.AVAILABLE);
			break;
		case UNAVAILABLE:
			loanArticle.setState(ArticleState.AVAILABLE);
			break;
		case ONLOANBOOKED:
			loanArticle.setState(ArticleState.BOOKED);			
			Booking booking = bookingDao.searchBookings(null, loanArticle).get(0);
			booking.setBookingEndDate(Date.valueOf(today.plusDays(3)));
			break;
		default:
			break;
		}
				
		loanToReturn.setState(LoanState.RETURNED);
	}
	
	public Loan getLoanInfo(Long loanId) {
		Loan loan = loanDao.findById(loanId);
		
		if(loan == null)
			throw new LoanDoesNotExistException("Specified Loan not registered in the system!");
		
		if(loan.getState() == LoanState.ACTIVE)
			loan.validateState();
		
		return loan;
	}

	public List<Loan> getLoansByUser(Long userId) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");

		List<Loan> userLoans = loanDao.searchLoans(user, null);
		
		if(userLoans.isEmpty())
			throw new LoanDoesNotExistException("No loans relative to the specified user found!");
		
		for(Loan loan : userLoans)
			if(loan.getState() == LoanState.ACTIVE)
				loan.validateState();
		
		return userLoans;
	}
	
	public void extendLoan(Long loanId) {
		Loan loanToExtend = loanDao.findById(loanId);
		
		if(loanToExtend == null)
			throw new LoanDoesNotExistException("Cannot extend Loan! Loan does not exist!");

		if(loanToExtend.getArticleOnLoan().getState() == ArticleState.ONLOAN)
			loanToExtend.setDueDate(Date.valueOf(today.plusMonths(1)));
		else
			throw new InvalidOperationException("Cannot extend loan, another User has booked the Article!");
	}
}

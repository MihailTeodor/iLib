package it.gurzu.swam.iLib.controllers;

import java.time.LocalDate;
import java.util.List;

import it.gurzu.swam.iLib.dao.ArticleDao;
import it.gurzu.swam.iLib.dao.BookingDao;
import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.LoanDTO;
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
import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Model
@Transactional
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

	
	public Long registerLoan(Long userId, Long articleId) {
		Loan loanToRegister = null;
		User loaningUser = userDao.findById(userId);
		Article loanedArticle = articleDao.findById(articleId);
		
		if(loaningUser == null)
			throw new UserDoesNotExistException("Cannot register Loan, specified User not present in the system!");
		if(loanedArticle == null)
			throw new ArticleDoesNotExistException("Cannot register Loan, specified Article not present in catalogue!");

		switch(loanedArticle.getState()) {
		case BOOKED:
			List<Booking> bookings = bookingDao.searchBookings(null, loanedArticle, 0, 1);
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
		loanToRegister.setLoanDate(today);
		loanToRegister.setDueDate(today.plusMonths(1));
		loanToRegister.setRenewed(false);
		
		loanedArticle.setState(ArticleState.ONLOAN);
		loanToRegister.setState(LoanState.ACTIVE);
		
		loanDao.save(loanToRegister);
		
		return loanToRegister.getId();
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
			Booking booking = bookingDao.searchBookings(null, loanArticle, 0, 1).get(0);
			booking.setBookingEndDate(today.plusDays(3));
			break;
		default:
			break;
		}
				
		loanToReturn.setState(LoanState.RETURNED);
	}
	
	public LoanDTO getLoanInfo(Long loanId) {
		Loan loan = loanDao.findById(loanId);
		
		if(loan == null)
			throw new LoanDoesNotExistException("Specified Loan not registered in the system!");
		
		if(loan.getState() == LoanState.ACTIVE)
			loan.validateState();
		
		return new LoanDTO(loan);
	}

	@Transactional(value = TxType.REQUIRES_NEW)
	public List<Loan> getLoansByUser(Long userId, int fromIndex, int limit) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");

		List<Loan> userLoans = loanDao.searchLoans(user, null, fromIndex, limit);
		
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
			loanToExtend.setDueDate(today.plusMonths(1));
		else
			throw new InvalidOperationException("Cannot extend loan, another User has booked the Article!");
	}
	
	public Long countLoansByUser(Long userId) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("Specified user is not registered in the system!");

		return loanDao.countLoans(user, null);
	}
}

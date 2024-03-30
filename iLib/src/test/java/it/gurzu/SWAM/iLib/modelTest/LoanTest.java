package it.gurzu.SWAM.iLib.modelTest;

import java.sql.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.State;
import it.gurzu.swam.iLib.model.User;

public class LoanTest {
	private User user;
	private Article article;
	private Loan loan;
	
	@BeforeEach
	public void setup() {
		user = ModelFactory.user();
		article = ModelFactory.book();
		loan = ModelFactory.loan();
		loan.setLoaningUser(user);
		loan.setArticleOnLoan(article);
	}
	
	@Test
	public void testValidateStateWhenLoanDueDatePassed() {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		loan.setDueDate(Date.valueOf(today.toLocalDate().plusDays(-1)));
		
		Loan.validateState(loan);
		
		Assertions.assertEquals(State.UNAVAILABLE, loan.getArticleOnLoan().getState());
	}
	
	@Test
	public void testValidateStateWhenBookingEndDateNotPassed() {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		loan.setDueDate(Date.valueOf(today.toLocalDate().plusDays(1)));
		loan.getArticleOnLoan().setState(State.ONLOAN);
		
		Loan.validateState(loan);
		
		Assertions.assertEquals(State.ONLOAN, loan.getArticleOnLoan().getState());
	}

}

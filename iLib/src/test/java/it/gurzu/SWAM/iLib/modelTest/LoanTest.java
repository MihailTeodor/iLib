package it.gurzu.SWAM.iLib.modelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.ArticleState;
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
	public void testValidateStateWhenLoanStateIsNotActive() {
		loan.setState(LoanState.RETURNED);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		loan.setDueDate(Date.valueOf(today.toLocalDate().plusDays(1)));

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			Loan.validateState(loan);
		});
		assertEquals("The Loan state is not ACTIVE!", thrownException.getMessage());
	}

	@Test
	public void testValidateStateWhenLoanDueDatePassed() {
		loan.setState(LoanState.ACTIVE);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		loan.setDueDate(Date.valueOf(today.toLocalDate().plusDays(-1)));
		
		Loan.validateState(loan);
		
		assertEquals(ArticleState.UNAVAILABLE, loan.getArticleOnLoan().getState());
		assertEquals(LoanState.OVERDUE, loan.getState());
	}
	
	@Test
	public void testValidateStateWhenBookingEndDateNotPassed() {
		loan.setState(LoanState.ACTIVE);
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		loan.setDueDate(Date.valueOf(today.toLocalDate().plusDays(1)));
		loan.getArticleOnLoan().setState(ArticleState.ONLOAN);
		
		Loan.validateState(loan);
		
		assertEquals(ArticleState.ONLOAN, loan.getArticleOnLoan().getState());
	}

}

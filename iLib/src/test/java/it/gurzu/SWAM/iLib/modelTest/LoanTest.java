package it.gurzu.SWAM.iLib.modelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

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
	public void testValidateState_WhenLoanStateIsNotActive_ThrowsIllegalArgumentException() {
		loan.setState(LoanState.RETURNED);
		LocalDate today = LocalDate.now();
		loan.setDueDate(today.plusDays(1));

		Exception thrownException = assertThrows(IllegalArgumentException.class, () -> {
			loan.validateState();
		});
		assertEquals("The Loan state is not ACTIVE!", thrownException.getMessage());
	}

	@Test
	public void testValidateState_WhenLoanDueDatePassed_SetsArticleAndLoanStateAccordingly() {
		loan.setState(LoanState.ACTIVE);
		LocalDate today = LocalDate.now();
		loan.setDueDate(today.plusDays(-1));
		
		loan.validateState();
		
		assertEquals(ArticleState.UNAVAILABLE, loan.getArticleOnLoan().getState());
		assertEquals(LoanState.OVERDUE, loan.getState());
	}
	
	@Test
	public void testValidateState_WhenBookingEndDateNotPassed_DoesNotChangeState() {
		loan.setState(LoanState.ACTIVE);
		LocalDate today = LocalDate.now();
		loan.setDueDate(today.plusDays(1));
		loan.getArticleOnLoan().setState(ArticleState.ONLOAN);
		
		loan.validateState();
		
		assertEquals(ArticleState.ONLOAN, loan.getArticleOnLoan().getState());
		assertEquals(LoanState.ACTIVE, loan.getState());
	}

}

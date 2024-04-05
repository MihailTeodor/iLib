package it.gurzu.SWAM.iLib.daoTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;

public class LoanDaoTest extends JPATest {

	private Loan loan;
	private User user;
	private Article article;
	private LoanDao loanDao;
	
	@Override
	protected void init() throws IllegalAccessException {
		user = ModelFactory.user();
		
		article = ModelFactory.book();
		
		loan = ModelFactory.loan();
		loan.setArticleOnLoan(article);
		loan.setLoaningUser(user);
		loan.setDueDate(Date.valueOf("2024-01-01"));
		loan.setState(LoanState.RETURNED);
		
		em.persist(article);
		em.persist(user);
		em.persist(loan);
		
		loanDao = new LoanDao();
		FieldUtils.writeField(loanDao, "em", em, true);	
	}

	@Test
	public void testGetUserFromLoan() {
		User retrievedUser = loanDao.getUserFromLoan(loan);
		assertEquals(user, retrievedUser);
	}
	
	@Test
	public void testGetArticleFromLoan() {
		Article retrievedArticle = loanDao.getArticleFromLoan(loan);
		assertEquals(article, retrievedArticle);
	}
	
	@Test
	public void testSearchLoans() {
		Article article2 = ModelFactory.magazine();
		Loan loan2 = ModelFactory.loan();
		loan2.setArticleOnLoan(article2);
		loan2.setLoaningUser(user);
		loan2.setDueDate(Date.valueOf("2024-03-01"));
		loan2.setState(LoanState.RETURNED);
		em.persist(article2);
		em.persist(loan2);
		
		// test search by user
		List<Loan> retrievedLoans = loanDao.searchLoans(user, null);
		
		assertEquals(2, retrievedLoans.size());
		assertEquals(loan2, retrievedLoans.get(0));
		assertEquals(loan, retrievedLoans.get(1));

		User user2 = ModelFactory.user();
		em.persist(user2);
		
		Loan loan3 = ModelFactory.loan();
		loan3.setLoaningUser(user2);
		loan3.setArticleOnLoan(article2);
		loan3.setDueDate(Date.valueOf("2024-05-01"));
		loan3.setState(LoanState.ACTIVE);
		em.persist(loan3);
		
		//test search by article
		retrievedLoans = loanDao.searchLoans(null, article2);
		
		assertEquals(2, retrievedLoans.size());
		assertEquals(loan3, retrievedLoans.get(0));
		assertEquals(loan2, retrievedLoans.get(1));	
		
		// test search by user and article
		retrievedLoans = loanDao.searchLoans(user2, article2);
		
		assertEquals(1, retrievedLoans.size());
		assertEquals(loan3, retrievedLoans.get(0));		
		
		// test ordering
		retrievedLoans = loanDao.searchLoans(null, null);
		
		assertEquals(3, retrievedLoans.size());
		assertEquals(loan3, retrievedLoans.get(0));
		assertEquals(loan2, retrievedLoans.get(1));	
		assertEquals(loan, retrievedLoans.get(2));	
		
	}
}
package it.gurzu.SWAM.iLib.DaoTest;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.dao.LoanDao;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Loan;
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
		user.setName("Mihail");
		
		article = ModelFactory.book();
		article.setTitle("Cujo");
		
		loan = ModelFactory.loan();
		loan.setArticleOnLoan(article);
		loan.setLoaningUser(user);
		
		em.persist(article);
		em.persist(user);
		em.persist(loan);
		
		loanDao = new LoanDao();
		FieldUtils.writeField(loanDao, "em", em, true);	
	}

	@Test
	public void testGetUserFromLoan() {
		User retrievedUser = loanDao.getUserFromLoan(loan);
		Assertions.assertEquals(user, retrievedUser);
	}
	
	@Test
	public void testGetArticleFromLoan() {
		Article retrievedArticle = loanDao.getArticleFromLoan(loan);
		Assertions.assertEquals(article, retrievedArticle);
	}
	
	@Test
	public void testFindLoansByUser() {
		List<Loan> retrievedLoans = loanDao.findLoansByUser(user);
		Assertions.assertEquals(1, retrievedLoans.size());
		Assertions.assertEquals(true, retrievedLoans.contains(loan));
	}
	
	@Test
	public void testFindLoansByArticle() {
		List<Loan> retrievedLoans = loanDao.findLoansByArticle(article);
		Assertions.assertEquals(1, retrievedLoans.size());
		Assertions.assertEquals(true, retrievedLoans.contains(loan));
	}
}

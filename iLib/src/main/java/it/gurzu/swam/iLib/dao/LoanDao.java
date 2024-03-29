package it.gurzu.swam.iLib.dao;

import java.util.List;

import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.User;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class LoanDao extends BaseDao<Loan> {
	
	public LoanDao() {
		super(Loan.class);
	}
	
	public User getUserFromLoan(Loan loan) {
		return this.em.createQuery("SELECT l.loaningUser FROM Loan l WHERE l.id = :loanId", User.class)
				.setParameter("loanId", loan.getId())
				.getSingleResult();
	}
	
	public Article getArticleFromLoan(Loan loan) {
		return this.em.createQuery("SELECT l.articleOnLoan FROM Loan l WHERE l.id = :loanId", Article.class)
				.setParameter("loanId", loan.getId())
				.getSingleResult();
	}
	
    public List<Loan> findLoansByUser(User user) {
        return this.em.createQuery("SELECT l FROM Loan l WHERE l.loaningUser = :user", Loan.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Loan> findLoansByArticle(Article article) {
        return this.em.createQuery("SELECT l FROM Loan l WHERE l.articleOnLoan = :article", Loan.class)
                .setParameter("article", article)
                .getResultList();
    }

}

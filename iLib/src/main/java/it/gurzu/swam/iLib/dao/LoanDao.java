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
	
    public List<Loan> findLoanByUser(User user) {
        return this.em.createQuery("SELECT l FROM Loan l WHERE l.loaningUser = :user", Loan.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Loan> findLoanByArticle(Article article) {
        return this.em.createQuery("SELECT l FROM Loan l WHERE l.articleOnLoan = :article", Loan.class)
                .setParameter("article", article)
                .getResultList();
    }

}

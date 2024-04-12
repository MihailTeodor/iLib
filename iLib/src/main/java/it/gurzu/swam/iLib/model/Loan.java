package it.gurzu.swam.iLib.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {

	@ManyToOne
	private Article articleOnLoan;
	
	@ManyToOne
	private User loaningUser;
	
	private Date loanDate;
	private Date dueDate;
	private boolean renewed;
	
	private LoanState state;

	Loan() { }
	
	public Loan(String uuid) {
		super(uuid);
	}
	
	public Article getArticleOnLoan() {
		return articleOnLoan;
	}
	
	public void setArticleOnLoan(Article articleOnLoan) {
		this.articleOnLoan = articleOnLoan;
	}
	
	public User getLoaningUser() {
		return loaningUser;
	}
	
	public void setLoaningUser(User loaningUser) {
		this.loaningUser = loaningUser;
	}
		
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public boolean isRenewed() {
		return renewed;
	}
	
	public void setRenewed(boolean renewed) {
		this.renewed = renewed;
	}
	
	public Date getLoanDate() {
		return loanDate;
	}

	public void setLoanDate(Date loanDate) {
		this.loanDate = loanDate;
	}

	public LoanState getState() {
		return state;
	}

	public void setState(LoanState state) {
		this.state = state;
	}

	/**
	 * Validates the state of the Article related to the specific Loan.
	 * If the due date has passed, the state of the Article is set to UNAVAILABLE and it cannot be booked anymore. 
	 * In this case the state of the Loan is set to OVERDUE. 
	 * If the specific loan's state is not ACTIVE, an IllegalArgumentException is thrown.
	 */
	public void validateState() {
		if(this.getState() == LoanState.ACTIVE) {
			long millis = System.currentTimeMillis();
			Date today = new Date(millis);
			int comparizonResult = this.getDueDate().compareTo(today);
			if(comparizonResult < 0) {
				this.getArticleOnLoan().setState(ArticleState.UNAVAILABLE);
				this.setState(LoanState.OVERDUE);
			}			
		}else
			throw new IllegalArgumentException("The Loan state is not ACTIVE!");
	}
}
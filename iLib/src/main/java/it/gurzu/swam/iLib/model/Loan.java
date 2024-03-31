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
	
	private Date startDate;
	private Date dueDate;
	private boolean renewed;

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
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
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
	
	/**
	 * Validates the state of the Article related to a given Loan. 
	 * If the due date has passed, the state of the Article is set to UNAVAILABLE and it cannot be booked anymore.
	 * @param loan the Loan relative to the Article to be validated.
	 */
	public static void validateState(Loan loan) {
		long millis = System.currentTimeMillis();
		Date today = new Date(millis);
		int comparizonResult = loan.getDueDate().compareTo(today);
		if(comparizonResult < 0) {
			loan.getArticleOnLoan().setState(State.UNAVAILABLE);
		}

	}
	
}
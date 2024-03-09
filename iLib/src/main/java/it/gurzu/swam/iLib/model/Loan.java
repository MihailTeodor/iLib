package it.gurzu.swam.iLib.model;

import java.sql.Date;

public class Loan extends BaseEntity {

	private Article articleOnLoan;
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
	
}
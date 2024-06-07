package it.gurzu.swam.iLib.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER) 
	private Article articleOnLoan;
	
	@ManyToOne(fetch = FetchType.EAGER) 
	private User loaningUser;
	
	@Temporal(value = TemporalType.DATE)
	private LocalDate loanDate;
	
	@Temporal(value = TemporalType.DATE)
	private LocalDate dueDate;
	private boolean renewed;
	
	@Enumerated(EnumType.STRING)
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
		
	public LocalDate getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	
	public boolean isRenewed() {
		return renewed;
	}
	
	public void setRenewed(boolean renewed) {
		this.renewed = renewed;
	}
	
	public LocalDate getLoanDate() {
		return loanDate;
	}

	public void setLoanDate(LocalDate loanDate) {
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
			LocalDate today = LocalDate.now();
			if(this.getDueDate().isBefore(today)) {
				this.getArticleOnLoan().setState(ArticleState.UNAVAILABLE);
				this.setState(LoanState.OVERDUE);
			}			
		}else
			throw new IllegalArgumentException("The Loan state is not ACTIVE!");
	}
}
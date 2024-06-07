package it.gurzu.swam.iLib.dto;

import java.time.LocalDate;

import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class LoanDTO {

    private Long id;
    
    @NotNull(message = "Article ID cannot be null")
    private Long articleId;
    private String articleTitle;
    
    @NotNull(message = "Loaning user ID cannot be null")
    private Long loaningUserId;
    
    @NotNull(message = "Loan date cannot be null")
    @PastOrPresent(message = "Loan date cannot be in the future")
    private LocalDate loanDate;
    
    @NotNull(message = "Due date cannot be null")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;
    private boolean renewed;
    private LoanState state;

    public LoanDTO() {}
    
    public LoanDTO(Loan loan) {
    	this.id = loan.getId();
    	this.articleId = loan.getArticleOnLoan().getId();
    	this.articleTitle = loan.getArticleOnLoan().getTitle();
    	this.loaningUserId = loan.getLoaningUser().getId();
    	this.loanDate = loan.getLoanDate();
    	this.dueDate = loan.getDueDate();
    	this.renewed = loan.isRenewed();
    	this.state = loan.getState();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public Long getLoaningUserId() {
        return loaningUserId;
    }

    public void setLoaningUserId(Long loaningUserId) {
        this.loaningUserId = loaningUserId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
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

    public LoanState getState() {
        return state;
    }

    public void setState(LoanState state) {
        this.state = state;
    }
}

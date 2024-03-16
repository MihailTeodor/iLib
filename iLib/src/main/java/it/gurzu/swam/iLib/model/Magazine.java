package it.gurzu.swam.iLib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "magazines")
public class Magazine extends Article {

	private int issueNumber;
	private String issn;

	Magazine() { }
	
	public Magazine(String uuid) {
		super(uuid);
	}
	
	public int getIssueNumber() {
		return issueNumber;
	}
	
	public void setIssueNumber(int issueNumber) {
		this.issueNumber = issueNumber;
	}
	
	public String getIssn() {
		return issn;
	}
	
	public void setIssn(String issn) {
		this.issn = issn;
	}
	
}
package it.gurzu.swam.iLib.model;

import java.sql.Date;

public class Notification extends BaseEntity {

	private String message;
	private Date date;
	private boolean toRead;

	Notification() { }
	
	public Notification(String uuid) {
		super(uuid);
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public boolean isToRead() {
		return toRead;
	}
	
	public void setToRead(boolean toRead) {
		this.toRead = toRead;
	}
	
}
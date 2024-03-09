package it.gurzu.swam.iLib.model;

import java.util.ArrayList;
import java.util.List;

public class User extends BaseEntity {

	private String name;
	private String surname;
	private String email;
	private String password;
	private String address;
	private String telephoneNumber;
    private UserRole role;	
	private List<Notification> notifications;
	
	
	User(){}
	
	public User(String uuid) {
		super(uuid);
		this.notifications = new ArrayList<Notification>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSurname() {
		return surname;
	}
	
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getTelephoneNumber() {
		return telephoneNumber;
	}
	
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	
	public List<Notification> getNotifications() {
		return notifications;
	}
	
	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}
	
}
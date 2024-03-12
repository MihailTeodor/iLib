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
	
	
	User(){}
	
	public User(String uuid) {
		super(uuid);
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
	
	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}
	
}
package it.gurzu.swam.iLib.dto;

import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.utils.PasswordUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserDTO {
	
	private Long id;

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String plainPassword;

    @NotEmpty(message = "Name is required")
    private String name;

    @NotEmpty(message = "Surname is required")
    private String surname;

    private String address;

    @NotEmpty(message = "Telephone number is required")
    @Size(min = 10, max = 10, message = "The Telephone Number must be 10 characters long")
    private String telephoneNumber;

    public UserDTO() {}
    
    public UserDTO(User user) {
    	this.id = user.getId();
    	this.email = user.getEmail();
    	this.plainPassword = null;
    	this.name = user.getName();
    	this.surname = user.getSurname();
    	this.address = user.getAddress();
    	this.telephoneNumber = user.getTelephoneNumber();
    }
    
    public User toEntity() {
    	User user = ModelFactory.user();
    	
    	if(this.plainPassword != null)
    		user.setPassword(PasswordUtils.hashPassword(this.plainPassword));

    	user.setEmail(this.email);
    	user.setName(this.name);
    	user.setSurname(this.surname);
    	user.setAddress(this.address);
    	user.setTelephoneNumber(this.telephoneNumber);
    	
    	return user;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPlainPassword() {
		return plainPassword;
	}

	public void setPlainPassword(String plainPassword) {
		this.plainPassword = plainPassword;
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
    
}

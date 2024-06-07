package it.gurzu.swam.iLib.dto;

import java.util.List;
import java.util.stream.Collectors;

import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.User;

public class UserDashboardDTO {

	private Long id;
    private String name;
    private String surname;
    private String email;
    private String address;
    private String telephoneNumber;
    
    private List<BookingDTO> bookings;
    private List<LoanDTO> loans;
    
    protected UserDashboardDTO() {}
    
    public UserDashboardDTO(User user, List<Booking> bookings, List<Loan> loans) {
    	this.id = user.getId();
    	this.name = user.getName();
    	this.surname = user.getSurname();
    	this.email = user.getEmail();
    	this.address = user.getAddress();
    	this.telephoneNumber = user.getTelephoneNumber();
    	
    	if(bookings != null && bookings.get(0) != null)
    		this.bookings = bookings.stream()
    							.map(BookingDTO::new)
    							.collect(Collectors.toList());
    	
    	if(loans != null && loans.get(0) != null)
    		this.loans = loans.stream()
    					  .map(LoanDTO::new)
    					  .collect(Collectors.toList());
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	public List<LoanDTO> getLoans() {
		return loans;
	}
	public void setLoans(List<LoanDTO> loans) {
		this.loans = loans;
	}
	public List<BookingDTO> getBookings() {
		return bookings;
	}
	public void setBookings(List<BookingDTO> bookings) {
		this.bookings = bookings;
	}    
}

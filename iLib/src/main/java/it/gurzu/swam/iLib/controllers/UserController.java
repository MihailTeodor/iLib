package it.gurzu.swam.iLib.controllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.UserDTO;
import it.gurzu.swam.iLib.dto.UserDashboardDTO;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;
import it.gurzu.swam.iLib.utils.PasswordUtils;
import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Model
@Transactional
public class UserController {

	@Inject
	private UserDao userDao;
	
	@Inject
	private BookingController bookingController;
	
	@Inject
	private LoanController loanController;
	
	
	public Long addUser(UserDTO userDTO) {
		User tmpUser = null;
		try {
			tmpUser = userDao.findUsersByEmail(userDTO.getEmail()); 
		} catch (Exception e) {
			tmpUser = null;
		}
		if(tmpUser != null)
			throw new IllegalArgumentException("Email already registered!");
		
		if(userDTO.getPlainPassword() == null)
			throw new IllegalArgumentException("Password is required!");
		
		User userToAdd = userDTO.toEntity();
		userToAdd.setRole(UserRole.CITIZEN);

		userDao.save(userToAdd);
		
		return userToAdd.getId();
	}

	
	public void updateUser(Long id, UserDTO userDTO) {
		User userToUpdate = userDao.findById(id);
		
		if(userToUpdate == null)
			throw new UserDoesNotExistException("User does not exist!");
		
		if(userDTO.getPlainPassword() != null)
			userToUpdate.setPassword(PasswordUtils.hashPassword(userDTO.getPlainPassword()));
		
		userToUpdate.setEmail(userDTO.getEmail());
		userToUpdate.setName(userDTO.getName());
		userToUpdate.setSurname(userDTO.getSurname());
		userToUpdate.setAddress(userDTO.getAddress());
		userToUpdate.setTelephoneNumber(userDTO.getTelephoneNumber());
		
		userDao.save(userToUpdate);
	}
	

	public List<User> searchUsers(String email, String name, String surname, String telephoneNumber, int fromIndex, int limit) {
		List<User> retrievedUsers = Collections.emptyList();
		try {
			if(email != null) {
				retrievedUsers = Arrays.asList(userDao.findUsersByEmail(email));
			}
			else {
				retrievedUsers = userDao.findUsers(name, surname, telephoneNumber, fromIndex, limit);
			}			
		}catch (Exception e) {
			retrievedUsers = Collections.emptyList();
		}
		if(retrievedUsers.isEmpty())
			throw new SearchHasGivenNoResultsException("Search has given no results!");
		return retrievedUsers;
	}
	
	
	public Long countUsers(String email, String name, String surname, String telephoneNumber) {
			return email == null ? userDao.countUsers(name, surname, telephoneNumber) : 1;
	}
	
	
	public UserDashboardDTO getUserInfoExtended(Long id) {
		User user = userDao.findById(id);
		
		if(user == null)
			throw new UserDoesNotExistException("User does not exist!");
		
		List<Booking> bookings;
		try {
			bookings = bookingController.getBookingsByUser(user.getId(), 0, 5);			
		} catch (Exception e) {
			bookings = null;
		}
	
		List<Loan> loans;
		try {
			loans = loanController.getLoansByUser(user.getId(), 0, 5);
		} catch (Exception e) {
			loans = null;
		}
		
		return new UserDashboardDTO(user, bookings, loans);
	}
}
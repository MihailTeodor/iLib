package it.gurzu.swam.iLib.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@RequestScoped
@Transactional
public class UserService {

	@Inject
	private UserDao userDao;
	
	@Inject
	private BookingService bookingService;
	
	@Inject
	private LoanService loanService;
	
	
	public Long addUser(UserDTO userDTO) {
		User tmpUser = null;
		try {
			tmpUser = userDao.findUsersByEmail(userDTO.getEmail()); 
		} catch (Exception e) {
			tmpUser = null;
		}
		if(tmpUser != null)
			throw new IllegalArgumentException("Email already registered!");
		
		if(StringUtils.isBlank(userDTO.getPlainPassword()))
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
		
		if(!(StringUtils.isBlank(userDTO.getPlainPassword())))
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
			if(!(StringUtils.isBlank(email))) {
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
			return StringUtils.isBlank(email) ? userDao.countUsers(name, surname, telephoneNumber) : 1;
	}
	
	
	public UserDashboardDTO getUserInfoExtended(Long id) {
		User user = userDao.findById(id);
		
		if(user == null)
			throw new UserDoesNotExistException("User does not exist!");
		
		List<Booking> bookings;
		try {
			bookings = bookingService.getBookingsByUser(user.getId(), 0, 5);			
		} catch (Exception e) {
			bookings = null;
		}
	
		List<Loan> loans;
		try {
			loans = loanService.getLoansByUser(user.getId(), 0, 5);
		} catch (Exception e) {
			loans = null;
		}
		
		Long totalBookings = bookingService.countBookingsByUser(id);
		Long totalLoans = loanService.countLoansByUser(id);
		return new UserDashboardDTO(user, bookings, loans, totalBookings, totalLoans);
	}
}
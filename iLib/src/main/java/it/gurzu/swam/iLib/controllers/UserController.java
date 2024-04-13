package it.gurzu.swam.iLib.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.gurzu.swam.iLib.Utils.PasswordUtils;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.ModelFactory;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;
import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;

@Model
public class UserController {

	@Inject
	private UserDao userDao;
	
	public void addUser(String email, String plainPassword, String name, String surname, String address, String telephoneNumber) {
		if(email == null)
			throw new IllegalArgumentException("Email missing!");
		
		if(plainPassword == null)
			throw new IllegalArgumentException("Password missing!");
		
		User tmpUser = userDao.findUsersByEmail(email); 
		
		if(tmpUser != null)
			throw new IllegalArgumentException("Email already registered!");
		
		User userToAdd = ModelFactory.user();
		userToAdd.setName(name);
		userToAdd.setSurname(surname);
		userToAdd.setEmail(email);
		userToAdd.setPassword(PasswordUtils.hashPassword(plainPassword));
		userToAdd.setAddress(address);
		userToAdd.setTelephoneNumber(telephoneNumber);
		userToAdd.setRole(UserRole.CITIZEN);

		userDao.save(userToAdd);
	}
	
	public void updateUser(Long userId, String name, String surname, String email, String plainPassword, String address, String telephoneNumber) {
		User userToUpdate = userDao.findById(userId);
		
		if(userToUpdate == null)
			throw new UserDoesNotExistException("User does not exist!");
		
		userToUpdate.setName(name);
		userToUpdate.setSurname(surname);
		userToUpdate.setEmail(email);
		userToUpdate.setPassword(PasswordUtils.hashPassword(plainPassword));
		userToUpdate.setAddress(address);
		userToUpdate.setTelephoneNumber(telephoneNumber);
		
		userDao.save(userToUpdate);
	}
	
	public User getUserInfo(Long userId) {
		User user = userDao.findById(userId);
		
		if(user == null)
			throw new UserDoesNotExistException("User does not exist!");
		return user;
	}

	public List<User> searchUsers(String email, String name, String surname, String telephoneNumber) {
		List<User> retrievedUsers = new ArrayList<User>();
		if(email != null) {
			retrievedUsers = Arrays.asList(userDao.findUsersByEmail(email));
		}
		else {
			retrievedUsers = userDao.findUsers(name, surname, telephoneNumber);
		}
		if(retrievedUsers == null || retrievedUsers.get(0) == null)
			throw new SearchHasGivenNoResultsException("Search has given no results!");
		return retrievedUsers;
	}
}
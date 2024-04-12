package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import it.gurzu.swam.iLib.controllers.UserController;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.User;

public class UserControllerTest {
	private UserController userController;
	private UserDao userDaoMock;
	private User userMock;
	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		userController = new UserController();
		userDaoMock = mock(UserDao.class);
		userMock = mock(User.class);
		
		FieldUtils.writeField(userController, "userDao", userDaoMock, true);
	}
	
	@Test
	public void testAddUser_WhenEmailIsNull_ThrowsIllegalArgumentException() {
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userController.addUser(null, "password", "name", "surname", "address", "telNumber");
		});
		assertEquals("Email missing!", thrownException.getMessage());	

	}
	
	@Test
	public void testAddUser_WhenPasswordIsMissing_ThrowsIllegalArgumentException() {
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userController.addUser("email", null, "name", "surname", "address", "telNumber");
		});
		assertEquals("Password missing!", thrownException.getMessage());	

	}
	
	@Test
	public void testAddUser_WhenEmailAlreadyRegistered_ThrowsIllegalArgumentException() {
		when(userDaoMock.findUsersByEmail("email")).thenReturn(userMock);
		
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userController.addUser("email", "password", null, null, null, null);
		});
		assertEquals("Email already registered!", thrownException.getMessage());	
	}
	
	@Test
	public void testAddUserSuccessful() {
		when(userDaoMock.findUsersByEmail("inexistingEmail")).thenReturn(null);
		
		userController.addUser("inexistingEmail", "password", null, null, null, null);
		verify(userDaoMock).save(Mockito.any(User.class));
	}
	
	@Test
	public void testUpdateUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			userController.updateUser(1L, null, null, null, null, null, null);
		});
		assertEquals("User does not exist!", thrownException.getMessage());	
	}

	@Test
	public void testUpdateUser_WhenUserDoesExist() {
		when(userDaoMock.findById(1L)).thenReturn(userMock);
		
		userController.updateUser(1L, null, null, null, null, null, null);
		
		verify(userDaoMock).save(userMock);
	}
	
	@Test
	public void testGetUserInfo_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			userController.getUserInfo(1L);
		});
		assertEquals("User does not exist!", thrownException.getMessage());	
		
	}
	
	@Test
	public void testGetUserInfo_WhenUserExists() {
		when(userDaoMock.findById(1L)).thenReturn(userMock);
		
		User retrievedUser = userController.getUserInfo(1L);
		
		assertEquals(userMock, retrievedUser);
		assertEquals(null, retrievedUser.getPassword());
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNotNull_PerformsFindUsersByEmail() {
		when(userDaoMock.findUsersByEmail("email")).thenReturn(userMock);
	
		userController.searchUsers("email", null, null, null);
		
		verify(userDaoMock).findUsersByEmail("email");
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNotNullAndNoResults_ThrowsSearchHasGivenNoResultsException() {
		when(userDaoMock.findUsersByEmail("email")).thenReturn(null);
		
		Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, ()->{
			userController.searchUsers("email", null, null, null);
		});
		assertEquals("Search has given no results!", thrownException.getMessage());	
	}

	@Test
	public void testSearchUsers_WhenEmailIsNull_PerformsNormalSearch() {
		List<User> retrievedUsers = new ArrayList<User>();
		retrievedUsers.add(userMock);
		when(userDaoMock.findUsers(null, null, null)).thenReturn(retrievedUsers);
		
		userController.searchUsers(null, null, null, null);
		
		verify(userDaoMock, never()).findUsersByEmail(Mockito.anyString());
		verify(userDaoMock).findUsers(null, null, null);
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNullAndNoResults_ThrowsSearchHasGivenNoResultsException() {
		List<User> retrievedUsers = new ArrayList<User>();
		retrievedUsers.add(userMock);
		when(userDaoMock.findUsers(null, null, null)).thenReturn(null);

		Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, ()->{
			userController.searchUsers(null, null, null, null);
		});
		assertEquals("Search has given no results!", thrownException.getMessage());	
		
	}
}
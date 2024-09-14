package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import it.gurzu.swam.iLib.controllers.BookingController;
import it.gurzu.swam.iLib.controllers.LoanController;
import it.gurzu.swam.iLib.controllers.UserController;
import it.gurzu.swam.iLib.dao.UserDao;
import it.gurzu.swam.iLib.dto.UserDTO;
import it.gurzu.swam.iLib.dto.UserDashboardDTO;
import it.gurzu.swam.iLib.exceptions.SearchHasGivenNoResultsException;
import it.gurzu.swam.iLib.exceptions.UserDoesNotExistException;
import it.gurzu.swam.iLib.model.Article;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;

public class UserControllerTest {
	private UserController userController;
	private UserDao userDaoMock;
	private User userMock;
	private BookingController bookingControllerMock;
	private LoanController loanControllerMock;
	
	@BeforeEach
	public void setup() throws IllegalAccessException {
		userController = new UserController();
		userDaoMock = mock(UserDao.class);
		userMock = mock(User.class);
		bookingControllerMock = mock(BookingController.class);
		loanControllerMock = mock(LoanController.class);
		
		FieldUtils.writeField(userController, "userDao", userDaoMock, true);
		FieldUtils.writeField(userController, "bookingController", bookingControllerMock, true);
		FieldUtils.writeField(userController, "loanController", loanControllerMock, true);
	}
	
	@Test
	public void testAddUser_WhenEmailAlreadyRegistered_ThrowsIllegalArgumentException() {
		UserDTO userDTO = mock(UserDTO.class);
		when(userDTO.getEmail()).thenReturn("email");
		when(userDaoMock.findUsersByEmail("email")).thenReturn(userMock);
		
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userController.addUser(userDTO);
		});
		assertEquals("Email already registered!", thrownException.getMessage());	
	}

	@Test
	public void testAddUser_WhenPasswordMissing_ThrowsIllegalArgumentException() {
		UserDTO userDTO = mock(UserDTO.class);
		when(userDTO.getEmail()).thenReturn("email");
		when(userDTO.getPlainPassword()).thenReturn(null);
		
		when(userDaoMock.findUsersByEmail("email")).thenReturn(null);
		
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			userController.addUser(userDTO);
		});
		assertEquals("Password is required!", thrownException.getMessage());	
	}

	
	@Test
	public void testAddUserSuccessful() {
		when(userDaoMock.findUsersByEmail("inexistingEmail")).thenReturn(null);
		
		UserDTO userDTO = new UserDTO();
		userDTO.setName("name");
		userDTO.setSurname("surname");
		userDTO.setEmail("email");
		userDTO.setPlainPassword("password");
		userDTO.setAddress("address");
		userDTO.setTelephoneNumber("1234567890");
		
	    userController.addUser(userDTO);
	    
	    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
	    verify(userDaoMock).save(userCaptor.capture());
	    
	    User savedUser = userCaptor.getValue();
	   
	    assertEquals(userDTO.getName(), savedUser.getName());
	    assertEquals(userDTO.getSurname(), savedUser.getSurname());
	    assertEquals(userDTO.getEmail(), savedUser.getEmail());
	    assertTrue(new BCryptPasswordEncoder().matches(userDTO.getPlainPassword(), savedUser.getPassword()));
	    assertEquals(userDTO.getAddress(), savedUser.getAddress());
	    assertEquals(userDTO.getTelephoneNumber(), savedUser.getTelephoneNumber());
	    assertEquals(UserRole.CITIZEN, savedUser.getRole());
	}
	
	@Test
	public void testUpdateUser_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
		UserDTO userDTO = new UserDTO();
		when(userDaoMock.findById(1L)).thenReturn(null);
		
		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			userController.updateUser(1L, userDTO);
		});
		assertEquals("User does not exist!", thrownException.getMessage());	
	}

	@Test
	public void testUpdateUser_WhenUserDoesExist() {
		UserDTO userDTO = new UserDTO();
		
		userDTO.setEmail("new email");
		userDTO.setPlainPassword("new plain password");
		userDTO.setName("new name");
		userDTO.setSurname("new surname");
		userDTO.setAddress("new address");
		userDTO.setTelephoneNumber("0987654321");
		
	    User existingUser = mock(User.class);
	    when(userDaoMock.findById(1L)).thenReturn(existingUser);

	    userController.updateUser(1L, userDTO);
	    
	    verify(userDaoMock).save(existingUser);
	    verify(existingUser).setEmail(userDTO.getEmail());
	    verify(existingUser).setName(userDTO.getName());
	    verify(existingUser).setSurname(userDTO.getSurname());
	    verify(existingUser).setAddress(userDTO.getAddress());
	    verify(existingUser).setTelephoneNumber(userDTO.getTelephoneNumber());

	    ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(existingUser).setPassword(passwordCaptor.capture());

        String capturedPassword = passwordCaptor.getValue();
        assertTrue(new BCryptPasswordEncoder().matches(userDTO.getPlainPassword(), capturedPassword));
        
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNotNull_PerformsFindUsersByEmail() {
		when(userDaoMock.findUsersByEmail("email")).thenReturn(userMock);
	
		userController.searchUsers("email", null, null, null, 0, 0);
		
		verify(userDaoMock).findUsersByEmail("email");
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNotNullAndNoResults_ThrowsSearchHasGivenNoResultsException() {
        when(userDaoMock.findUsersByEmail(anyString())).thenThrow(new RuntimeException());
		
		Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, ()->{
			userController.searchUsers("non existing email", null, null, null, 0, 0);
		});
		assertEquals("Search has given no results!", thrownException.getMessage());	
	}

	@Test
	public void testSearchUsers_WhenEmailIsNull_PerformsNormalSearch() {
		List<User> retrievedUsers = new ArrayList<User>();
		retrievedUsers.add(userMock);
		when(userDaoMock.findUsers(null, null, null, 0, 0)).thenReturn(retrievedUsers);
		
		userController.searchUsers(null, null, null, null, 0, 0);
		
		verify(userDaoMock, never()).findUsersByEmail(Mockito.anyString());
		verify(userDaoMock).findUsers(null, null, null, 0, 0);
	}
	
	@Test
	public void testSearchUsers_WhenEmailIsNullAndNoResults_ThrowsSearchHasGivenNoResultsException() {
		List<User> retrievedUsers = new ArrayList<User>();
		retrievedUsers.add(userMock);
		when(userDaoMock.findUsers(null, null, null, 0, 0)).thenReturn(Collections.emptyList());

		Exception thrownException = assertThrows(SearchHasGivenNoResultsException.class, ()->{
			userController.searchUsers(null, null, null, null, 0 ,0);
		});
		assertEquals("Search has given no results!", thrownException.getMessage());	
		
	}

    @Test
    public void testCountUsersWithValidParameters() {
    	userController.countUsers(null, "John", "Snow", "123456789");

    	verify(userDaoMock).countUsers("John", "Snow", "123456789");
    }

    @Test
    public void testCountUsersWithEmailParameterNotNull() {
    	Long count = userController.countUsers("email@example.com", "John", "Snow", "123456789");

    	verify(userDaoMock, never()).countUsers("John", "Snow", "123456789");
    	assertEquals(1, count);
    }
	
	@Test
	public void testGetUserInfoExtended_WhenUserDoesNotExist_ThrowsUserDoesNotExistException() {
	    when(userDaoMock.findById(1L)).thenReturn(null);

		Exception thrownException = assertThrows(UserDoesNotExistException.class, ()->{
			userController.getUserInfoExtended(1L);
		});
		assertEquals("User does not exist!", thrownException.getMessage());	
	}

    @Test
    public void testGetUserInfoExtended_WhenUserExists_ReturnsUserDTO() {
        User mockUser = mock(User.class);
        Article mockArticle = mock(Article.class);
        Booking mockBooking = mock(Booking.class);
        Loan mockLoan = mock(Loan.class);

        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getName()).thenReturn("Mihail");
        when(mockUser.getSurname()).thenReturn("Teodor");
        when(mockUser.getEmail()).thenReturn("email");
        when(mockUser.getAddress()).thenReturn("address");
        when(mockUser.getTelephoneNumber()).thenReturn("1234567890");
        
        when(mockBooking.getBookedArticle()).thenReturn(mockArticle);
        when(mockBooking.getBookingUser()).thenReturn(mockUser);
        when(mockBooking.getId()).thenReturn(10L); // Example ID for the booking
        
        when(mockLoan.getArticleOnLoan()).thenReturn(mockArticle);
        when(mockLoan.getLoaningUser()).thenReturn(mockUser);
        when(mockLoan.getId()).thenReturn(20L); // Example ID for the loan

        when(mockArticle.getId()).thenReturn(2L);
        
        when(userDaoMock.findById(1L)).thenReturn(mockUser);
        when(bookingControllerMock.getBookingsByUser(1L, 0, 5)).thenReturn(Collections.singletonList(mockBooking));
        when(loanControllerMock.getLoansByUser(1L, 0, 5)).thenReturn(Collections.singletonList(mockLoan));
        when(bookingControllerMock.countBookingsByUser(1L)).thenReturn(1L);
        when(loanControllerMock.countLoansByUser(1L)).thenReturn(1L);

        UserDashboardDTO result = userController.getUserInfoExtended(1L);

        assertNotNull(result);
        assertEquals(mockUser.getName(), result.getName());
        assertEquals(mockUser.getSurname(), result.getSurname());
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getAddress(), result.getAddress());
        assertEquals(mockUser.getTelephoneNumber(), result.getTelephoneNumber());
        assertEquals(1, result.getBookings().size());
        assertEquals(Long.valueOf(10L), result.getBookings().get(0).getId());
        
        assertEquals(1, result.getLoans().size());
        assertEquals(Long.valueOf(20L), result.getLoans().get(0).getId());

        assertEquals(1L, result.getTotalBookings());
        assertEquals(1L, result.getTotalLoans());
    }

}
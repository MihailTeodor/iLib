package it.gurzu.SWAM.iLib.controllerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.gurzu.swam.iLib.dto.UserDTO;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;

public class UserControllerTest extends ControllerTest {

	private User adminUser;
	private User citizenUser;
	private String adminToken;
	private String citizenToken;

	private RequestSpecification request;
	private Response response;
	private String body;
	private JsonObject responseBody;


	@Override
	protected void beforeEachInit() throws SQLException, IllegalAccessException {
		setBaseURL("/iLib/v1/usersEndpoint");

		QueryUtils.queryTruncateAll(connection);

		adminUser = QueryUtils.queryCreateUser(connection, 10L, "admin@example.com", "admin password", "adminName",
				"adminSurname", "adminAddress", "adminTelephoneNumber", UserRole.ADMINISTRATOR);
		adminToken = AuthHelper.getAuthToken("admin@example.com", "admin password");

		citizenUser = QueryUtils.queryCreateUser(connection, 20L, "user@Email.com", "user password", "name", "surname",
				"address", "123432", UserRole.CITIZEN);
		citizenToken = AuthHelper.getAuthToken("user@Email.com", "user password");

	}

	@Test
	public void testGetUser_WhereRequestingUserIsAdmin_CanRequestInfo()
			throws IllegalAccessException, SQLException {

		Book book = QueryUtils.queryCreateBook(connection, 1L, "upstairs", "cujo", LocalDate.now(), "publisher",
				"horror", "a nice book", ArticleState.BOOKED, "King", "isbn");
		Booking booking = QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3),
				BookingState.ACTIVE, book, citizenUser);

		request = RestAssured.given();
		request.header("Authorization", "Bearer" + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("userId", 20L);

		response = executeGet(request, "/{userId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals(responseBody.get("id").getAsLong(), citizenUser.getId());
		assertEquals(responseBody.get("email").getAsString(), citizenUser.getEmail());
		assertEquals(responseBody.get("name").getAsString(), citizenUser.getName());
		assertEquals(responseBody.get("surname").getAsString(), citizenUser.getSurname());
		assertEquals(responseBody.get("address").getAsString(), citizenUser.getAddress());
		assertEquals(responseBody.get("telephoneNumber").getAsString(), citizenUser.getTelephoneNumber());

		JsonArray bookingsArray = responseBody.get("bookings").getAsJsonArray();
		assertEquals(bookingsArray.size(), 1);

		JsonObject bookingObject = bookingsArray.get(0).getAsJsonObject();
		assertEquals(bookingObject.get("id").getAsLong(), booking.getId());
		assertEquals(bookingObject.get("bookedArticleId").getAsLong(), booking.getBookedArticle().getId());
		assertEquals(bookingObject.get("bookedArticleTitle").getAsString(), booking.getBookedArticle().getTitle());
		assertEquals(bookingObject.get("bookingUserId").getAsLong(), booking.getBookingUser().getId());
		assertEquals(bookingObject.get("state").getAsString(), booking.getState().toString());

		JsonArray bookingDateArray = bookingObject.get("bookingDate").getAsJsonArray();
		LocalDate bookingDateResponse = LocalDate.of(bookingDateArray.get(0).getAsInt(),
				bookingDateArray.get(1).getAsInt(), bookingDateArray.get(2).getAsInt());
		assertEquals(bookingDateResponse, booking.getBookingDate());

		JsonArray bookingEndDateArray = bookingObject.get("bookingEndDate").getAsJsonArray();
		LocalDate bookingEndDateResponse = LocalDate.of(bookingEndDateArray.get(0).getAsInt(),
				bookingEndDateArray.get(1).getAsInt(), bookingEndDateArray.get(2).getAsInt());
		assertEquals(bookingEndDateResponse, booking.getBookingEndDate());

		assertTrue(responseBody.get("loans").isJsonNull());
		
		assertEquals(responseBody.get("totalBookings").getAsLong(), 1L);
		assertEquals(responseBody.get("totalLoans").getAsLong(), 0L);
	}

	@Test
	public void testGetUser_WhereRequestingUserIsSameUser_CanRequestInfo()
			throws IllegalAccessException, SQLException {

		Book book = QueryUtils.queryCreateBook(connection, 1L, "upstairs", "cujo", LocalDate.now(), "publisher",
				"horror", "a nice book", ArticleState.BOOKED, "King", "isbn");
		Booking booking = QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3),
				BookingState.ACTIVE, book, citizenUser);

		request = RestAssured.given();
		request.header("Authorization", "Bearer" + citizenToken);
		request.header("Content-type", "application/json");
		request.pathParam("userId", 20L);

		response = executeGet(request, "/{userId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals(responseBody.get("id").getAsLong(), citizenUser.getId());
		assertEquals(responseBody.get("email").getAsString(), citizenUser.getEmail());
		assertEquals(responseBody.get("name").getAsString(), citizenUser.getName());
		assertEquals(responseBody.get("surname").getAsString(), citizenUser.getSurname());
		assertEquals(responseBody.get("address").getAsString(), citizenUser.getAddress());
		assertEquals(responseBody.get("telephoneNumber").getAsString(), citizenUser.getTelephoneNumber());

		JsonArray bookingsArray = responseBody.get("bookings").getAsJsonArray();
		assertEquals(bookingsArray.size(), 1);

		JsonObject bookingObject = bookingsArray.get(0).getAsJsonObject();
		assertEquals(bookingObject.get("id").getAsLong(), booking.getId());
		assertEquals(bookingObject.get("bookedArticleId").getAsLong(), booking.getBookedArticle().getId());
		assertEquals(bookingObject.get("bookedArticleTitle").getAsString(), booking.getBookedArticle().getTitle());
		assertEquals(bookingObject.get("bookingUserId").getAsLong(), booking.getBookingUser().getId());
		assertEquals(bookingObject.get("state").getAsString(), booking.getState().toString());

		JsonArray bookingDateArray = bookingObject.get("bookingDate").getAsJsonArray();
		LocalDate bookingDateResponse = LocalDate.of(bookingDateArray.get(0).getAsInt(),
				bookingDateArray.get(1).getAsInt(), bookingDateArray.get(2).getAsInt());
		assertEquals(bookingDateResponse, booking.getBookingDate());

		JsonArray bookingEndDateArray = bookingObject.get("bookingEndDate").getAsJsonArray();
		LocalDate bookingEndDateResponse = LocalDate.of(bookingEndDateArray.get(0).getAsInt(),
				bookingEndDateArray.get(1).getAsInt(), bookingEndDateArray.get(2).getAsInt());
		assertEquals(bookingEndDateResponse, booking.getBookingEndDate());

		assertTrue(responseBody.get("loans").isJsonNull());
		
		assertEquals(responseBody.get("totalBookings").getAsLong(), 1L);
		assertEquals(responseBody.get("totalLoans").getAsLong(), 0L);
	}

	
	@Test
	public void testGetUserInfo_WhenUserNotAdminAndDifferentId_ReturnsUnauthorizedResponse()
			throws IllegalAccessException, SQLException {

		request = RestAssured.given();
		request.header("Authorization", "Bearer" + citizenToken);
		request.header("Content-type", "application/json");
		request.pathParam("userId", 10L);

		response = executeGet(request, "/{userId}");

		response.then().statusCode(403);
	}

	@Test
	public void testGetUserInfo_WhenRequestedUserDoesNotExist_ReturnsNotFoundResponse() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer" + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("userId", 7L);

		response = executeGet(request, "/{userId}");

		response.then().statusCode(404);
		
		body = response.getBody().asString();

		assertTrue(body.contains("User does not exist!"));
	}

	@Test
	public void testCreateUser_WhenRoleIsNotAdministrator_ReturnsForbiddenResponse() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer" + citizenToken);
		request.header("Content-type", "application/json");

		response = executePost(request, "");

		response.then().statusCode(403);
		
		body = response.getBody().asString();

		assertEquals("Access forbidden: role not allowed", body);
	}
	

	@Test
	public void testCreateUser_WhenRoleIsAdministrator() {
		UserDTO userDTO = new UserDTO();
		userDTO.setEmail("newuser@example.com");
		userDTO.setPlainPassword("password123");
		userDTO.setName("New");
		userDTO.setSurname("User");
		userDTO.setAddress("New Address");
		userDTO.setTelephoneNumber("1234567890");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.body(userDTO);

		response = executePost(request, "/");

		response.then().statusCode(201).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertTrue(responseBody.has("userId"));
	}
	
	
	   @Test
	    public void testCreateUser_WhenEmailAlreadyExists_ReturnsBadRequestResponse() throws SQLException, IllegalAccessException {
	        UserDTO userDTO = new UserDTO();
	        userDTO.setEmail(citizenUser.getEmail());
	        userDTO.setPlainPassword("password123");
	        userDTO.setName("New");
	        userDTO.setSurname("User");
	        userDTO.setAddress("New Address");
	        userDTO.setTelephoneNumber("1234567890");

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.body(userDTO);

	        response = executePost(request, "/");

	        response.then().statusCode(400).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("Email already registered!", responseBody.get("error").getAsString());
	    }

	   @ParameterizedTest
	   @MethodSource("provideInvalidUserData")
	   public void testCreateUser_InvalidData(UserDTO userDTO, String expectedErrorMessage) {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.body(userDTO);

	        response = executePost(request, "/");

	        response.then().statusCode(400).contentType("text/plain;charset=UTF-8");

	        body = response.getBody().asString();
	        assertTrue(body.contains(expectedErrorMessage));
	   }
	   

	    @Test
	    public void testCreateUser_MissingPassword() {
	        UserDTO userDTO = createUserDTO("email@asd.com", "name", "surname", "1234567890");

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.body(userDTO);

	        response = executePost(request, "/");

	        response.then().statusCode(400).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("Password is required!", responseBody.get("error").getAsString());
	    }

	    @Test
	    public void testUpdateUser_Success() {
	        UserDTO userDTO = new UserDTO();
	        userDTO.setEmail("updateduser@example.com");
	        userDTO.setPlainPassword("newpassword123");
	        userDTO.setName("Updated");
	        userDTO.setSurname("User");
	        userDTO.setAddress("Updated Address");
	        userDTO.setTelephoneNumber("0987654321");

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.pathParam("userId", citizenUser.getId());
	        request.body(userDTO);

	        response = executePut(request, "/{userId}");

	        response.then().statusCode(200).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("User updated successfully.", responseBody.get("message").getAsString());
	    }

	    @Test
	    public void testUpdateUser_Forbidden() {
	        UserDTO userDTO = new UserDTO();
	        userDTO.setEmail("updateduser@example.com");
	        userDTO.setPlainPassword("newpassword123");
	        userDTO.setName("Updated");
	        userDTO.setSurname("User");
	        userDTO.setAddress("Updated Address");
	        userDTO.setTelephoneNumber("0987654321");

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + citizenToken);
	        request.header("Content-type", "application/json");
	        request.pathParam("userId", adminUser.getId());
	        request.body(userDTO);

	        response = executePut(request, "/{userId}");

	        response.then().statusCode(403);
	    }

	    @Test
	    public void testUpdateUser_UserNotFound() {
	        UserDTO userDTO = new UserDTO();
	        userDTO.setEmail("updateduser@example.com");
	        userDTO.setPlainPassword("newpassword123");
	        userDTO.setName("Updated");
	        userDTO.setSurname("User");
	        userDTO.setAddress("Updated Address");
	        userDTO.setTelephoneNumber("0987654321");

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.pathParam("userId", 9999L);
	        request.body(userDTO);

	        response = executePut(request, "/{userId}");

	        response.then().statusCode(404).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("User does not exist!", responseBody.get("error").getAsString());
	    }
	
	    @ParameterizedTest
	    @MethodSource("provideInvalidUserData")
	    public void testUpdateUser_InvalidEmailFormat(UserDTO userDTO, String expectedErrorMessage) {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.pathParam("userId", citizenUser.getId());
	        request.body(userDTO);

	        response = executePut(request, "/{userId}");

	        response.then().statusCode(400).contentType("text/plain;charset=UTF-8");

	        body = response.getBody().asString();
	        assertTrue(body.contains(expectedErrorMessage));
	    }
	        
	    @Test
	    public void testSearchUsers_Success() {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("email", "user@Email.com");

	        response = executeGet(request, "/");

	        response.then().statusCode(200).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertTrue(responseBody.has("items"));
	        assertEquals(1, responseBody.get("items").getAsJsonArray().size());
	        JsonObject userObject = responseBody.get("items").getAsJsonArray().get(0).getAsJsonObject();
	      
	        assertEquals(citizenUser.getEmail(), userObject.get("email").getAsString());
	        assertEquals(citizenUser.getName(), userObject.get("name").getAsString());
	        assertEquals(citizenUser.getSurname(), userObject.get("surname").getAsString());
	        assertEquals(citizenUser.getAddress(), userObject.get("address").getAsString());
	        assertEquals(citizenUser.getTelephoneNumber(), userObject.get("telephoneNumber").getAsString());

	        assertEquals(1, responseBody.get("pageNumber").getAsInt());
	        assertEquals(10, responseBody.get("resultsPerPage").getAsInt());
	        assertEquals(1, responseBody.get("totalResults").getAsInt());
	        assertEquals(1, responseBody.get("totalPages").getAsInt());
	    }
	    
	    @Test
	    public void testSearchUsers_Unauthorized() {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + citizenToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("email", "user@Email.com");

	        response = executeGet(request, "/");

	        response.then().statusCode(403).contentType("text/html;charset=UTF-8");
	    }

	    @Test
	    public void testSearchUsers_InvalidPaginationParameters() {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("pageNumber", 0); // Invalid page number

	        response = executeGet(request, "/");

	        response.then().statusCode(400).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("Pagination parameters incorrect!", responseBody.get("error").getAsString());
	    }

	    @Test
	    public void testSearchUsers_NoResults() {
	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("email", "nonexistent@Email.com");

	        response = executeGet(request, "/");

	        response.then().statusCode(404).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertEquals("Search has given no results!", responseBody.get("error").getAsString());
	    }

	    @Test
	    public void testSearchUsers_MultipleResults() throws IllegalAccessException, SQLException {
	    	
	        QueryUtils.queryCreateUser(connection, 30L, "user1@Email.com", "user password", "Alice", "Brown", "Address1", "1234321", UserRole.CITIZEN);
	        QueryUtils.queryCreateUser(connection, 40L, "user2@Email.com", "user password", "Bob", "Johnson", "Address2", "1234322", UserRole.CITIZEN);
	        QueryUtils.queryCreateUser(connection, 50L, "user3@Email.com", "user password", "Charlie", "Brown", "Address3", "1234323", UserRole.CITIZEN);

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("surname", "Brown");

	        response = executeGet(request, "/");

	        response.then().statusCode(200).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertTrue(responseBody.has("items"));
	        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
	        assertEquals(2, itemsArray.size());

	        for (int i = 0; i < itemsArray.size(); i++) {
	            JsonObject userObject = itemsArray.get(i).getAsJsonObject();
	            assertEquals(userObject.get("surname").getAsString(), "Brown");
	        }
	        
	        assertEquals(1, responseBody.get("pageNumber").getAsInt());
	        assertEquals(10, responseBody.get("resultsPerPage").getAsInt());
	        assertTrue(responseBody.get("totalResults").getAsInt() == 2);
	        assertTrue(responseBody.get("totalPages").getAsInt() == 1);
	    }

	    @Test
	    public void testSearchUsers_Pagination() throws IllegalAccessException, SQLException {
	        QueryUtils.queryCreateUser(connection, 30L, "user1@Email.com", "user password", "Alice", "Brown", "Address1", "1234321", UserRole.CITIZEN);
	        QueryUtils.queryCreateUser(connection, 40L, "user2@Email.com", "user password", "Bob", "Johnson", "Address2", "1234322", UserRole.CITIZEN);
	        QueryUtils.queryCreateUser(connection, 50L, "user3@Email.com", "user password", "Charlie", "Brown", "Address3", "1234323", UserRole.CITIZEN);

	        request = RestAssured.given();
	        request.header("Authorization", "Bearer " + adminToken);
	        request.header("Content-type", "application/json");
	        request.queryParam("resultsPerPage", 2);
	        request.queryParam("pageNumber", 2);

	        response = executeGet(request, "/");

	        response.then().statusCode(200).contentType("application/json");

	        body = response.getBody().asString();
	        responseBody = JsonParser.parseString(body).getAsJsonObject();

	        assertTrue(responseBody.has("items"));

	        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
	        assertEquals(2, itemsArray.size());

	        // first page contains adminUser and charlie; the third page would contain the citizenUser
	        assertEquals("user2@Email.com", itemsArray.get(0).getAsJsonObject().get("email").getAsString());
	        assertEquals("user3@Email.com", itemsArray.get(1).getAsJsonObject().get("email").getAsString());

	        assertEquals(2, responseBody.get("pageNumber").getAsInt());
	        assertEquals(2, responseBody.get("resultsPerPage").getAsInt());
	        assertTrue(responseBody.get("totalResults").getAsInt() == 5);
	        assertTrue(responseBody.get("totalPages").getAsInt() == 3);
	    }
	    
	    private static Stream<Arguments> provideInvalidUserData(){
	    	return Stream.of(
	    			Arguments.of(createUserDTO(null,  "name", "surname", "1234567890"), "Email is required"),
	    			Arguments.of(createUserDTO("email.com", "name", "surname", "1234567890"), "Invalid email format"),
	    			Arguments.of(createUserDTO("email@test.com", null, "surname", "1234567890"), "Name is required"),
	    			Arguments.of(createUserDTO("email@test.com", "name", null, "1234567890"), "Surname is required"),
	    			Arguments.of(createUserDTO("email@test.com", "name", "surname", null), "Telephone number is required"),
	    			Arguments.of(createUserDTO("email@test.com", "name", "surname", "123425"), "The Telephone Number must be 10 characters long")
	    			);
	    }
	    
	    private static UserDTO createUserDTO(String email, String name, String surname, String telephoneNumber) {
	    	UserDTO userDTO= new UserDTO();
	    	
	    	userDTO.setEmail(email);
	    	userDTO.setName(name);
	    	userDTO.setSurname(surname);
	    	userDTO.setTelephoneNumber(telephoneNumber);
	    	
	    	return userDTO;
	    	
	    }
}

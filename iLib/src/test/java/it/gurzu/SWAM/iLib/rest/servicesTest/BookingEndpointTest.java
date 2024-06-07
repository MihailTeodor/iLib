package it.gurzu.SWAM.iLib.rest.servicesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.Booking;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;

public class BookingEndpointTest extends ServiceTest {
	private User citizenUser;
	private String adminToken;
	private String citizenToken;

	private RequestSpecification request;
	private Response response;
	private String body;
	private JsonObject responseBody;

	@Override
	protected void beforeEachInit() throws SQLException, IllegalAccessException {
		setBaseURL("/iLib/v1/bookingsEndpoint");

		QueryUtils.queryTruncateAll(connection);

		QueryUtils.queryCreateUser(connection, 1L, "admin@example.com", "admin password", "adminName",
				"adminSurname", "adminAddress", "adminTelephoneNumber", UserRole.ADMINISTRATOR);
		adminToken = AuthHelper.getAuthToken("admin@example.com", "admin password");

		citizenUser = QueryUtils.queryCreateUser(connection, 2L, "user@Email.com", "user password", "name", "surname",
				"address", "123432", UserRole.CITIZEN);
		citizenToken = AuthHelper.getAuthToken("user@Email.com", "user password");
	}

	@Test
	public void testRegisterBooking_Success() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction",
				"Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1L);
		request.queryParam("articleId", 1L);

		response = executePost(request, "/");

		response.then().statusCode(201).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertTrue(responseBody.has("bookingId"));
	}

	@Test
	public void testRegisterBooking_MissingUserId() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("articleId", 1);

		response = executePost(request, "/");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot register Booking, User not specified!", responseBody.get("error").getAsString());
	}

	@Test
	public void testRegisterBooking_MissingArticleId() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);

		response = executePost(request, "/");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot register Booking, Article not specified!", responseBody.get("error").getAsString());
	}

	@Test
	public void testRegisterBooking_Unauthorized() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);
		request.queryParam("articleId", 1);

		response = executePost(request, "/");

		response.then().statusCode(401);
	}

	@Test
	public void testRegisterBooking_UserNotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 9999);
		request.queryParam("articleId", 1);

		response = executePost(request, "/");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot register Booking, specified User not present in the system!",
				responseBody.get("error").getAsString());
	}

	@Test
	public void testRegisterBooking_ArticleNotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);
		request.queryParam("articleId", 9999);

		response = executePost(request, "/");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot register Booking, specified Article not present in catalogue!",
				responseBody.get("error").getAsString());
	}

	@Test
	public void testRegisterBooking_InvalidOperation() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2", "Science",
				"Description 2", ArticleState.BOOKED, "Author 2", "0987654321");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);
		request.queryParam("articleId", 2);

		response = executePost(request, "/");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot register Booking, specified Article is already booked!",
				responseBody.get("error").getAsString());
	}

	@Test
	public void testGetBookingInfo_Success() throws IllegalAccessException, SQLException {
		Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1),
				"Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		Booking booking = QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3),
				BookingState.ACTIVE, book, citizenUser);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");

		response = executeGet(request, "/1");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals(booking.getId(), responseBody.get("id").getAsLong());
		assertEquals(booking.getBookedArticle().getId(), responseBody.get("bookedArticleId").getAsLong());
		assertEquals(booking.getBookedArticle().getTitle(), responseBody.get("bookedArticleTitle").getAsString());
		assertEquals(booking.getBookingUser().getId(), responseBody.get("bookingUserId").getAsLong());
		assertEquals(booking.getState().toString(), responseBody.get("state").getAsString());

		JsonArray bookingDateArray = responseBody.get("bookingDate").getAsJsonArray();
		LocalDate bookingDateResponse = LocalDate.of(bookingDateArray.get(0).getAsInt(),
				bookingDateArray.get(1).getAsInt(), bookingDateArray.get(2).getAsInt());

		assertEquals(booking.getBookingDate(), bookingDateResponse);

		JsonArray bookingEndDateArray = responseBody.get("bookingEndDate").getAsJsonArray();
		LocalDate bookingEndDateResponse = LocalDate.of(bookingEndDateArray.get(0).getAsInt(),
				bookingEndDateArray.get(1).getAsInt(), bookingEndDateArray.get(2).getAsInt());

		assertEquals(booking.getBookingEndDate(), bookingEndDateResponse);
	}

	@Test
	public void testGetBookingInfo_NotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");

		response = executeGet(request, "/999");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Specified Booking not registered in the system!", responseBody.get("error").getAsString());
	}

	@Test
	public void testCancelBooking_Success() throws IllegalAccessException, SQLException {
		Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1",
				"Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE,
				book, citizenUser);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");

		response = executePatch(request, "/1/cancel");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Booking cancelled successfully.", responseBody.get("message").getAsString());
	}

	@Test
	public void testCancelBooking_NotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");

		response = executePatch(request, "/999/cancel");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Specified Booking not registered in the system!", responseBody.get("error").getAsString());
	}

	@Test
	public void testCancelBooking_Unauthorized() throws IllegalAccessException, SQLException {
		Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1",
				"Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE,
				book, citizenUser);
		QueryUtils.queryCreateUser(connection, 3L, "other@Email.com", "other password", "other",
				"surname", "address", "123432", UserRole.CITIZEN);
		String otherUserToken = AuthHelper.getAuthToken("other@Email.com", "other password");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + otherUserToken);
		request.header("Content-type", "application/json");

		response = executePatch(request, "/1/cancel");

		response.then().statusCode(401);
	}

	@Test
	public void testCancelBooking_InvalidOperation() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBooking(connection, 2L, LocalDate.now(), LocalDate.now().plusDays(3),
				BookingState.CANCELLED,
				QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.of(2021, 1, 1), "Publisher 2",
						"Science", "Description 2", ArticleState.AVAILABLE, "Author 2", "0987654321"),
				citizenUser);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");

		response = executePatch(request, "/2/cancel");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot cancel Booking. Specified Booking is not active!",
				responseBody.get("error").getAsString());
	}
	
	
    @Test
    public void testGetBookedArticlesByUser_Success() throws IllegalAccessException, SQLException {
        Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Booking booking = QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE, book, citizenUser);
    
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/2/bookings");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(1, itemsArray.size());

        JsonObject bookingObject = itemsArray.get(0).getAsJsonObject();
        assertEquals(booking.getId(), bookingObject.get("id").getAsLong());
        assertEquals(booking.getBookedArticle().getId(), bookingObject.get("bookedArticleId").getAsLong());
        assertEquals(booking.getBookedArticle().getTitle(), bookingObject.get("bookedArticleTitle").getAsString());
        assertEquals(booking.getBookingUser().getId(), bookingObject.get("bookingUserId").getAsLong());
        assertEquals(booking.getState().toString(), bookingObject.get("state").getAsString());
    
		JsonArray bookingDateArray = bookingObject.get("bookingDate").getAsJsonArray();
		LocalDate bookingDateResponse = LocalDate.of(bookingDateArray.get(0).getAsInt(),
				bookingDateArray.get(1).getAsInt(), bookingDateArray.get(2).getAsInt());

		assertEquals(booking.getBookingDate(), bookingDateResponse);

		JsonArray bookingEndDateArray = bookingObject.get("bookingEndDate").getAsJsonArray();
		LocalDate bookingEndDateResponse = LocalDate.of(bookingEndDateArray.get(0).getAsInt(),
				bookingEndDateArray.get(1).getAsInt(), bookingEndDateArray.get(2).getAsInt());

		assertEquals(booking.getBookingEndDate(), bookingEndDateResponse);
    }

    @Test
    public void testGetBookedArticlesByUser_MissingUser() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/999/bookings");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Specified user is not registered in the system!", responseBody.get("error").getAsString());
    }

    @Test
    public void testGetBookedArticlesByUser_InvalidPagination() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("pageNumber", 0);
        request.queryParam("resultsPerPage", -1);

        response = executeGet(request, "/2/bookings");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Pagination parameters incorrect!", responseBody.get("error").getAsString());
    }

    @Test
    public void testGetBookedArticlesByUser_Unauthorized() throws IllegalAccessException, SQLException {
        QueryUtils.queryCreateUser(connection, 3L, "other@Email.com", "other password", "other", "surname", "address", "123432", UserRole.CITIZEN);
        String otherUserToken = AuthHelper.getAuthToken("other@Email.com", "other password");

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + otherUserToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/2/bookings");

        response.then().statusCode(401);
    }

    @Test
    public void testGetBookedArticlesByUser_NoBookingsFound() throws IllegalAccessException, SQLException {
        QueryUtils.queryCreateUser(connection, 4L, "nobookings@Email.com", "user password", "No", "Bookings", "address", "123432", UserRole.CITIZEN);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/4/bookings");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("No bookings relative to the specified user found!", responseBody.get("error").getAsString());
    }

    @Test
    public void testGetBookedArticlesByUser_MultipleResults() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Book book2 = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2", "Fiction", "Description 2", ArticleState.AVAILABLE, "Author 2", "1234567890");

        QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE, book1, citizenUser);
        QueryUtils.queryCreateBooking(connection, 2L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE, book2, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("resultsPerPage", 1);
        request.queryParam("pageNumber", 1);

        response = executeGet(request, "/2/bookings");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(1, itemsArray.size());

        assertEquals(1, responseBody.get("pageNumber").getAsInt());
        assertEquals(1, responseBody.get("resultsPerPage").getAsInt());
        assertEquals(responseBody.get("totalResults").getAsInt(), 2);
        assertEquals(responseBody.get("totalPages").getAsInt(), 2);
    }

    @Test
    public void testGetBookedArticlesByUser_Pagination() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Book book2 = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2", "Fiction", "Description 2", ArticleState.AVAILABLE, "Author 2", "1234567890");
        Book book3 = QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3", "Fiction", "Description 3", ArticleState.AVAILABLE, "Author 3", "1234567890");
        Book book4 = QueryUtils.queryCreateBook(connection, 4L, "Shelf 4", "Book 4", LocalDate.now(), "Publisher 4", "Fiction", "Description 4", ArticleState.AVAILABLE, "Author 4", "1234567890");

        QueryUtils.queryCreateBooking(connection, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), BookingState.ACTIVE, book1, citizenUser);
        QueryUtils.queryCreateBooking(connection, 2L, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3), BookingState.ACTIVE, book2, citizenUser);
        Booking booking3 = QueryUtils.queryCreateBooking(connection, 3L, LocalDate.now().plusDays(3), LocalDate.now().plusDays(3), BookingState.ACTIVE, book3, citizenUser);
        Booking booking4 = QueryUtils.queryCreateBooking(connection, 4L, LocalDate.now().plusDays(4), LocalDate.now().plusDays(3), BookingState.ACTIVE, book4, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("resultsPerPage", 2);
        request.queryParam("pageNumber", 2);

        response = executeGet(request, "/2/bookings");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(2, itemsArray.size());

        JsonObject bookingObject = itemsArray.get(0).getAsJsonObject();
        assertEquals(booking3.getId(), bookingObject.get("id").getAsLong());

        bookingObject = itemsArray.get(1).getAsJsonObject();
        assertEquals(booking4.getId(), bookingObject.get("id").getAsLong());

        assertEquals(2, responseBody.get("pageNumber").getAsInt());
        assertEquals(2, responseBody.get("resultsPerPage").getAsInt());
        assertTrue(responseBody.get("totalResults").getAsInt() == 4);
        assertTrue(responseBody.get("totalPages").getAsInt() == 2);
    }

}

package it.gurzu.SWAM.iLib.rest.servicesTest;

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
import it.gurzu.swam.iLib.dto.ArticleDTO;
import it.gurzu.swam.iLib.dto.ArticleType;
import it.gurzu.swam.iLib.dto.BookingDTO;
import it.gurzu.swam.iLib.dto.LoanDTO;
import it.gurzu.swam.iLib.model.ArticleState;
import it.gurzu.swam.iLib.model.Book;
import it.gurzu.swam.iLib.model.BookingState;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;

public class ArticleEndpointTest extends ServiceTest {
	private String adminToken;
	private String citizenToken;

	private RequestSpecification request;
	private Response response;
	private String body;
	private JsonObject responseBody;

	@Override
	protected void beforeEachInit() throws SQLException, IllegalAccessException {
		setBaseURL("/iLib/v1/articlesEndpoint");

		QueryUtils.queryTruncateAll(connection);

		QueryUtils.queryCreateUser(connection, 1L, "admin@example.com", "admin password", "adminName",
				"adminSurname", "adminAddress", "adminTelephoneNumber", UserRole.ADMINISTRATOR);
		adminToken = AuthHelper.getAuthToken("admin@example.com", "admin password");

		QueryUtils.queryCreateUser(connection, 2L, "user@Email.com", "user password", "name", "surname",
				"address", "123432", UserRole.CITIZEN);
		citizenToken = AuthHelper.getAuthToken("user@Email.com", "user password");
	}

	@Test
	public void testCreateArticle_Success() {
		ArticleDTO articleDTO = createArticleDTO(1L, ArticleType.BOOK, "Shelf 1", "A Great Book",
				LocalDate.now().minusYears(1), "Publisher", "Fiction", "Description", ArticleState.AVAILABLE, "Author",
				"1234567890", null, null, null, null, null, null);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.body(articleDTO);

		response = executePost(request, "/");

		response.then().statusCode(201).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertTrue(responseBody.has("articleId"));
	}

	@ParameterizedTest
	@MethodSource("provideInvalidArticleData")
	public void testCreateArticle_InvalidData(ArticleDTO articleDTO, String expectedErrorMessage) {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.body(articleDTO);

		response = executePost(request, "/");

		response.then().statusCode(400).contentType("text/plain;charset=UTF-8");

		body = response.getBody().asString();

		assertTrue(body.contains(expectedErrorMessage));
	}

	@Test
	public void testCreateArticle_Unauthorized() {
		ArticleDTO articleDTO = createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "A Great Book",
				LocalDate.now().minusYears(1), "Publisher", "Fiction", "Description", ArticleState.AVAILABLE, "Author",
				"1234567890", null, null, null, null, null, null);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.body(articleDTO);

		response = executePost(request, "/");

		response.then().statusCode(403).contentType("text/html;charset=UTF-8");

		body = response.getBody().asString();

		assertEquals("Access forbidden: role not allowed", body);
	}

	@Test
	public void testUpdateArticle_Success() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 2L, "upstairs", "cujo", LocalDate.now(), "publisher",
				"horror", "a nice book", ArticleState.BOOKED, "King", "isbn");

		ArticleDTO articleDTO = createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "Updated Book", LocalDate.now(),
				"Updated Publisher", "Updated Genre", "Updated Description", ArticleState.BOOKED, "Updated Author",
				"1234567890", null, null, null, null, null, null);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 2L);
		request.body(articleDTO);

		response = executePut(request, "/{articleId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Article updated successfully.", responseBody.get("message").getAsString());
	}

	@Test
	public void testUpdateArticle_NotFound() {
		ArticleDTO articleDTO = createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "Updated Book",
				LocalDate.now(), "Updated Publisher", "Updated Genre", "Updated Description",
				ArticleState.AVAILABLE, "Updated Author", "1234567890", null, null, null, null, null, null);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 9999L);
		request.body(articleDTO);

		response = executePut(request, "/{articleId}");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Article does not exist!", responseBody.get("error").getAsString());
	}

	@ParameterizedTest
	@MethodSource("provideInvalidArticleData")
	public void testUpdateArticle_InvalidData(ArticleDTO articleDTO, String expectedErrorMessage) {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);
		request.body(articleDTO);

		response = executePut(request, "/{articleId}");

		response.then().statusCode(400).contentType("text/plain;charset=UTF-8");

		body = response.getBody().asString();

		assertTrue(body.contains(expectedErrorMessage));
	}

	@Test
	public void testUpdateArticle_Unauthorized() {
		ArticleDTO articleDTO = createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "Updated Book",
				LocalDate.now(), "Updated Publisher", "Updated Genre", "Updated Description",
				ArticleState.AVAILABLE, "Updated Author", "1234567890", null, null, null, null, null, null);

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);
		request.body(articleDTO);

		response = executePut(request, "/{articleId}");

		response.then().statusCode(403).contentType("text/html;charset=UTF-8");

		body = response.getBody().asString();

		assertEquals("Access forbidden: role not allowed", body);
	}

	@Test
	public void testGetArticleInfo_Success() throws IllegalAccessException, SQLException {
		Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "A Great Book",
				LocalDate.now().minusYears(1), "Publisher", "Fiction", "Description", ArticleState.AVAILABLE, "Author",
				"1234567890");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);

		response = executeGet(request, "/{articleId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals(book.getId(), responseBody.get("id").getAsLong());
		assertEquals(book.getLocation(), responseBody.get("location").getAsString());
		assertEquals(book.getTitle(), responseBody.get("title").getAsString());
		assertEquals(book.getPublisher(), responseBody.get("publisher").getAsString());
		assertEquals(book.getGenre(), responseBody.get("genre").getAsString());
		assertEquals(book.getDescription(), responseBody.get("description").getAsString());
		assertEquals(book.getState().toString(), responseBody.get("state").getAsString());
		assertEquals("BOOK", responseBody.get("type").getAsString());
		assertEquals(book.getAuthor(), responseBody.get("author").getAsString());
		assertEquals(book.getIsbn(), responseBody.get("isbn").getAsString());

		JsonArray yearEditionArray = responseBody.get("yearEdition").getAsJsonArray();
		LocalDate yearEditionResponse = LocalDate.of(yearEditionArray.get(0).getAsInt(),
				yearEditionArray.get(1).getAsInt(), yearEditionArray.get(2).getAsInt());

		assertEquals(book.getYearEdition(), yearEditionResponse);
	}

	@Test
	public void testGetArticleInfo_NotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 9999L);

		response = executeGet(request, "/{articleId}");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Article does not exist!", responseBody.get("error").getAsString());
	}

	@Test
	public void testSearchArticles_Success() throws IllegalAccessException, SQLException {

		Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now().minusYears(1),
				"Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now().minusYears(2),
				"Publisher 2", "Science", "Description 2", ArticleState.BOOKED, "Author 2", "0987654321");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("title", "Book 1");

		response = executeGet(request, "/");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals(1, responseBody.get("totalResults").getAsInt());
		assertEquals(1, responseBody.get("totalPages").getAsInt());
		assertEquals(1, responseBody.get("pageNumber").getAsInt());
		assertEquals(10, responseBody.get("resultsPerPage").getAsInt());

		JsonObject article = responseBody.get("items").getAsJsonArray().get(0).getAsJsonObject();
		assertEquals(book1.getId(), article.get("id").getAsLong());
		assertEquals(book1.getLocation(), article.get("location").getAsString());
		assertEquals(book1.getTitle(), article.get("title").getAsString());
		assertEquals(book1.getPublisher(), article.get("publisher").getAsString());
		assertEquals(book1.getGenre(), article.get("genre").getAsString());
		assertEquals(book1.getDescription(), article.get("description").getAsString());
		assertEquals(book1.getState().toString(), article.get("state").getAsString());
		assertEquals(book1.getIsbn(), article.get("isbn").getAsString());

		JsonArray yearEditionArray = article.get("yearEdition").getAsJsonArray();
		LocalDate yearEditionResponse = LocalDate.of(yearEditionArray.get(0).getAsInt(),
				yearEditionArray.get(1).getAsInt(), yearEditionArray.get(2).getAsInt());

		assertEquals(book1.getYearEdition(), yearEditionResponse);

		assertEquals(1, responseBody.get("pageNumber").getAsInt());
		assertEquals(10, responseBody.get("resultsPerPage").getAsInt());
		assertEquals(1, responseBody.get("totalResults").getAsInt());
		assertEquals(1, responseBody.get("totalPages").getAsInt());

	}

	@Test
	public void testSearchArticles_InvalidDateFormat() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("yearEdition", "invalid-date");

		response = executeGet(request, "/");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Invalid date format for 'yearEdition', expected format YYYY-MM-DD.",
				responseBody.get("error").getAsString());
	}

	@Test
	public void testSearchArticles_InvalidPaginationParameters() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("pageNumber", 0);
		request.queryParam("resultsPerPage", -1);

		response = executeGet(request, "/");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Pagination parameters incorrect!", responseBody.get("error").getAsString());
	}

	@Test
	public void testSearchArticles_NoResults() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("title", "Nonexistent Book");

		response = executeGet(request, "/");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("The search has given 0 results!", responseBody.get("error").getAsString());
	}

	@Test
	public void testSearchArticles_MultipleResults() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now().minusYears(2), "Publisher 1",
				"Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now().minusYears(1), "Publisher 2",
				"Science", "Description 2", ArticleState.BOOKED, "Author 2", "0987654321");
		QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3",
				"History", "Description 3", ArticleState.ONLOAN, "Author 1", "1234509876");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.queryParam("author", "Author 1");

		response = executeGet(request, "/");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertTrue(responseBody.has("items"));
		JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
		assertEquals(2, itemsArray.size());

		for (int i = 0; i < itemsArray.size(); i++) {
			JsonObject articleObject = itemsArray.get(i).getAsJsonObject();
			assertEquals(articleObject.get("author").getAsString(), "Author 1");
		}

		assertEquals(1, responseBody.get("pageNumber").getAsInt());
		assertEquals(10, responseBody.get("resultsPerPage").getAsInt());
		assertTrue(responseBody.get("totalResults").getAsInt() == 2);
		assertTrue(responseBody.get("totalPages").getAsInt() == 1);
	}

	@Test
	public void testSearchArticles_Pagination() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now().minusYears(2), "Publisher 1",
				"Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now().minusYears(1), "Publisher 2",
				"Science", "Description 2", ArticleState.BOOKED, "Author 2", "0987654321");
		QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3",
				"History", "Description 3", ArticleState.ONLOAN, "Author 1", "1234509876");

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
		assertEquals(1, itemsArray.size());

		JsonObject articleObject = itemsArray.get(0).getAsJsonObject();
		assertEquals("Book 1", articleObject.get("title").getAsString());

		assertEquals(2, responseBody.get("pageNumber").getAsInt());
		assertEquals(2, responseBody.get("resultsPerPage").getAsInt());
		assertEquals(3, responseBody.get("totalResults").getAsInt());
		assertEquals(2, responseBody.get("totalPages").getAsInt());
	}

	@Test
	public void testDeleteArticle_Success() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now().minusYears(2), "Publisher 1",
				"Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
		QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now().minusYears(1), "Publisher 2",
				"Science", "Description 2", ArticleState.ONLOAN, "Author 2", "0987654321");
		QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3",
				"History", "Description 3", ArticleState.BOOKED, "Author 3", "1234509876");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);

		response = executeDelete(request, "/{articleId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Article deleted successfully.", responseBody.get("message").getAsString());
	}
	
	@Test
	public void testDeleteArticle_Unauthorized() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);

		response = executeDelete(request, "/{articleId}");

		response.then().statusCode(403).contentType("text/html;charset=UTF-8");

		body = response.getBody().asString();

		assertEquals("Access forbidden: role not allowed", body);
	}

	@Test
	public void testDeleteArticle_NotFound() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 9999L);

		response = executeDelete(request, "/{articleId}");

		response.then().statusCode(404).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot remove Article! Article not in catalogue!", responseBody.get("error").getAsString());
	}

	@Test
	public void testDeleteArticle_InvalidOperation_OnLoan() throws IllegalAccessException, SQLException {
		QueryUtils.queryCreateBook(connection, 1L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2",
				"Science", "Description 2", ArticleState.ONLOAN, "Author 2", "0987654321");

		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);

		response = executeDelete(request, "/{articleId}");

		response.then().statusCode(400).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Cannot remove Article from catalogue! Article currently on loan!",
				responseBody.get("error").getAsString());
	}

	@Test
	public void testDeleteArticle_BookedArticle() throws IllegalAccessException, SQLException {
		User user = QueryUtils.queryCreateUser(connection, 3L, "user@Email.com", "user password", "name", "surname", "address",
				"123432", UserRole.CITIZEN);

		Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3",
				"History", "Description 3", ArticleState.BOOKED, "Author 3", "1234509876");

		QueryUtils.queryCreateBooking(connection, 7L, LocalDate.now(), LocalDate.now().plusDays(3), BookingState.ACTIVE,
				book, user);
		
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + adminToken);
		request.header("Content-type", "application/json");
		request.pathParam("articleId", 1L);

		response = executeDelete(request, "/{articleId}");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Article deleted successfully.", responseBody.get("message").getAsString());
	}

	private static Stream<Arguments> provideInvalidArticleData() {
		return Stream.of(
				Arguments.of(createArticleDTO(null, null, "Shelf 1", "A Great Book", LocalDate.now().minusYears(1),
						"Publisher", "Fiction", "Description", ArticleState.AVAILABLE, "Author", "1234567890", null,
						null, null, null, null, null), "Type is required"), // null ArticleType
				Arguments.of(createArticleDTO(null, ArticleType.BOOK, null, "A Great Book",
						LocalDate.now().minusYears(1), "Publisher", "Fiction", "Description", ArticleState.AVAILABLE,
						"Author", "1234567890", null, null, null, null, null, null), "Location is required"), // null
																												// location
				Arguments.of(createArticleDTO(null, ArticleType.BOOK, "Shelf 1", null, LocalDate.now().minusYears(1),
						"Publisher", "Fiction", "Description", ArticleState.AVAILABLE, "Author", "1234567890", null,
						null, null, null, null, null), "Title is required"), // null title
				Arguments.of(createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "A Great Book", null, "Publisher",
						"Fiction", "Description", ArticleState.AVAILABLE, "Author", "1234567890", null, null, null,
						null, null, null), "Year of edition is required"), // null yearEdition
				Arguments.of(
						createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "A Great Book",
								LocalDate.now().plusYears(1), "Publisher", "Fiction", "Description",
								ArticleState.AVAILABLE, "Author", "1234567890", null, null, null, null, null, null),
						"Year of edition cannot be in the future"), // yarEdition in the future
				Arguments.of(createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "A Great Book",
						LocalDate.now().minusYears(1), null, "Fiction", "Description", ArticleState.AVAILABLE, "Author",
						"1234567890", null, null, null, null, null, null), "Publisher is required"), // null publisher
				Arguments.of(createArticleDTO(null, ArticleType.BOOK, "Shelf 1", "A Great Book",
						LocalDate.now().minusYears(1), "Publisher", null, "Description", ArticleState.AVAILABLE,
						"Author", "1234567890", null, null, null, null, null, null), "Genre is required") // null genre
		);
	}

	private static ArticleDTO createArticleDTO(Long id, ArticleType type, String location, String title,
			LocalDate yearEdition, String publisher, String genre, String description, ArticleState state,
			String author, String isbn, Integer issueNumber, String issn, String director, String isan,
			BookingDTO bookingDTO, LoanDTO loanDTO) {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setId(id);
		articleDTO.setType(type);
		articleDTO.setLocation(location);
		articleDTO.setTitle(title);
		articleDTO.setYearEdition(yearEdition);
		articleDTO.setPublisher(publisher);
		articleDTO.setGenre(genre);
		articleDTO.setDescription(description);
		articleDTO.setState(state);
		articleDTO.setAuthor(author);
		articleDTO.setIsbn(isbn);
		articleDTO.setIssueNumber(issueNumber);
		articleDTO.setIssn(issn);
		articleDTO.setDirector(director);
		articleDTO.setIsan(isan);
		articleDTO.setLoanDTO(loanDTO);
		articleDTO.setBookingDTO(bookingDTO);
		return articleDTO;
	}

}
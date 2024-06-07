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
import it.gurzu.swam.iLib.model.Loan;
import it.gurzu.swam.iLib.model.LoanState;
import it.gurzu.swam.iLib.model.User;
import it.gurzu.swam.iLib.model.UserRole;

public class LoanEndpointTest extends ServiceTest {
	private User citizenUser;
	private String adminToken;
	private String citizenToken;

	private RequestSpecification request;
	private Response response;
	private String body;
	private JsonObject responseBody;

	@Override
	protected void beforeEachInit() throws SQLException, IllegalAccessException {
		setBaseURL("/iLib/v1/loansEndpoint");

		QueryUtils.queryTruncateAll(connection);

		QueryUtils.queryCreateUser(connection, 1L, "admin@example.com", "admin password", "adminName",
				"adminSurname", "adminAddress", "adminTelephoneNumber", UserRole.ADMINISTRATOR);
		adminToken = AuthHelper.getAuthToken("admin@example.com", "admin password");

		citizenUser = QueryUtils.queryCreateUser(connection, 2L, "user@Email.com", "user password", "name", "surname",
				"address", "123432", UserRole.CITIZEN);
		citizenToken = AuthHelper.getAuthToken("user@Email.com", "user password");
	}

    @Test
    public void testRegisterLoan_Success() throws IllegalAccessException, SQLException {
        QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("userId", 2L);
        request.queryParam("articleId", 1L);

        response = executePost(request, "/");

        response.then().statusCode(201).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("loanId"));
    }

	@Test
	public void testRegisterLoan_Unauthorized() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);
		request.queryParam("articleId", 1);

		response = executePost(request, "/");

		response.then().statusCode(403);
	}

    @Test
    public void testRegisterLoan_MissingUser() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("articleId", 1L);

        response = executePost(request, "/");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot register Loan, User not specified!", responseBody.get("error").getAsString());
    }

    @Test
    public void testRegisterLoan_MissingArticle() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("userId", 2L);

        response = executePost(request, "/");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot register Loan, Article not specified!", responseBody.get("error").getAsString());
    }

    @Test
    public void testRegisterLoan_UserNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("userId", 9999L);
        request.queryParam("articleId", 1L);

        response = executePost(request, "/");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot register Loan, specified User not present in the system!", responseBody.get("error").getAsString());
    }

    @Test
    public void testRegisterLoan_ArticleNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("userId", 2L);
        request.queryParam("articleId", 9999L);

        response = executePost(request, "/");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot register Loan, specified Article not present in catalogue!", responseBody.get("error").getAsString());
    }

    @Test
    public void testRegisterLoan_ArticleAlreadyOnLoan() throws IllegalAccessException, SQLException {
        Book book = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.of(2020, 1, 2), "Publisher 2", "Non-Fiction", "Description 2", ArticleState.ONLOAN, "Author 2", "0987654321");
        QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.queryParam("userId", 2L);
        request.queryParam("articleId", 2L);

        response = executePost(request, "/");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot register Loan, specified Article is already on loan!", responseBody.get("error").getAsString());
    }
    
    @Test
    public void testRegisterReturn_Success() throws SQLException, IllegalAccessException {
        Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.ONLOAN, "Author 1", "1234567890");
        Loan loan = QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executePatch(request, "/" + loan.getId() + "/return");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Loan successfully returned.", responseBody.get("message").getAsString());
    }
    
	@Test
	public void testRegisterReturn_Unauthorized() {
		request = RestAssured.given();
		request.header("Authorization", "Bearer " + citizenToken);
		request.header("Content-type", "application/json");
		request.queryParam("userId", 1);
		request.queryParam("articleId", 1);

		response = executePost(request, "/");

		response.then().statusCode(403);
	}

    @Test
    public void testRegisterReturn_LoanNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executePatch(request, "/9999/return");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot return article! Loan not registered!", responseBody.get("error").getAsString());
    }

    @Test
    public void testRegisterReturn_AlreadyReturned() throws SQLException, IllegalAccessException {
        Book book = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.of(2020, 1, 2), "Publisher 2", "Non-Fiction", "Description 2", ArticleState.AVAILABLE, "Author 2", "0987654321");
        Loan loan = QueryUtils.queryCreateLoan(connection, 2L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.RETURNED, false, book, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executePatch(request, "/" + loan.getId() + "/return");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot return article! Loan has already been returned!", responseBody.get("error").getAsString());
    }
    
    @Test
    public void testGetLoanInfo_Success() throws SQLException, IllegalAccessException {
        Book book = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Loan loan = QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/" + loan.getId());

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals(loan.getId().longValue(), responseBody.get("id").getAsLong());
        assertEquals(loan.getArticleOnLoan().getId().longValue(), responseBody.get("articleId").getAsLong());
        assertEquals(loan.getArticleOnLoan().getTitle(), responseBody.get("articleTitle").getAsString());
        assertEquals(loan.getLoaningUser().getId().longValue(), responseBody.get("loaningUserId").getAsLong());
        assertEquals(loan.isRenewed(), responseBody.get("renewed").getAsBoolean());
        assertEquals(loan.getState().toString(), responseBody.get("state").getAsString());

		JsonArray loanDateArray = responseBody.get("loanDate").getAsJsonArray();
		LocalDate loanDateResponse = LocalDate.of(loanDateArray.get(0).getAsInt(),
				loanDateArray.get(1).getAsInt(), loanDateArray.get(2).getAsInt());

		assertEquals(loan.getLoanDate(), loanDateResponse);

		JsonArray loanDueDateArray = responseBody.get("dueDate").getAsJsonArray();
		LocalDate loanDueDateResponse = LocalDate.of(loanDueDateArray.get(0).getAsInt(),
				loanDueDateArray.get(1).getAsInt(), loanDueDateArray.get(2).getAsInt());

		assertEquals(loan.getDueDate(), loanDueDateResponse);
    }

    @Test
    public void testGetLoanInfo_LoanNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");

        response = executeGet(request, "/9999");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Specified Loan not registered in the system!", responseBody.get("error").getAsString());
    }
    
    @Test
    public void testGetLoansByUser_Success() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Loan loan = QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book1, citizenUser);
        
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", citizenUser.getId());

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(1, itemsArray.size());

        JsonObject loanObject = itemsArray.get(0).getAsJsonObject();
        assertEquals(loan.getId(), loanObject.get("id").getAsLong());
        assertEquals(loan.getArticleOnLoan().getId(), loanObject.get("articleId").getAsLong());
        assertEquals(loan.getArticleOnLoan().getTitle(), loanObject.get("articleTitle").getAsString());
        assertEquals(loan.getLoaningUser().getId(), loanObject.get("loaningUserId").getAsLong());
        assertEquals(loan.getState().toString(), loanObject.get("state").getAsString());
        assertEquals(loan.isRenewed(), loanObject.get("renewed").getAsBoolean());

    
		JsonArray loanDateArray = loanObject.get("loanDate").getAsJsonArray();
		LocalDate loanDateResponse = LocalDate.of(loanDateArray.get(0).getAsInt(),
				loanDateArray.get(1).getAsInt(), loanDateArray.get(2).getAsInt());

		assertEquals(loan.getLoanDate(), loanDateResponse);

		JsonArray loanDueDateArray = loanObject.get("dueDate").getAsJsonArray();
		LocalDate loanDueDateResponse = LocalDate.of(loanDueDateArray.get(0).getAsInt(),
				loanDueDateArray.get(1).getAsInt(), loanDueDateArray.get(2).getAsInt());

		assertEquals(loan.getDueDate(), loanDueDateResponse);
    }

    @Test
    public void testGetLoansByUser_InvalidPagination() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", citizenUser.getId());
        request.queryParam("pageNumber", 0); // Invalid page number

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Pagination parameters incorrect!", responseBody.get("error").getAsString());
    }

    @Test
    public void testGetLoansByUser_Unauthorized() throws IllegalAccessException, SQLException {
        QueryUtils.queryCreateUser(connection, 3L, "another@Email.com", "another password", "anotherName", "anotherSurname", "anotherAddress", "1234323", UserRole.CITIZEN);
        String anotherToken = AuthHelper.getAuthToken("another@Email.com", "another password");

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + anotherToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", citizenUser.getId());

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(401);
    }

    @Test
    public void testGetLoansByUser_UserNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", 9999L);

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Specified user is not registered in the system!", responseBody.get("error").getAsString());
    }

    @Test
    public void testGetLoansByUser_LoansNotFound() throws SQLException, IllegalAccessException {
        User newUser = QueryUtils.queryCreateUser(connection, 4L, "new@Email.com", "new password", "newName", "newSurname", "newAddress", "1234324", UserRole.CITIZEN);
        String newToken = AuthHelper.getAuthToken("new@Email.com", "new password");

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + newToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", newUser.getId());

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("No loans relative to the specified user found!", responseBody.get("error").getAsString());
    }
    
    @Test
    public void testGetLoansByUser_MultipleResults() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Book book2 = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2", "Non-Fiction", "Description 2", ArticleState.AVAILABLE, "Author 2", "0987654321");
        Book book3 = QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3", "Science", "Description 3", ArticleState.AVAILABLE, "Author 3", "1122334455");

        QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book1, citizenUser);
        QueryUtils.queryCreateLoan(connection, 2L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book2, citizenUser);
        QueryUtils.queryCreateLoan(connection, 3L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book3, citizenUser);
        
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", citizenUser.getId());
        request.queryParam("pageNumber", 1);
        request.queryParam("resultsPerPage", 2);

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(2, itemsArray.size());

        assertEquals(1, responseBody.get("pageNumber").getAsInt());
        assertEquals(2, responseBody.get("resultsPerPage").getAsInt());
        assertEquals(3, responseBody.get("totalResults").getAsInt());
        assertEquals(2, responseBody.get("totalPages").getAsInt());
    }

    @Test
    public void testGetLoansByUser_Pagination() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.now(), "Publisher 1", "Fiction", "Description 1", ArticleState.AVAILABLE, "Author 1", "1234567890");
        Book book2 = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.now(), "Publisher 2", "Non-Fiction", "Description 2", ArticleState.AVAILABLE, "Author 2", "0987654321");
        Book book3 = QueryUtils.queryCreateBook(connection, 3L, "Shelf 3", "Book 3", LocalDate.now(), "Publisher 3", "Science", "Description 3", ArticleState.AVAILABLE, "Author 3", "1122334455");
        Book book4 = QueryUtils.queryCreateBook(connection, 4L, "Shelf 4", "Book 4", LocalDate.now(), "Publisher 4", "Science", "Description 4", ArticleState.AVAILABLE, "Author 4", "1122334455");

        QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book1, citizenUser);
        QueryUtils.queryCreateLoan(connection, 2L, LocalDate.now().plusDays(2), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book2, citizenUser);
        Loan loan3 = QueryUtils.queryCreateLoan(connection, 3L, LocalDate.now().plusDays(3), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book3, citizenUser);
        Loan loan4 = QueryUtils.queryCreateLoan(connection, 4L, LocalDate.now().plusDays(4), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book4, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("userId", citizenUser.getId());
        request.queryParam("pageNumber", 2);
        request.queryParam("resultsPerPage", 2);

        response = executeGet(request, "/{userId}/loans");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertTrue(responseBody.has("items"));
        JsonArray itemsArray = responseBody.get("items").getAsJsonArray();
        assertEquals(2, itemsArray.size());

        JsonObject loanObject = itemsArray.get(0).getAsJsonObject();
        assertEquals(loan3.getId(), loanObject.get("id").getAsLong());

        loanObject = itemsArray.get(1).getAsJsonObject();
        assertEquals(loan4.getId(), loanObject.get("id").getAsLong());

        assertEquals(2, responseBody.get("pageNumber").getAsInt());
        assertEquals(2, responseBody.get("resultsPerPage").getAsInt());
        assertEquals(4, responseBody.get("totalResults").getAsInt());
        assertEquals(2, responseBody.get("totalPages").getAsInt());
    }
    
    @Test
    public void testExtendLoan_Success() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1), "Publisher 1", "Fiction", "Description 1", ArticleState.ONLOAN, "Author 1", "1234567890");
        QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book1, citizenUser);
        
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("loanId", 1L);

        response = executePatch(request, "/{loanId}/extend");

        response.then().statusCode(200).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Loan extended successfully.", responseBody.get("message").getAsString());
    }

    @Test
    public void testExtendLoan_Unauthorized() throws IllegalAccessException, SQLException {
        Book book1 = QueryUtils.queryCreateBook(connection, 1L, "Shelf 1", "Book 1", LocalDate.of(2020, 1, 1), "Publisher 1", "Fiction", "Description 1", ArticleState.ONLOAN, "Author 1", "1234567890");
        QueryUtils.queryCreateLoan(connection, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book1, citizenUser);

    	QueryUtils.queryCreateUser(connection, 3L, "another@Email.com", "another password", "anotherName", "anotherSurname", "anotherAddress", "1234323", UserRole.CITIZEN);
        String anotherToken = AuthHelper.getAuthToken("another@Email.com", "another password");

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + anotherToken);
        request.header("Content-type", "application/json");
        request.pathParam("loanId", 1L);

        response = executePatch(request, "/{loanId}/extend");

        response.then().statusCode(401);
    }

    @Test
    public void testExtendLoan_LoanNotFound() {
        request = RestAssured.given();
        request.header("Authorization", "Bearer " + adminToken);
        request.header("Content-type", "application/json");
        request.pathParam("loanId", 9999L);

        response = executePatch(request, "/{loanId}/extend");

        response.then().statusCode(404).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Specified Loan not registered in the system!", responseBody.get("error").getAsString());
    }

    @Test
    public void testExtendLoan_InvalidOperation() throws IllegalAccessException, SQLException {
        Book book2 = QueryUtils.queryCreateBook(connection, 2L, "Shelf 2", "Book 2", LocalDate.of(2020, 1, 2), "Publisher 2", "Non-Fiction", "Description 2", ArticleState.ONLOANBOOKED, "Author 2", "0987654321");
        QueryUtils.queryCreateLoan(connection, 2L, LocalDate.now(), LocalDate.now().plusMonths(1), LoanState.ACTIVE, false, book2, citizenUser);

        request = RestAssured.given();
        request.header("Authorization", "Bearer " + citizenToken);
        request.header("Content-type", "application/json");
        request.pathParam("loanId", 2L);

        response = executePatch(request, "/{loanId}/extend");

        response.then().statusCode(400).contentType("application/json");

        body = response.getBody().asString();
        responseBody = JsonParser.parseString(body).getAsJsonObject();

        assertEquals("Cannot extend loan, another User has booked the Article!", responseBody.get("error").getAsString());
    }
}

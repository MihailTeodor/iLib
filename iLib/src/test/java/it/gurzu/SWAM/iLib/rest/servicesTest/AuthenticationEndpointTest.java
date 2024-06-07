package it.gurzu.SWAM.iLib.rest.servicesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import it.gurzu.swam.iLib.dto.LoginDTO;
import it.gurzu.swam.iLib.model.UserRole;

public class AuthenticationEndpointTest extends ServiceTest {

	private RequestSpecification request;
	private Response response;
	private String body;
	private JsonObject responseBody;

	@Override
	protected void beforeEachInit() throws SQLException, IllegalAccessException {
		setBaseURL("/iLib/v1/auth");

		QueryUtils.queryTruncateAll(connection);

		QueryUtils.queryCreateUser(connection, 2L, "user@Email.com", "user password", "name", "surname",
				"address", "123432", UserRole.CITIZEN);

	}

	@Test
	public void testLoginUser_Success() {
		LoginDTO credentials = new LoginDTO();
		credentials.setEmail("user@Email.com");
		credentials.setPassword("user password");

		request = RestAssured.given();
		request.header("Content-type", "application/json");
		request.body(credentials);

		response = executePost(request, "/login");

		response.then().statusCode(200).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertTrue(responseBody.has("token"));
	}

	@ParameterizedTest
	@MethodSource("provideInvalidCredentials")
	public void testLoginUser_InvalidCredentials(String email, String password) {
		LoginDTO credentials = new LoginDTO();
		credentials.setEmail(email);
		credentials.setPassword(password);

		request = RestAssured.given();
		request.header("Content-type", "application/json");
		request.body(credentials);

		response = executePost(request, "/login");

		response.then().statusCode(401).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Credentials are invalid.", responseBody.get("error").getAsString());
	}

	private static Stream<Arguments> provideInvalidCredentials() {
		return Stream.of(Arguments.of("user@example.com", "wrongpassword"),
				Arguments.of("wronguser@example.com", "user password")
		);
	}

	@ParameterizedTest
	@MethodSource("provideMissingCredentials")
	public void testLoginUser_MissingCredentials(String email, String password, String expectedErrorMessage) {
		LoginDTO credentials = new LoginDTO();
		credentials.setEmail(email);
		credentials.setPassword(password);

		request = RestAssured.given();
		request.header("Content-type", "application/json");
		request.body(credentials);

		response = executePost(request, "/login");

		response.then().statusCode(400).contentType("text/plain;charset=UTF-8");

		body = response.getBody().asString();

		assertTrue(body.contains(expectedErrorMessage));
	}

	private static Stream<Arguments> provideMissingCredentials() {
		return Stream.of(Arguments.of("", "password123", "Email is required"),
				Arguments.of("user@example.com", "", "Password is required"),
				Arguments.of("", "", "Email is required")
		);
	}

	@Test
	public void testLoginUser_UserNotFound() {
		LoginDTO credentials = new LoginDTO();
		credentials.setEmail("nonexistent@example.com");
		credentials.setPassword("password123");

		request = RestAssured.given();
		request.header("Content-type", "application/json");
		request.body(credentials);

		response = executePost(request, "/login");

		response.then().statusCode(401).contentType("application/json");

		body = response.getBody().asString();
		responseBody = JsonParser.parseString(body).getAsJsonObject();

		assertEquals("Credentials are invalid.", responseBody.get("error").getAsString());
	}
}

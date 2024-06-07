package it.gurzu.SWAM.iLib.rest.servicesTest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.NamingException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class ServiceTest {
	private String baseURL;

	private static String DBconnectionURL;
	private static String username;
	private static String password;

	protected static Connection connection;

	@BeforeAll
	public static void setup() throws SQLException, ClassNotFoundException, NamingException {

		Reader json = new InputStreamReader(ServiceTest.class.getResourceAsStream("/configurations.json"));
		JsonObject jsonObject = JsonParser.parseReader(json).getAsJsonObject();
		JsonObject rest = jsonObject.get("rest").getAsJsonObject();
		String baseURI = rest.get("baseURI").getAsString();
		int port = rest.get("port").getAsInt();

		RestAssured.baseURI = baseURI;
		RestAssured.port = port;

		RestAssured.get().then().statusCode(200); // check that server is up and running

		JsonObject DB = jsonObject.get("DB").getAsJsonObject();
		DBconnectionURL = DB.get("connectionURL").getAsString();
		username = DB.get("username").getAsString();
		password = DB.get("password").getAsString();
		connection = DriverManager.getConnection(DBconnectionURL, username, password); // check DB connection
	}

	@BeforeEach
	public void beforeEach() throws IllegalAccessException, SQLException {

		beforeEachInit();

	}

	protected abstract void beforeEachInit() throws SQLException, IllegalAccessException;

	@AfterEach
	public void afterEach() throws SQLException {

	}

	@AfterAll
	public static void afterAll() throws SQLException {
		connection.close();
	}

	protected Response executeGet(RequestSpecification r, String path) {
		return r.get(baseURL + path);
	}

	protected Response executePut(RequestSpecification r, String path) {
		return r.put(baseURL + path);
	}

	protected Response executePost(RequestSpecification r, String path) {
		return r.post(baseURL + path);
	}

	protected Response executePatch(RequestSpecification r, String path) {
		return r.patch(baseURL + path);
	}

	protected Response executeDelete(RequestSpecification r, String path) {
		return r.delete(baseURL + path);
	}

	void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
}

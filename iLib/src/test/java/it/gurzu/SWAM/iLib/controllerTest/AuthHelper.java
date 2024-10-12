package it.gurzu.SWAM.iLib.controllerTest;

import static io.restassured.RestAssured.given;

public class AuthHelper {

	public static String getAuthToken(String email, String password) {
        return given()
                .header("Content-Type", "application/json")
                .body("{ \"email\": \"" + email +"\", \"password\": \"" + password + "\"}")
                .when()
                .post("http://localhost/iLib/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }	
}


package com.devsuperior.dscommerce.tests;

import static io.restassured.RestAssured.given;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TokenUtil {
	
	public static String obtainAccessToken(String username, String password) {
		Response response = authRequest(username, password);
		JsonPath jsonPath = response.jsonPath();	
		return jsonPath.getString("access_token");
	}
	
	private static Response authRequest(String username, String password) {
		return given()
				.auth()
				.preemptive()
				.basic("myclientid","myclientsecret")
				.contentType("application/x-www-form-urlencoded")
				.formParam("grant_type", "password")
				.formParam("username", username)
				.formParam("password", password)
				.when()
				.post("/oauth2/token");
	}

}

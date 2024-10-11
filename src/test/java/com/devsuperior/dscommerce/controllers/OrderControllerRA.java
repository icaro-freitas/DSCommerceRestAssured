package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;

public class OrderControllerRA {
	
	private String clientUserName;

	private String clientPassword;

	private String adminUserName;

	private String adminPassword;
	
	private String clientToken;

	private String adminToken;

	private String invalidToken;
	
	private Long existingOrderId;

	private Long nonExistingOrderId;
	
	@BeforeEach
	public void setUp() {
		
		baseURI = "http://localhost:8080";
		
		existingOrderId = 1L;
		nonExistingOrderId = 100L;		
		
		clientUserName = "maria@gmail.com";
		clientPassword = "123456";
		adminUserName = "alex@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUserName, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUserName, adminPassword);
		invalidToken = adminToken + "xpto";
		
	}
	
	@Test
	public void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + adminToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", existingOrderId)
		.then()
		.statusCode(200)
		.body("id", is(1))
		.body("moment", equalTo("2022-07-25T13:00:00Z"))
		.body("status", equalTo("PAID"))
		.body("client.name", equalTo("Maria Brown"))
		.body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
		.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
		
	}
	
	@Test
	public void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", existingOrderId)
		.then()
		.statusCode(200)
		.body("id", is(1))
		.body("moment", equalTo("2022-07-25T13:00:00Z"))
		.body("status", equalTo("PAID"))
		.body("client.name", equalTo("Maria Brown"))
		.body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
		.body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
		
	}
	
	@Test
	public void findByIdShouldReturnForbiddenIdExistsAndClientLoggedAndOrderDoesNotBelongToClient() {
		Long otherOrderId = 2L;
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", otherOrderId)
		.then()
		.statusCode(403);
		
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + adminToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", nonExistingOrderId)
		.then()
		.statusCode(404);
		
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndClientLogged() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + clientToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", nonExistingOrderId)
		.then()
		.statusCode(404);
		
	}
	
	@Test
	public void findByIdShouldReturnUnauthorizedWhenIdDoesNotExistsAndInvalidToken() {
		
		given()
		.header("Content-type", "application/json")
		.header("Authorization", "Bearer " + invalidToken)
		.accept(ContentType.JSON)
		.when()
		.get("/orders/{id}", existingOrderId)
		.then()
		.statusCode(404);
		
	}

}

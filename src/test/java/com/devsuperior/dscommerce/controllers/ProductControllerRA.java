package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dscommerce.tests.TokenUtil;

import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;

public class ProductControllerRA {

	private Long existingProductId;

	private Long nonExistingProductId;

	private Long dependentProductId;

	private String clientUserName;

	private String clientPassword;

	private String adminUserName;

	private String adminPassword;

	private String clientToken;

	private String adminToken;

	private String invalidToken;

	private String productName;

	private Map<String, Object> postProductInstance;

	@BeforeEach
	public void setUp() {
		baseURI = "http://localhost:8080";
		clientUserName = "maria@gmail.com";
		clientPassword = "123456";
		adminUserName = "alex@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUserName, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUserName, adminPassword);
		invalidToken = adminToken + "xpto";

		productName = "Macbook";

		postProductInstance = new HashMap<>();
		postProductInstance.put("name", "Meu produto");
		postProductInstance.put("description",
				"Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
		postProductInstance.put("imgUrl",
				"https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		postProductInstance.put("price", 50.0);

		List<Map<String, Object>> categories = new ArrayList<>();

		Map<String, Object> category1 = new HashMap<>();
		category1.put("id", 2);

		Map<String, Object> category2 = new HashMap<>();
		category2.put("id", 3);

		categories.add(category1);
		categories.add(category2);

		postProductInstance.put("categories", categories);

	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() {
		existingProductId = 2L;

		given().get("/products/{id}", existingProductId).then().statusCode(200).body("id", is(2))
				.body("name", equalTo("Smart TV"))
				.body("imgUrl", equalTo(
						"https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
				.body("price", is(2190.0F)).body("categories.id", hasItems(2, 3))
				.body("categories.name", hasItems("Eletrônicos", "Computadores"));
	}

	@Test
	public void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {
		given().get("/products?page=0").then().statusCode(200).body("content.name",
				hasItems("Macbook Pro", "PC Gamer Tera"));
	}

	@Test
	public void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {
		given().get("/products?name={productName}", productName).then().statusCode(200).body("content.id[0]", is(3))
				.body("content.name[0]", equalTo("Macbook Pro")).body("content.price[0]", is(1250.0F))
				.body("content.imgUrl[0]", equalTo(
						"https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
	}

	@Test
	public void findAllShouldReturnPageProductsWithPriceGreaterThan2000() {
		given().get("/products?size=25").then().statusCode(200).body("content.findAll { it.price > 2000 }.name",
				hasItems("Smart TV", "PC Gamer Weed"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {
		postProductInstance.put("name", "ab");

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(422).body("errors.message[0]", equalTo("Nome precisa ter de 3 a 80 caracteres"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {
		postProductInstance.put("description", "ab");

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(422).body("errors.message[0]", equalTo("Mensagem precisa ter de no mínimo 10 caracteres"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceNegative() {
		postProductInstance.put("price", -50.0);

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(422).body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsZero() {
		postProductInstance.put("price", 0);

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(422).body("errors.message[0]", equalTo("O preço deve ser positivo"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {
		postProductInstance.put("categories", null);

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(422).body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
	}

	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() {

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + clientToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(403);
	}

	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() {

		JSONObject newProduct = new JSONObject(postProductInstance);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + invalidToken)
				.body(newProduct).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/products").then()
				.statusCode(401);
	}

	@Test
	public void deleteShouldReturnNoContentWhenIdExistsAndAdminLogged() {
		existingProductId = 25L;

		given().header("Authorization", "Bearer " + adminToken).when().delete("/products/{id}", existingProductId)
				.then().statusCode(204);
	}

	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {
		nonExistingProductId = 100L;

		given().header("Authorization", "Bearer " + adminToken).when().delete("/products/{id}", nonExistingProductId)
				.then().statusCode(404).body("error", equalTo("Recurso não encontrado")).body("status", equalTo(404));
	}

	@Test
	public void deleteShouldReturnBadRequestWhenDependentIdExistsAndAdminLogged() {
		dependentProductId = 3L;

		given().header("Authorization", "Bearer " + adminToken).when().delete("/products/{id}", dependentProductId)
				.then().statusCode(400);
	}

	@Test
	public void deleteShouldReturnForbiddenWhenIdExistsAndClientLogged() {
		existingProductId = 25L;

		given().header("Authorization", "Bearer " + clientToken).when().delete("/products/{id}", existingProductId)
				.then().statusCode(403);
	}

	@Test
	public void deleteShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {
		existingProductId = 25L;

		given().header("Authorization", "Bearer " + invalidToken).when().delete("/products/{id}", existingProductId)
				.then().statusCode(401);
	}

}

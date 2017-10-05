package com.csye6225.demo.rest_assured;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;


import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
//import org.junit.Ignore;

import java.net.URI;
import java.net.URISyntaxException;
import com.csye6225.demo.bean.User;

public class RestApiTest  {


@Ignore
    @Test
    public void testGetHomePage() throws URISyntaxException {
        RestAssured.when().get(new URI("http://localhost:8080/")).then().statusCode(200);
    }
/*

    @Test
    public void makeSureThatGoogleIsUp() {
        RestAssured.when().get("http://www.google.com").then().statusCode(200);
    }*/
/*@Ignore
   @Test
    public void aCarGoesIntoTheGarage() {

        given()

                .contentType("application/json")
                .body("{\"UserName\":\"Jimi\"\"EmailId\":\"Jimi@gmail.com\"\"Password\":\"Jimi34\"}")
                .when().post("/user/register").then()
                .statusCode(200);
    }*/

}

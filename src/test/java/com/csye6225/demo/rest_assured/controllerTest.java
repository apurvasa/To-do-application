package com.csye6225.demo.rest_assured;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Ignore;

public class controllerTest {



@Ignore
        @BeforeClass
        public static void setup() {
            String port = System.getProperty("server.port");
            if (port == null) {
                RestAssured.port = Integer.valueOf(8080);
            }
            else{
                RestAssured.port = Integer.valueOf(port);
            }


            String basePath = System.getProperty("server.base");
            if(basePath==null){
                basePath = "//";
            }
            RestAssured.basePath = basePath;

            String baseHost = System.getProperty("server.host");
            if(baseHost==null){
                baseHost = "http://localhost";
            }
            RestAssured.baseURI = baseHost;

        }




}
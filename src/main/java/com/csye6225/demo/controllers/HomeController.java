package com.csye6225.demo.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

  private final static Logger logger = LoggerFactory.getLogger(HomeController.class);


  @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public String welcome() {

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("Home Page", "Welcome to CSYE 6225 Assignment_2 !!!");
    //jsonObject.addProperty("To Register New User Go To:", "/user/register/{name}/{email}/{password}");

    return jsonObject.toString();
  }

}

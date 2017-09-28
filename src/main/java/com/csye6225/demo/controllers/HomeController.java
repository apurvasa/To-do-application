package com.csye6225.demo.controllers;


import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.*;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;


@Controller
public class HomeController {

    @Autowired
    private UserDao userDao;

  private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

 @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public Object welcome(HttpServletRequest request) {



   final String authorization = request.getHeader("Authorization");
   if (authorization != null && authorization.startsWith("Basic")) {

       String base64Credentials = authorization.substring("Basic".length()).trim();
       String credentials = new String(Base64.getDecoder().decode(base64Credentials),
               Charset.forName("UTF-8"));

       final String[] values = credentials.split(":", 2);

       String email = values[0];
       String password = values[1];

       System.out.println(email);
       System.out.println(password);

       Iterable<User> lu = userDao.findAll();

       Iterator itr = lu.iterator();
       do{

           User u1 = (User)itr.next();

           if(u1.getEmail().equalsIgnoreCase(email)){

               if(BCrypt.checkpw(password, u1.getPassword())){


                   JsonObject jo = new JsonObject();
                   jo.addProperty("Login ", "Welcome "+u1.getUserName()+" You Are Logged In");
                   jo.addProperty("Current Time ",""+ new Date().toString());

                   System.out.println(jo.toString());

                   return jo.toString();


               }else{

                   JsonObject j = new JsonObject();
                   j.addProperty("Error","Password Doesn't Match");
                   return j.toString();
               }
           }

       }
       while(itr.hasNext());

       JsonObject j = new JsonObject();
       j.addProperty("Error","User With Given Email "+email+" Doest Exist!!!");

       return j.toString();

   }else{

       JsonObject j = new JsonObject();
       j.addProperty("Error","Unauthorized User: You Are Not Logged In");


       return j.toString();
   }



   /* JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("Home Page", "Welcome to CSYE 6225 Assignment_2 !!!");
    jsonObject.addProperty("To Register New User Go To:", "/user/register/{name}/{email}/{password}");
    jsonObject.addProperty("To Login Go To", "/user/login/{email}/{password}");
    jsonObject.addProperty("To Logout Go To", "/user/logout");
    return jsonObject.toString();*/
  }

}

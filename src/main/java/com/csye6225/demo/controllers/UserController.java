package com.csye6225.demo.controllers;

import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.UserDao;
import com.google.gson.*;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.coyote.Constants;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.tomcat.util.http.parser.Authorization;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

import java.util.Iterator;
import java.util.Iterator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
public class UserController{


    @Autowired
    private UserDao userDao;





    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public Object register(@RequestBody JSONObject jo) {
      //  public String register (@RequestBody String jo){
            System.out.println(jo.toString());


            String userName = jo.get("UserName").toString();
            String email = jo.get("EmailId").toString();
            String password = jo.get("Password").toString();

            Iterable<User> users = userDao.findAll();
            Iterator itr = users.iterator();

            while (itr.hasNext()) {


                    User user = (User) itr.next();

                    if (user.getEmail().equalsIgnoreCase(email)) {

                        return "User already exist!!!";
                    }



            }

            String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            System.out.println(pw_hash);

            User u = new User();
            u.setUserName(userName);
            u.setEmail(email);
            u.setPassword(pw_hash);

            userDao.save(u);

            System.out.println("User " + userName + " is registered successfully!!");

            JsonObject j = new JsonObject();
            j.addProperty("Message", "User " +userName+ " is registered successfully!!");

            return jo;





        }



  /*  @RequestMapping(value = "/user/login/{email}/{password}", method = RequestMethod.POST)
    public Object login(@PathVariable String email, @PathVariable String password){



        Iterable<User> lu = userDao.findAll();

        Iterator itr = lu.iterator();
        do{

          User u1 = (User)itr.next();

            if(u1.getEmail().equalsIgnoreCase(email)){

                if(BCrypt.checkpw(password, u1.getPassword())){

                    Gson gs = new Gson();
                    String jsonString = gs.toJson(u1);
                    JsonParser parser = new JsonParser();
                    JsonObject json = (JsonObject) parser.parse(jsonString);

                    JsonObject jo = new JsonObject();
                    jo.addProperty("Login ", "Welcome "+u1.getUserName()+" you are logeed in");


                    JsonArray array = new JsonArray();
                    array.add(jo);
                    array.add(json);

                    System.out.println(array.toString());

                    return array;


                }else{

                    return "Password Doesn't Match";
                }
            }

        }
        while(itr.hasNext());


        return "User With Given Email "+email+" Doest Exist!!!";
    }
*/



}




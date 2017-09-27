package com.csye6225.demo.controllers;

import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.UserDao;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import java.util.Iterator;


@RestController
public class UserController extends HttpServlet{

    @Autowired
    private UserDao userDao;

    @RequestMapping(value = "/user/register/{name}/{password}", method = RequestMethod.POST)
    public String register(@PathVariable String name, @PathVariable String password) {


        System.out.println("inside method");

        Iterable<User> users = userDao.findAll();
        Iterator itr = users.iterator();

        do {

            User user = (User) itr.next();

            if (user.getUserName().equalsIgnoreCase(name)) {

                return "User already exist!!!";
            }


        } while (itr.hasNext());

        String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        System.out.println(pw_hash);

        User u = new User();
        u.setUserName(name);

        u.setPassword(pw_hash);

        userDao.save(u);

        System.out.println("User " + name + " is registered successfully!!");

        return "User " + name + " is registered successfully!!!";



    }




}

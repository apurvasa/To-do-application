package com.csye6225.demo.controllers;

import com.csye6225.demo.bean.TodoTask;
import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.TaskDao;
import com.csye6225.demo.dao.UserDao;
import com.csye6225.demo.services.GenerateUUID;
import com.google.gson.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;



@RestController
public class UserController{


    @Autowired
    private UserDao userDao;

    @Autowired
    private GenerateUUID generateUUID;

    @Autowired
    private TaskDao taskDao;




    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public Object register(@RequestBody JSONObject jo) {

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

    @RequestMapping(value = "/tasks" ,method = RequestMethod.POST)
    public String createTask(@RequestBody JSONObject jsonObject,HttpServletRequest request, HttpServletResponse response) {


        String description = jsonObject.get("Description").toString();

        int descriptionLength = description.length();

        if (descriptionLength < 4097) {


            String id = generateUUID.getUUID();

            TodoTask tt = new TodoTask(id, description);

            taskDao.save(tt);

            response.setStatus(201);


            return "Task Added Succsesfully" + "Your To Do Task Id is : " +id;
        }else{

            response.setStatus(400);

            return "Description Should have less than 4096 Characters ";
        }
    }

    @RequestMapping(value = "/tasks/{id}" ,method = RequestMethod.PUT)
    public String updateTask(@RequestBody JSONObject jsonObject, @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {


        String taskId = id;

        String description = jsonObject.get("Description").toString();



        boolean exist = taskDao.exists(new Long(taskId).longValue());

        if(exist == true){

            TodoTask tt = new TodoTask(taskId,description);
            taskDao.save(tt);

            response.setStatus(200);

            return "Update to the Task Id: " +taskId+ " has been done";
        }else{

            response.setStatus(400);

            return "Given Task Id doesn't exists";
        }
    }


    }




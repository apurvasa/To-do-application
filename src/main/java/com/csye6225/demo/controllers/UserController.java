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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;



@RestController
public class UserController {


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
        j.addProperty("Message", "User " + userName + " is registered successfully!!");

        return jo;


    }

    @RequestMapping(value = "/tasks", method = RequestMethod.POST)
    public String createTask(@RequestBody JSONObject jsonObject, HttpServletRequest request, HttpServletResponse response) {


        String description = jsonObject.get("Description").toString();

        int descriptionLength = description.length();

        if (descriptionLength < 4097) {


            String id = generateUUID.getUUID();

            TodoTask tt = new TodoTask(id, description);

            taskDao.save(tt);

            response.setStatus(201);


            return "Task Added Succsesfully" + "Your To Do Task Id is : " + id;
        } else {

            response.setStatus(400);

            return "Description Should have less than 4096 Characters ";
        }
    }

    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.PUT)
    public String updateTask(@RequestBody JSONObject jsonObject, @PathVariable(value = "id") String id, HttpServletRequest request, HttpServletResponse response) {


        String taskId = id;

        String description = jsonObject.get("Description").toString();

        Iterable<TodoTask> tasks = taskDao.findAll();

        Iterator itr = tasks.iterator();

        while (itr.hasNext()) {

            TodoTask todo = (TodoTask) itr.next();

            if (todo.getId().equalsIgnoreCase(taskId)) {

                TodoTask tt = new TodoTask(taskId, description);

                taskDao.save(tt);

                response.setStatus(200);

                return "Update to the Task Id: " + taskId + " has been done";

            }
        }

        response.setStatus(400);

        return "Given Task Id doesn't exists";

    }


    @RequestMapping(value = "/tasks/{id}/attachments", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    @ResponseBody
    public String addAttachments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id, @RequestParam("name") String name,
                                 @RequestParam("file") MultipartFile file) {


        if (!file.isEmpty()) {
            System.out.println("inside if ");

            try {
                //String rootPath = request.getServletContext().getRealPath("/");
                String fileName = file.getOriginalFilename();

//                System.out.println("orig path"+fileName);
//                File destinationfile= new File(rootPath+File.separator+fileName);


                byte[] bytes = file.getBytes();
                Path path = Paths.get("//home//riddhi//Desktop//tmpFiles//"+fileName);
                Files.write(path, bytes);




                String idd = generateUUID.getUUID();
                TodoTask tt = new TodoTask(idd, "description");
                tt.setTaskAttachments(path.toString());
                taskDao.save(tt);
                System.out.println("You successfully uploaded file=" + name);
                response.setStatus(200);
                return "Saved";
            } catch (Exception e) {
                System.out.println(e.getMessage());
                response.setStatus(400);
                        return "Bad Request";
            }
        } else {
            System.out.println("You failed to upload " + name + " because the file was empty.");
            return "File Empty";
        }



    }



}






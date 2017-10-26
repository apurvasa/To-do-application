package com.csye6225.demo.controllers;

import com.csye6225.demo.bean.TaskAttachments;
import com.csye6225.demo.bean.TodoTask;
import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.AttachmentsDao;
import com.csye6225.demo.dao.TaskDao;
import com.csye6225.demo.dao.UserDao;
import com.csye6225.demo.services.GenerateUUID;
import com.google.gson.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.json.simple.JSONArray;
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
import java.nio.charset.Charset;
import java.util.*;


@RestController
public class UserController {


    @Autowired
    private UserDao userDao;

    @Autowired
    private GenerateUUID generateUUID;

    @Autowired
    private TaskDao taskDao;


    @Autowired
    private AttachmentsDao attachmentsDao;

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

                JsonObject j = new JsonObject();
                j.addProperty("Message", "User already exist!!!");

                return jo;

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
    public Object createTask(@RequestBody JSONObject jsonObject, HttpServletRequest request, HttpServletResponse response) {

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
            do {

                User u1 = (User) itr.next();

                if (u1.getEmail().equalsIgnoreCase(email)) {

                    if (BCrypt.checkpw(password, u1.getPassword())) {

                        String description = jsonObject.get("Description").toString();

                        int descriptionLength = description.length();

                        if (descriptionLength < 4097) {


                            String id = generateUUID.getUUID();

                            TodoTask tt = new TodoTask(id, description);

                            u1.getTodoTasks().add(tt);

                            tt.setUsers(u1);

                            taskDao.save(tt);

                            response.setStatus(201);

                            JsonObject j = new JsonObject();
                            j.addProperty("Information", "Task Added on the id :" + id);
                            return j.toString();
                        } else {

                            response.setStatus(400);

                            JsonObject j = new JsonObject();
                            j.addProperty("Information", "Description Should have less than 4096 Characters");
                            return j.toString();

                        }


                    } else {

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Password Doesn't Match");
                        return j.toString();
                    }
                }

            }
            while (itr.hasNext());

            JsonObject j = new JsonObject();
            j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");

            return j.toString();

        } else {

            JsonObject j = new JsonObject();
            j.addProperty("Error", "Unauthorized User: You Are Not Logged In");

        }



        return null;
    }

    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.PUT)
    public String updateTask(@RequestBody JSONObject jsonObject, @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {

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

            do {

                User u1 = (User) itr.next();

                if (u1.getEmail().equalsIgnoreCase(email)) {

                    if (BCrypt.checkpw(password, u1.getPassword())) {

                        String taskId = id;

                        String description = jsonObject.get("Description").toString();

                        Iterable<TodoTask> tasks = taskDao.findAll();

                        Iterator itr1 = tasks.iterator();

                        while (itr1.hasNext()) {

                            TodoTask todo = (TodoTask) itr1.next();

                            if (todo.getId().equalsIgnoreCase(taskId) && todo.getUsers()==u1) {

                                Long userId = todo.getUsers().getUserId();

                                System.out.println(userId);

                                User user1 = userDao.findOne(userId);

                                TodoTask tt = new TodoTask(taskId, description);

                                user1.getTodoTasks().add(tt);

                                tt.setUsers(user1);

                                taskDao.save(tt);

                                response.setStatus(200);

                                JsonObject j = new JsonObject();
                                j.addProperty("Information", "Update to the Task Id: " + taskId + " has been done");
                                return j.toString();


                            }
                        }

                        response.setStatus(400);

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Given Task Id doesn't exists");
                        return j.toString();


                    }else {

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Password Doesn't Match");
                        return j.toString();
                    }
                }

            } while (itr.hasNext());

            JsonObject j = new JsonObject();
            j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");

            return j.toString();
        } else {

            JsonObject j = new JsonObject();
            j.addProperty("Error", "Unauthorized User: You Are Not Logged In");

            return j.toString();
        }


    }

    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.DELETE)
    public String deleteTask(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {


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

            do {

                User u1 = (User) itr.next();

                if (u1.getEmail().equalsIgnoreCase(email)) {

                    if (BCrypt.checkpw(password, u1.getPassword())) {



                        String taskId = id;

                        Iterable<TodoTask> tasks = taskDao.findAll();

                        Iterator itr1 = tasks.iterator();

                        while (itr1.hasNext()) {

                            TodoTask todoTask = (TodoTask) itr1.next();

                            if (todoTask.getId().equalsIgnoreCase(taskId) && todoTask.getUsers()==u1) {

                                taskDao.delete(todoTask);
                                response.setStatus(204);

                                JsonObject j = new JsonObject();
                                j.addProperty("Information", "Task Id: " +taskId+ " has been deleted");
                                return j.toString();


                            }
                        }

                        response.setStatus(400);

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Given Task Id doesn't exists");
                        return j.toString();


                    }else {

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Password Doesn't Match");
                        return j.toString();
                    }
                }

            } while (itr.hasNext());

            JsonObject j = new JsonObject();
            j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");

            return j.toString();
        } else {

            JsonObject j = new JsonObject();
            j.addProperty("Error", "Unauthorized User: You Are Not Logged In");

            return j.toString();
        }


    }



    @RequestMapping(value = "/tasks/{id}/attachments", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    @ResponseBody
    public String addAttachments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id,
                                 @RequestParam("file") MultipartFile file) {


        if (!file.isEmpty()) {
            System.out.println("inside if ");

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

                do {

                    User u1 = (User) itr.next();

                    if (u1.getEmail().equalsIgnoreCase(email)) {

                        if (BCrypt.checkpw(password, u1.getPassword())) {

                            try {
                                System.out.println("id and pass correct");
                                String taskId = id;
                                boolean flag = false;
                                Iterable<TodoTask> tasks = taskDao.findAll();

                                Iterator itr1 = tasks.iterator();

                                while (itr1.hasNext()) {

                                    TodoTask todoTask = (TodoTask) itr1.next();


                                    if (todoTask.getId().equalsIgnoreCase(taskId) && todoTask.getUsers()==u1) {


                                        String fileName = file.getOriginalFilename();


                                        byte[] bytes = file.getBytes();
                                        Path path = Paths.get(fileName);
                                        Files.write(path, bytes);

                                        TaskAttachments ta = new TaskAttachments();

                                        ta.setPath(path.toString());
                                        ta.setId(generateUUID.getUUID());
                                        ta.setTodoTask(todoTask);

                                        List<TaskAttachments> tal = new ArrayList<TaskAttachments>();
                                        tal.add(ta);
                                        todoTask.setTaskAttachments(tal);
                                        attachmentsDao.save(ta);

                                        flag = true;
                                        System.out.println("You successfully uploaded file");
                                        response.setStatus(200);
                                        JsonObject j = new JsonObject();
                                        j.addProperty("Information", "Saved");
                                        return j.toString();



                                    }
                                }
                                if (!flag){
                                    JsonObject j = new JsonObject();
                                    j.addProperty("Error", "ID does not exists");
                                    return j.toString();

                                }


                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                response.setStatus(400);
                                JsonObject j = new JsonObject();
                                j.addProperty("Error", "Bad Request");
                                return j.toString();

                            }
                        }else {

                            JsonObject j = new JsonObject();
                            j.addProperty("Error", "Password Doesn't Match");
                            return j.toString();
                        }

                    }
                }while (itr.hasNext());

                JsonObject j = new JsonObject();
                j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");
                return j.toString();

            } else {

                JsonObject j = new JsonObject();
                j.addProperty("Error", "Unauthorized User: You Are Not Logged In");
                return j.toString();
            }

        }else {
            System.out.println("You failed to upload  because the file was empty.");

            JsonObject j = new JsonObject();
            j.addProperty("Error", "File Empty");
            return j.toString();

        }


    }







    @RequestMapping(value = "/tasks/{id}/attachments/{idAttachments}", method = RequestMethod.DELETE)
    public String deleteAttachment(@PathVariable("id") long todotaskid,@PathVariable("idAttachments") String idAttachments, HttpServletRequest request, HttpServletResponse response) {


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

            Iterator itr1 = lu.iterator();

            do {

                User u1 = (User) itr1.next();

                if (u1.getEmail().equalsIgnoreCase(email)) {

                    if (BCrypt.checkpw(password, u1.getPassword())) {

                        try {

                            String attachmentId = idAttachments;

                            TodoTask task=taskDao.findOne(todotaskid);

                            Iterable<TaskAttachments> attachments = attachmentsDao.findAll();

                            Iterator itr = attachments.iterator();

                            while (itr.hasNext()) {

                                TaskAttachments taskAttachments = (TaskAttachments) itr.next();


                                if (taskAttachments.getId().equalsIgnoreCase(attachmentId) && taskAttachments.getTodoTask()==task && task.getUsers()==u1) {


                                    attachmentsDao.delete(taskAttachments);

                                    response.setStatus(204);

                                    JsonObject j = new JsonObject();
                                    j.addProperty("Information", "Attachment with  Id: " + attachmentId + " has been deleted");
                                    return j.toString();



                                }
                            }



                            response.setStatus(400);

                            JsonObject j = new JsonObject();
                            j.addProperty("Error", "Given Task Id doesn't exists");
                            return j.toString();




                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            response.setStatus(400);
                            JsonObject j = new JsonObject();
                            j.addProperty("Error", "Bad Request");
                            return j.toString();

                        }
                    }else {

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Password Doesn't Match");
                        return j.toString();
                    }

                }
            }while (itr1.hasNext());

            JsonObject j = new JsonObject();
            j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");

            return j.toString();
        } else {

            JsonObject j = new JsonObject();
            j.addProperty("Error", "Unauthorized User: You Are Not Logged In");
            return j.toString();
        }


    }






    @RequestMapping(value = "/tasks/{id}/attachments", method = RequestMethod.GET)
    public Object getAttachment(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {


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

            Iterator itr1 = lu.iterator();

            do {

                User u1 = (User) itr1.next();

                if (u1.getEmail().equalsIgnoreCase(email)) {

                    if (BCrypt.checkpw(password, u1.getPassword())) {

                        try {

                            String taskId = id;

                            Iterable<TodoTask> tasks = taskDao.findAll();

                            Iterator itr = tasks.iterator();

                            while (itr.hasNext()) {

                                TodoTask todoTask = (TodoTask) itr.next();

                                if (todoTask.getId().equalsIgnoreCase(taskId) && todoTask.getUsers()==u1) {

                                    List<TaskAttachments> tal;

                                    tal=todoTask.getTaskAttachments();

                                    JSONArray ja =new JSONArray();
                                   //JsonArray ja = new JsonArray();


                                    for(TaskAttachments ta : tal){

                                        JsonObject jo = new JsonObject();
                                        jo.addProperty("AttachmentID",ta.getId());
                                        jo.addProperty("Path",ta.getPath());
                                    ja.add(jo);


                                    }



                                    response.setStatus(200);

                                    return ja.toString() ;

                                }
                            }



                            response.setStatus(401);

                            JsonObject j = new JsonObject();
                            j.addProperty("Error", "Given Task Id doesn't exists");
                            return j.toString();






                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            response.setStatus(400);
                            return "Bad Request";
                        }
                    } else {

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Password Doesn't Match");
                        return j.toString();
                    }

                }
            } while (itr1.hasNext());

            JsonObject j = new JsonObject();
            j.addProperty("Error", "User With Given Email " + email + " Doest Exist!!!");

            return j.toString();
        } else {

            JsonObject j = new JsonObject();
            j.addProperty("Error", "Unauthorized User: You Are Not Logged In");
            return j.toString();
        }


    }
}





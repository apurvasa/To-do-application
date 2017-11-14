package com.csye6225.demo.controllers;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.csye6225.demo.bean.TaskAttachments;
import com.csye6225.demo.bean.TodoTask;
import com.csye6225.demo.bean.User;
import com.csye6225.demo.dao.AttachmentsDao;
import com.csye6225.demo.dao.TaskDao;
import com.csye6225.demo.dao.UserDao;
import com.csye6225.demo.services.GenerateUUID;
import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.*;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

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

//    @Autowired
//    S3Client s3Client;

    HttpServletRequest request;
    HttpServletResponse response;

    private static final String SUFFIX = "/";



    AmazonS3 s3client= AmazonS3ClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(false)).build();

    String bucketName = "code-deploy.csye6225-fall2017-chabhadiar.me.com";




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

                            if (todo.getId().equalsIgnoreCase(taskId) && todo.getUsers() == u1) {

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


                    } else {

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

                            if (todoTask.getId().equalsIgnoreCase(taskId) && todoTask.getUsers() == u1) {

                                taskDao.delete(todoTask);
                                response.setStatus(204);

                                JsonObject j = new JsonObject();
                                j.addProperty("Information", "Task Id: " + taskId + " has been deleted");
                                return j.toString();


                            }
                        }

                        response.setStatus(400);

                        JsonObject j = new JsonObject();
                        j.addProperty("Error", "Given Task Id doesn't exists");
                        return j.toString();


                    } else {

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



    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    @ResponseBody
    public String reset(@RequestBody JSONObject jo, HttpServletResponse response) {


        boolean flag=false;

        System.out.println(jo.toString());
        String userName = jo.get("Username").toString();
        AmazonSNSClient snsClient = new AmazonSNSClient(new DefaultAWSCredentialsProviderChain());
        String top=snsClient.createTopic("csye6225-Topic").getTopicArn();
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        String topicArn = top;

        Iterable<User> lu = userDao.findAll();
        Iterator itr = lu.iterator();

        do {

            User u1 = (User) itr.next();

            if (u1.getEmail().equalsIgnoreCase(userName)) {
                flag=true;
                return  "Email sent";
            }
        }
        while (itr.hasNext());

        if(flag==false){
            PublishRequest publishRequest = new PublishRequest(topicArn, userName);
            PublishResult publishResult = snsClient.publish(publishRequest);
            System.out.println("MessageId - " + publishResult.getMessageId());}
        response.setStatus(200);
        JsonObject j = new JsonObject();
        j.addProperty("Information", "Reset Link Sent");
        return j.toString();

    }



    public static void createFolder(String bucketName, MultipartFile multipartfile) {
//        // create meta-data for your folder and set content-length to 0
//        System.out.println("entered in fn");
//        String buc=System.getProperty("bucket.name");
//        System.out.println("buckettt"+buc);
//        //  ObjectMetadata metadata = new ObjectMetadata();
//        //  metadata.setContentLength(0);
//        // create empty content
//        //  InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
//        // create a PutObjectRequest passing the folder name suffixed by /
//        System.out.println("inputstream created");
//        PutObjectRequest putObjectRequest = new PutObjectRequest(buc, "test", folderName);
//        // send request to S3 to create folder
//        System.out.println("send request to s3");
//        client.putObject(putObjectRequest);
//        System.out.println("put object");
        AmazonS3 s3client= AmazonS3ClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(false)).build();

        File newFile=new File(multipartfile.getOriginalFilename());
        try {
            newFile.createNewFile();
            FileOutputStream fs=new FileOutputStream(newFile);
            fs.write(multipartfile.getBytes());
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        s3client.putObject(new PutObjectRequest(bucketName,newFile.getName(),newFile));

    }



    @RequestMapping(value = "/tasks/{id}/attachments", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
    public String addAttachments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id,
                                 @RequestParam("file") MultipartFile file) {

        System.out.println("hii");


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

                                    System.out.println("task id: " + todoTask.getId());
                                    if (todoTask.getId().equalsIgnoreCase(taskId)) {
                                        //create folder on s3 bucket
                                        //  createFolder(bucketName, taskId, s3client);

                                        String fileName = file.getOriginalFilename();
                                        Path path = Paths.get(fileName);
                                        byte[] bytes = file.getBytes();

                                        Files.write(path, bytes);

//                                        //  String fileName = folderName + SUFFIX + "testvideo.mp4";
//                                        s3client.putObject(new PutObjectRequest(bucketName, fileName, new File(fileName)));
//
//                                        AmazonS3 s3client = s3Client.getS3Client();
//
//                                        //     List<Bucket> buckets = s3client.listBuckets();
//
//                                        String bucketName = "code-deploy.csye6225-fall2017-patelshu.me";
//
//                                        String folderName = "FileFolder";
//                                        createFolder(bucketName, folderName, s3client);
//
//                                        String folderToPut = folderName + SUFFIX + fileName;
//
//                                        File f = new File(fileName);
//                                        file.transferTo(f);
//
//                                        s3client.putObject(new PutObjectRequest(bucketName, folderToPut, f));


                                        String buc=System.getProperty("bucket.name");
                                        createFolder(buc, file);
                                        //  String fileName = folderName + SUFFIX + "testvideo.mp4";
                                        // s3client.putObject(new PutObjectRequest(bucketName, fileName, new File(fileName)));

                                        // AmazonS3 s3client = s3Client.getS3Client();

                                        //     List<Bucket> buckets = s3client.listBuckets();

                                        //String bucketName = "code-deploy.csye6225-fall2017-patelshu.me";

                                        // String folderName = "FileFolder";
                                        //createFolder(bucketName, folderName, s3client);

                                        // String folderToPut = folderName + SUFFIX + fileName;

                                        //File f = new File(fileName);
                                        //file.transferTo(f);

                                        //s3client.putObject(new PutObjectRequest(bucketName, folderToPut, f));




                                        TaskAttachments ta = new TaskAttachments();
                                        ta.setPath("https://s3.amazonaws.com/"+buc+"/"+fileName);
                                        //  ta.setPath(path.toString());
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
                                if (!flag) {
                                    JsonObject j = new JsonObject();
                                    j.addProperty("Error", "ID does not exists");
                                    return j.toString();


                                }


                            } catch (Exception e) {
                                System.out.println(e);
                                response.setStatus(400);
                                JsonObject j = new JsonObject();
                                j.addProperty("Error", "Bad Request");
                                return j.toString();

                            }
                        } else {

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

        } else {
            System.out.println("You failed to upload  because the file was empty.");

            JsonObject j = new JsonObject();
            j.addProperty("Error", "File Empty");
            return j.toString();

        }


    }


    @RequestMapping(value = "/tasks/{id}/attachments/{idAttachments}", method = RequestMethod.DELETE)
    public String deleteAttachment(@PathVariable("id") String todotaskid, @PathVariable("idAttachments") String idAttachments, HttpServletRequest request, HttpServletResponse response) {


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

                            Iterable<TodoTask> tasks = taskDao.findAll();

                            Iterator itr2 = tasks.iterator();

                            while (itr2.hasNext()) {

                                TodoTask todoTask = (TodoTask) itr2.next();


                                //  s3client.deleteObject(bucketName, fileName);


                                boolean flag1 = false;
                                TaskAttachments tobedeletd = null;

                                if (todoTask.getId().equalsIgnoreCase(todotaskid) && todoTask.getUsers() == u1) {
                                    List<TaskAttachments> attachlist = new ArrayList<TaskAttachments>();
                                    for (TaskAttachments att : attachlist) {
                                        if (att.getId().equalsIgnoreCase(attachmentId)) {

                                            flag1 = true;
                                            tobedeletd = att;
                                        }
                                    }
                                    if (flag1) {
                                        attachmentsDao.delete(tobedeletd);
                                    }

                                    System.out.println("You successfully deleted file");
                                    response.setStatus(200);
                                    return "deleted";


                                } //else
                                //return "ID does not exists";

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

                                if (todoTask.getId().equalsIgnoreCase(taskId) && todoTask.getUsers() == u1) {

                                    List<TaskAttachments> tal;

                                    tal = todoTask.getTaskAttachments();

                                    JSONArray ja = new JSONArray();
                                    //JsonArray ja = new JsonArray();


                                    for (TaskAttachments ta : tal) {

                                        JsonObject jo = new JsonObject();
                                        jo.addProperty("AttachmentID", ta.getId());
                                        jo.addProperty("Path", ta.getPath());
                                        ja.add(jo);


                                    }


                                    response.setStatus(200);

                                    return ja.toString();

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



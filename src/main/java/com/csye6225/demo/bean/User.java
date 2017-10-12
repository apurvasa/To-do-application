package com.csye6225.demo.bean;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="userTable")
public class User {


    public User(){}

    public User(Long userId,String userName, String email,String password){


        this.userId = userId;
        this.email=email;
        this.userName=userName;
        this.password=password;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userID", unique = true)
     private Long userId;

    @Column(name="userName")
    private String userName;

    @Column(name="emailId")
    private String email;

    @Column(name = "password")
    private String password;

    @OneToMany(mappedBy = "users")
    private List<TodoTask> todoTasks = new ArrayList<TodoTask>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<TodoTask> getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(List<TodoTask> todoTasks) {
        this.todoTasks = todoTasks;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}

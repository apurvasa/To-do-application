package com.csye6225.demo.bean;



import javax.persistence.*;

@Entity
@Table(name="userTable")
public class User {


    public User(){}

    public User(int userId, String userName, String email, String password){

        this.userId = userId;
        this.userName=userName;
        this.password=password;

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userID", unique = true)
     private int userId;

    @Column(name="userName")
    private String userName;

    @Column(name="emailId")
    private String email;

    @Column(name = "password")
    private String password;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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
}

package com.csye6225.demo.bean;

import javax.persistence.*;

@Entity
@Table(name="todoTable")
public class TodoTask {

    public TodoTask(){}

    public TodoTask(String id, String description){
        this.id = id;
        this.description = description;
    }

    @Id
    @Column(name = "taskID")
    private String id;

    @Column(name="description")
    private String description;

    @ManyToOne
    @JoinColumn(name="userID",nullable = false)
    private User users;

    public User getUsers() {
        return users;
    }

    public void setUsers(User users) {
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package com.csye6225.demo.bean;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="taskAttachments")
public class TaskAttachments {

    public TaskAttachments(){}

    public TaskAttachments(String path){
        this.path = path;

    }


    @Id
    @Column(name="attachmentId")
    private String id;

    @Column(name="path")
    private String path;



    @ManyToOne
    @JoinColumn(name="taskID",nullable = false)
    private TodoTask todoTask;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TodoTask getTodoTask() {
        return todoTask;
    }

    public void setTodoTask(TodoTask todoTask) {
        this.todoTask = todoTask;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

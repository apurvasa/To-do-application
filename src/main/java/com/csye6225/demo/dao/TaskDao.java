package com.csye6225.demo.dao;

import com.csye6225.demo.bean.TodoTask;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public interface TaskDao extends CrudRepository<TodoTask, Long>{


}

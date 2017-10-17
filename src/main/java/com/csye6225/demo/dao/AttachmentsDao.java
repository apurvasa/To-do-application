package com.csye6225.demo.dao;


import com.csye6225.demo.bean.TaskAttachments;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public interface AttachmentsDao extends CrudRepository<TaskAttachments, Long> {
}





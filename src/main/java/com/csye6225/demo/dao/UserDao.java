package com.csye6225.demo.dao;


import com.csye6225.demo.bean.User;
import org.springframework.data.repository.CrudRepository;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public interface UserDao extends CrudRepository<User, Long> {

    public User findByEmail(String email);

}

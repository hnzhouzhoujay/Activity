package com.baimi.activiti.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.baimi.activiti.entity.User;


public interface UserDao{
	public List<User> queryAll();
	public User queryUser(String name);
	public void insertUser(User user);
	public void updateUser(User user);
	public void deleteUser(User user);
	public User queryUserById(int userId);

}

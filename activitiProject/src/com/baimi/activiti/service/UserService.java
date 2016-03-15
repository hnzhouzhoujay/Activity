package com.baimi.activiti.service;

import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

import com.baimi.activiti.entity.User;

public interface UserService {
	public ProcessInstance  startProcess(User user,Map<String,Object> variables,String applyUserId);
	public List<User> findTodoTask(String userId);
	public void claimTask(String taskId,String userId);
	public User findUserById(int userId);
	public void complete(String taskId,User user,Map<String,Object> variables,boolean saveEntity,String comment);
	public boolean unclaimTask(String taskId,String userId);
}

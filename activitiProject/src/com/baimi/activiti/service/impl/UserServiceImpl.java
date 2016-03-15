package com.baimi.activiti.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baimi.activiti.dao.UserDao;
import com.baimi.activiti.entity.User;
import com.baimi.activiti.service.UserService;
@Service("userServiceImpl")
public class UserServiceImpl implements UserService{
	@Autowired
	UserDao userDao;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	IdentityService  identityService;
	@Autowired
	HistoryService  historyService;
	@Autowired
	TaskService  taskService;
	
	public int saveUser(User user){
		userDao.insertUser(user);
		User savedUser=userDao.queryUser(user.getName());
		return savedUser.getId();
	}
	
	public ProcessInstance  startProcess(User user,Map<String,Object> variables,String applyUserId){
		if(user.getId()==null){
			user.setApplyUserId(applyUserId);
			user.setApplyTime(new Date());
		}
		identityService.setAuthenticatedUserId(applyUserId);
		userDao.insertUser(user);
		User savedUser=userDao.queryUser(user.getName());
		String businessKey=""+savedUser.getId();
		ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("userAudit", businessKey, variables);
		String processInstanceId=processInstance.getId();
		savedUser.setProcessInstanceId(processInstanceId);
		userDao.updateUser(user);
		return processInstance;
	}
	public List<User> findTodoTask(String userId){
		List<User> result=new ArrayList<User>();
		List<Task> tasklist=new ArrayList<Task>();
		//签收的任务
		List<Task> assignTask=taskService.createTaskQuery().taskAssignee(userId).list();
		//未签收的任务
		List<Task> unassignTask=taskService.createTaskQuery().taskCandidateUser(userId).list();
		tasklist.addAll(assignTask);
		tasklist.addAll(unassignTask);
		for (Task task : tasklist) {
			//流程定义ID
			String processDefineId=task.getProcessDefinitionId();
			//流程实例ID
			String processInstanceId=task.getProcessInstanceId();
			//找到对应的流程实例
			ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
			//通过流程实例找到对应businesskey即userId
			String businesskey=processInstance.getBusinessKey();
			User user=userDao.queryUserById(Integer.parseInt(businesskey));
			user.setProcessDefineId(processDefineId);
			user.setProcessInstanceId(processInstanceId);
			String taskId=task.getId();
			user.setTaskId(taskId);
			user.setTask(task);
			result.add(user);
			//任务ID
		}
		return result;
	}
	public void claimTask(String taskId,String userId){
		Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
		//如果任务已经被签收了
		if(StringUtils.isNotEmpty(task.getAssignee())){
			return;
		}
		taskService.claim(taskId, userId);
	}
	
	public boolean unclaimTask(String taskId,String userId){
		List<IdentityLink> list=taskService.getIdentityLinksForTask(taskId);
		for (IdentityLink identityLink : list) {
			//如果任务有候选者 
			if(IdentityLinkType.CANDIDATE.equals(identityLink.getType())){
				taskService.claim(taskId, null);
				return true;
			}
		}
		return false;
	}

	@Override
	public User findUserById(int userId) {
		User user=userDao.queryUserById(userId);
		return user;
	}

	@Override
	public void complete(String taskId, User user,
			Map<String, Object> variables, boolean saveEntity,String comment) {
			if(saveEntity){
				userDao.updateUser(user);
			}
			if(!StringUtils.isEmpty(comment)){
				Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
				taskService.addComment(taskId, task.getProcessInstanceId(), comment);
				
			}
			taskService.complete(taskId, variables);
	}
}

package com.baimi.activiti.listener;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baimi.activiti.entity.User;
import com.baimi.activiti.service.UserService;

@Service
public class AuditCompleteListener implements TaskListener,Serializable{

	/**
	 * 
	 */
	@Autowired
	UserService userService;
	private static final long serialVersionUID = 1L;
	@Override
	public void notify(DelegateTask delegateTask) {
		User user=userService.findUserById(Integer.parseInt(delegateTask.getExecution().getProcessBusinessKey()));
		System.out.println("task complete "+user.getName());
	}
}

package com.baimi.activiti.controller;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestActiviti {
	public static void main(String[] args) {
		//根据配置文件构建引擎
		ProcessEngine processEngine=ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml").buildProcessEngine();
		RepositoryService repositoryService=processEngine.getRepositoryService();
		String path="MyProcess.bpmn";
		//发布流程定义
		repositoryService.createDeployment().addInputStream("MyProcess.bpmn",
				TestActiviti.class.getClassLoader().getResourceAsStream(path)).deploy();
		ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().singleResult();
		System.out.println("流程名称:"+processDefinition.getKey());//流程的key
		RuntimeService runtimeService=processEngine.getRuntimeService();
		Map<String,Object> variables=new HashMap<String,Object>();
		variables.put("applyUser", "deptLeader");
		variables.put("days", 3);
		//开始流程
		ProcessInstance processInstance=runtimeService.startProcessInstanceByKey("myProcess", variables);
		System.out.println("processINID"+processInstance.getProcessInstanceId()+",processID:"+processInstance.getProcessDefinitionId());
		TaskService taskService=processEngine.getTaskService();
		//拿到属于deptLeader角色的任务
		Task  taskOfLeader=taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
		System.out.println("taskName:"+taskOfLeader.getName());
		//由该角色成员组中一个接受任务
		taskService.claim(taskOfLeader.getId(), "leaderUser");
		variables=new HashMap<String,Object>();
		variables.put("approval", true);
		taskService.complete(taskOfLeader.getId(), variables);
		taskOfLeader=taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
		System.out.println(taskOfLeader);
		
		HistoryService historyService=processEngine.getHistoryService();
		//查询已完成的流程定义
		long count=historyService.createHistoricProcessInstanceQuery().finished().count();
		System.out.println(count);
		
		
	}
}

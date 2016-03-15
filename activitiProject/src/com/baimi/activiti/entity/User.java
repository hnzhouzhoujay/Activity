package com.baimi.activiti.entity;

import java.util.Date;

import org.activiti.engine.task.Task;
import org.springframework.format.annotation.DateTimeFormat;

public class User {
	private Integer id;
	private String name;
	private int age;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;
	
	private String applyUserId;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date applyTime;
	private String processInstanceId;
	private String processDefineId;
	private String taskId;
	private Task task;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public String toString(){
		return this.id+","+this.name+","+this.age+","+this.birthday;
	}
	public String getApplyUserId() {
		return applyUserId;
	}
	public void setApplyUserId(String applyUserId) {
		this.applyUserId = applyUserId;
	}
	public Date getApplyTime() {
		return applyTime;
	}
	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getProcessDefineId() {
		return processDefineId;
	}
	public void setProcessDefineId(String processDefineId) {
		this.processDefineId = processDefineId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}

}

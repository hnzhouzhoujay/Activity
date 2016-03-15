package com.baimi.activiti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.baimi.activiti.entity.User;

public class ProcessServiceImpl <T>{
	
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	IdentityService  identityService;
	@Autowired
	HistoryService  historyService;
	@Autowired
	TaskService  taskService;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	ProcessEngineConfiguration processEngineConfiguration;
	 
	protected String processDefKey="";
	
	/**
	 * 开启流程
	 * @param businessKey 业务对象ID
	 * @param objclass  业务对象class
	 * @param bussinessObj 业务对象
	 * @param variables  开始流程传入参数
	 * @param applyUserId 流程申请者ID
	 * @return 设置了流程实例ID的业务对象
	 */
	public T  startProcess(String businessKey,Class<T> objclass,T bussinessObj,Map<String,Object> variables,String applyUserId){
		identityService.setAuthenticatedUserId(applyUserId);
		ProcessInstance processInstance=runtimeService.startProcessInstanceByKey(processDefKey, businessKey, variables);
		String processInstanceId=processInstance.getId();
		try {
			Method method=objclass.getMethod("setProcessInstanceId", String.class);
			method.invoke(bussinessObj, processInstanceId);
		}catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return bussinessObj;
	}
	/**
	 * 找到用户的所有要处理的任务，包括未签收和已签收
	 * @param userId 当前用户ID
	 * @return map<bussinesskey,task>
	 */
	public Map<String,Task> findTodoTask(String userId){
		List<Task> tasklist=new ArrayList<Task>();
		Map<String,Task> map=new HashMap<String,Task>();
		//签收的任务
		List<Task> assignTask=taskService.createTaskQuery().processDefinitionKey(processDefKey).taskAssignee(userId).list();
		//可签收的任务
		List<Task> unassignTask=taskService.createTaskQuery().processDefinitionKey(processDefKey).taskCandidateUser(userId).list();
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
			map.put(businesskey, task);
			//任务ID
		}
		return map;
	}
	
	/**
	 * 签收任务
	 * @param taskId 任务ID
	 * @param userId 用户
	 */
	public void claimTask(String taskId,String userId){
		Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
		//如果任务已经被签收了
		if(StringUtils.isNotEmpty(task.getAssignee())){
			return;
		}
		taskService.claim(taskId, userId);
	}
	/**
	 * 反签收任务
	 * @param taskId
	 * @param userId
	 * @return
	 */
	public void unclaimTask(String taskId,String userId){
		List<IdentityLink> list=taskService.getIdentityLinksForTask(taskId);
		Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
		boolean hasCandidateUser=false;
		for (IdentityLink identityLink : list) {
			//如果任务有其它候选者 
			if(IdentityLinkType.CANDIDATE.equals(identityLink.getType())){
				hasCandidateUser=true;
				break;
			}
		}
		//如果有候选者且操作用户为任务签收者，取消任务签收
		if(hasCandidateUser&&userId.equals(task.getAssignee())){
			taskService.claim(taskId, null);//取消任务签收
		}
	}
	/**
	 * 完成任务
	 * @param taskId 任务ID
	 * @param user 
	 * @param variables
	 * @param saveEntity
	 * @param comment
	 */
	public void complete(String taskId,String userId,
			Map<String, Object> variables, boolean saveEntity,String comment) {
			Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
			if(!StringUtils.isEmpty(comment)){
				taskService.addComment(taskId, task.getProcessInstanceId(), comment);
			}
			//如果当前用户是任务签收用户
			if(userId.equals(task.getAssignee()))
				taskService.complete(taskId, variables);
	}
	/**
	 * 输出流程跟踪的图片
	 * @param executionId 执行ID，单实例中与processInstanceId一致
	 * @param response 
	 */
	public void traceTask(String executionId,HttpServletResponse response){
		ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
		ProcessDefinitionEntity processDefinition=(ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
		BpmnModel bpmnModel=repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
		ProcessDiagramGenerator processDiagramGenerator= processEngineConfiguration.getProcessDiagramGenerator();
		List<String>activeActivityIds= runtimeService.getActiveActivityIds(executionId);
		List<String> highLightedFlows=getHighLightedFlows(processDefinition, processInstance.getId());
		InputStream input=processDiagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows,"宋体","宋体",null,1.0f);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        try {
			while ((len = input.read(b, 0, 1024)) != -1) {
			    response.getOutputStream().write(b, 0, len);
			}
			response.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	/**
	 * 找寻子流程活动ID
	 * @param processDefinition
	 * @param processInstanceId
	 * @return
	 */
	  private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
	        List<String> highLightedFlows = new ArrayList<String>();
	        List<HistoricActivityInstance> historicActivityInstances = historyService
	                .createHistoricActivityInstanceQuery()
	                .processInstanceId(processInstanceId)
	                .orderByHistoricActivityInstanceStartTime().asc().list();

	        List<String> historicActivityInstanceList = new ArrayList<String>();
	        for (HistoricActivityInstance hai : historicActivityInstances) {
	            historicActivityInstanceList.add(hai.getActivityId());
	        }

	        // add current activities to list
	        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
	        historicActivityInstanceList.addAll(highLightedActivities);

	        // activities and their sequence-flows
	        for (ActivityImpl activity : processDefinition.getActivities()) {
	            int index = historicActivityInstanceList.indexOf(activity.getId());

	            if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
	                List<PvmTransition> pvmTransitionList = activity
	                        .getOutgoingTransitions();
	                for (PvmTransition pvmTransition : pvmTransitionList) {
	                    String destinationFlowId = pvmTransition.getDestination().getId();
	                    if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
	                        highLightedFlows.add(pvmTransition.getId());
	                    }
	                }
	            }
	        }
	        return highLightedFlows;
	    }
}


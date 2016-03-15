package test.java.org.activiti.designer.test;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessTestLeave {

	private String filename = "D:/workspacesMV/activitiProject/src/leave.bpmn";

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg.xml");
	
	 protected ProcessEngine processEngine=null;
	 protected RepositoryService repositoryService=null;
	 protected RuntimeService runtimeService=null;
	 protected TaskService taskService=null;
	 protected HistoryService historyService=null;
	 protected IdentityService identityService=null;
	 protected ManagementService managementService=null;
	 protected FormService formService=null;
	 
	 @Before
	 public void init(){
		 SpringProcessEngineConfiguration s=null;
		 processEngine=activitiRule.getProcessEngine();
		 repositoryService=activitiRule.getRepositoryService();
		 runtimeService=activitiRule.getRuntimeService();
		 taskService=activitiRule.getTaskService();
		 historyService=activitiRule.getHistoryService();
		 identityService=activitiRule.getIdentityService();
		 managementService=activitiRule.getManagementService();
		formService=activitiRule.getFormService();
		System.out.println("ok");
	 }
	@Test
	public void startProcess() throws Exception {
		RepositoryService repositoryService = activitiRule.getRepositoryService();
		repositoryService.createDeployment().addInputStream("leave.bpmn20.xml",
				new FileInputStream(filename)).deploy();
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("name", "Activiti");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave", variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());
	}
	
	@Test
	 public void allApproved() throws Exception {
		 RepositoryService repositoryService = activitiRule.getRepositoryService();
			repositoryService.createDeployment().addInputStream("leave.bpmn20.xml",
					new FileInputStream(filename)).deploy();
	        // 验证是否部署成功
	        long count = repositoryService.createProcessDefinitionQuery().count();
	        assertEquals(1, count);

	        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();
	        IdentityService identityService=processEngine.getIdentityService();
	        // 设置当前用户
	        String currentUserId = "henryyan";
	        identityService.setAuthenticatedUserId(currentUserId);

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Map<String, String> variables = new HashMap<String, String>();
	        Calendar ca = Calendar.getInstance();
	        String startDate = sdf.format(ca.getTime());
	        ca.add(Calendar.DAY_OF_MONTH, 2); // 当前日期加2天
	        String endDate = sdf.format(ca.getTime());
	        // 启动流程
	        variables.put("startDate", startDate);
	        variables.put("endDate", endDate);
	        variables.put("reason", "公休");
	        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
	        assertNotNull(processInstance);

	        // 部门领导审批通过
	        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
	        variables = new HashMap<String, String>();
	        variables.put("deptLeaderApproved", "true");
	        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

	        // 人事审批通过
	        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
	        variables = new HashMap<String, String>();
	        variables.put("hrApproved", "true");
	        formService.submitTaskFormData(hrTask.getId(), variables);

	        // 销假（根据申请人的用户ID读取）
	        Task reportBackTask = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
	        variables = new HashMap<String, String>();
	        variables.put("reportBackDate", sdf.format(ca.getTime()));
	        formService.submitTaskFormData(reportBackTask.getId(), variables);

	        // 验证流程是否已经结束
	        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
	        assertNotNull(historicProcessInstance);

	        // 读取历史变量
	        Map<String, Object> historyVariables = packageVariables(processInstance);

	        // 验证执行结果
	        assertEquals("ok", historyVariables.get("result"));
	    }

	   @Test
	    public void cancelApply() throws Exception {
		   
		   RepositoryService repositoryService = activitiRule.getRepositoryService();
			repositoryService.createDeployment().addInputStream("leave.bpmn20.xml",
					new FileInputStream(filename)).deploy();
	        // 设置当前用户
	        String currentUserId = "henryyan";
	        IdentityService identityService=processEngine.getIdentityService();
	        identityService.setAuthenticatedUserId(currentUserId);

	        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();

	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Map<String, String> variables = new HashMap<String, String>();
	        Calendar ca = Calendar.getInstance();
	        String startDate = sdf.format(ca.getTime());
	        ca.add(Calendar.DAY_OF_MONTH, 2);
	        String endDate = sdf.format(ca.getTime());

	        // 启动流程
	        variables.put("startDate", startDate);
	        variables.put("endDate", endDate);
	        variables.put("reason", "公休");
	        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
	        assertNotNull(processInstance);

	        // 部门领导审批通过
	        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
	        variables = new HashMap<String, String>();
	        variables.put("deptLeaderApproved", "false");
	        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

	        // 调整申请
	        Task modifyApply = taskService.createTaskQuery().taskAssignee(currentUserId).singleResult();
	        variables = new HashMap<String, String>();
	        variables.put("reApply", "false");
	        variables.put("startDate", startDate);
	        variables.put("endDate", endDate);
	        variables.put("reason", "公休");
	        formService.submitTaskFormData(modifyApply.getId(), variables);

	        // 读取历史变量
	        Map<String, Object> historyVariables = packageVariables(processInstance);

	        // 验证执行结果
	        assertEquals("canceled", historyVariables.get("result"));

	    }

	    /**
	     * 读取历史变量并封装到Map中
	     */
	    private Map<String, Object> packageVariables(ProcessInstance processInstance) {
	        Map<String, Object> historyVariables = new HashMap<String, Object>();
	        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
	        for (HistoricDetail historicDetail : list) {
	            if (historicDetail instanceof HistoricFormProperty) {
	                // 表单中的字段
	                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
	                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
	                System.out.println("form field: taskId=" + field.getTaskId() + ", " + field.getPropertyId() + " = " + field.getPropertyValue());
	            } else if (historicDetail instanceof HistoricVariableUpdate) {
	                HistoricVariableUpdate variable = (HistoricVariableUpdate) historicDetail;
	                historyVariables.put(variable.getVariableName(), variable.getValue());
	                System.out.println("variable: " + variable.getVariableName() + " = " + variable.getValue());
	            }
	        }
	        return historyVariables;
	    }

}
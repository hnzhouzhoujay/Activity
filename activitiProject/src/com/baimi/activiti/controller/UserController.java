package com.baimi.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.baimi.activiti.entity.User;
import com.baimi.activiti.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	IdentityService  identityService;
	@Autowired
	UserService userService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService  historyService;
	 @Autowired
	ProcessEngineConfiguration processEngineConfiguration;
	@RequestMapping(value="/upload",method=RequestMethod.POST)
	public ModelAndView uploadProcess(@RequestParam("bpmnFile") MultipartFile file){
		String name=file.getOriginalFilename();
		String extname=name.substring(name.indexOf("\\."));
		if("bpmn".equals(extname) || "bpmn20.xml".equals(extname) ){
			ProcessEngineFactoryBean bean=null;
		}
		return new ModelAndView("upload");
	}
	
	@RequestMapping(value="/apply/page")
	public ModelAndView toApplyPage(HttpSession session){
		session.setAttribute("applyUserId","zj"+new Random().nextInt(50));
		return new ModelAndView("apply");
	}
	
	@RequestMapping(value="/startProcess")
	public String startProcess(User user,RedirectAttributes redirectAttributes,HttpSession session){
		Map<String,Object> variables=new HashMap<String,Object>();
		userService.startProcess(user, variables, (String) session.getAttribute("applyUserId"));
		redirectAttributes.addFlashAttribute("msg", "流程已经启动");
		return "redirect:apply/page.do";
	}
	
	@RequestMapping(value="/construct")
	public ModelAndView taskList(@RequestParam(required=true) String userId,@RequestParam(required=true) String groupId){
		org.activiti.engine.identity.User user=identityService.newUser(userId);
		identityService.saveUser(user);
		Group group=identityService.newGroup(groupId);
		identityService.saveGroup(group);
		identityService.createMembership(userId, groupId);
		return new ModelAndView("apply");
	}
	@RequestMapping(value="/taskList")
	public ModelAndView taskList(@RequestParam(required=true) String userId,HttpServletRequest request,HttpSession session){
		List<User> entitys=userService.findTodoTask(userId);
		request.setAttribute("entitys", entitys);
		session.setAttribute("userId", userId);
		return new ModelAndView("tasklist");
	}
	@RequestMapping(value="/claim")
	public String claimTask(@RequestParam String taskId,@RequestParam(required=false) String nextdo,@RequestParam String userId){
		String model=null;
		if(StringUtils.isNotBlank(nextdo)&&nextdo.equals("handle")){
			model= "redirect:/user/task/"+taskId+"/view.do";
		}else{
			model="redirect:/user/taskList.do?userId="+userId;
		}
		userService.claimTask(taskId, userId);
		return  model;
	}
	@RequestMapping(value="/unclaim")
	public String unclaimTask(@RequestParam String taskId,@RequestParam String userId){
		userService.unclaimTask(taskId, userId);
		return  "redirect:/user/taskList.do?userId="+userId;
	}
	@RequestMapping(value="/task/{taskId}/view.do")
	public ModelAndView viewTask(@PathVariable String taskId,HttpServletRequest request){
		Task task=taskService.createTaskQuery().taskId(taskId).singleResult();
		ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
		int userId=Integer.parseInt(processInstance.getBusinessKey());
		User u=userService.findUserById(userId);
		request.setAttribute("u", u);
		request.setAttribute("task", task);
		return new ModelAndView("task-"+task.getTaskDefinitionKey());
	}
	
	@RequestMapping(value="/task/{executionId}/trace")
	public void traceTask(@PathVariable String executionId,HttpServletResponse response){
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
	
	@RequestMapping(value="/task/{taskId}/complete.do")
	public ModelAndView completeTask(@PathVariable String taskId,@RequestParam(value="id") String id
			,@RequestParam(value="comment",required=false) String comment
			,@RequestParam(required=false) String saveEntity,HttpServletRequest request){
		User user=null;
		if(id!=null){
			user= userService.findUserById(Integer.parseInt(id));
		}
		user= new User();
		Map<String,Object> variables=new HashMap<String,Object>();
		Boolean boolSaveEntity=false;
		if(saveEntity!=null){
			boolSaveEntity=Boolean.parseBoolean(saveEntity);
		}
		Enumeration<String> pramNames=request.getParameterNames();
		while(pramNames.hasMoreElements()){
			String paramName=pramNames.nextElement();
			if(paramName.startsWith("p_")){
				String[] splits=paramName.split("_");
				if(splits.length==3){
					Object obj=null;
					if(splits[1].equals("B")){
						obj=Boolean.parseBoolean(request.getParameter(paramName));
					}
					//其它参数...
					variables.put(splits[2], obj);
				}
			}
		}
		userService.complete(taskId, user, variables, boolSaveEntity,comment);
		return new ModelAndView("apply");
	}
	@RequestMapping(value="/comment/{processInstanceId}")
	public ModelAndView comment(@PathVariable String processInstanceId,HttpServletRequest request){
		List<Comment> comments=taskService.getProcessInstanceComments(processInstanceId);
		List<HistoricTaskInstance> histasks=historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();
		Map<String,String> taskNames=new HashMap<String,String>();
		for (HistoricTaskInstance historicTaskInstance : histasks) {
			taskNames.put(historicTaskInstance.getId() , historicTaskInstance.getName());
		}
		request.setAttribute("taskNames", taskNames);
		request.setAttribute("comments", comments);
		return new ModelAndView("commentlist");
	}
	/*@ModelAttribute("user")
	public User findUser(@RequestParam(value="id") String id,HttpSession session){
		if(id!=null){
			return userService.findUserById(Integer.parseInt(id));
		}
		return new User();
			
	}*/

}

package com.baimi.activiti.controller;

import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/process")
public class ProcessController {
	@RequestMapping(value="/upload",method=RequestMethod.POST)
	public ModelAndView uploadProcess(@RequestParam("bpmnFile") MultipartFile file){
		String name=file.getOriginalFilename();
		String extname=name.substring(name.indexOf("\\."));
		if("bpmn".equals(extname) || "bpmn20.xml".equals(extname) ){
			ProcessEngineFactoryBean bean=null;
		}
		return new ModelAndView("upload");
	}

}

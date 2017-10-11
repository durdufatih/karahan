package com.alle;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fatih.durdu@milliyetemlak.com on 25-Sep-2017
 *
 * @author Mehmet Fatih Durdu
 */
@Controller
public class HelloController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @RequestMapping("/")
    public ModelAndView hello() {
        ModelAndView modelAndView = new ModelAndView("my-task");
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned("1").active().list();
        modelAndView.addObject("taskList", taskList);
        return modelAndView;
    }

    @RequestMapping("/is-listesi")
    public ModelAndView processList() {
        ModelAndView modelAndView = new ModelAndView("is-listesi");
        DeploymentModel deployment = new DeploymentModel();
        List<ProcessDefinition> deployments = repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
        deployment.setProcessDefinitionList(deployments);
        modelAndView.addObject("deploys", deployment);
        return modelAndView;
    }

    @RequestMapping(value = "/start/{id}", method = RequestMethod.GET)
    public ModelAndView getStartProcess(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("veri-girisi");
        Content content = new Content();
        content.setProcessId(id);
        modelAndView.addObject("model", content);
        return modelAndView;
    }

    @RequestMapping(value = "/start/{id}", method = RequestMethod.POST)
    public ModelAndView startProcess(@PathVariable String id, @ModelAttribute Content content) {
        ModelAndView modelAndView = new ModelAndView("redirect:/mytask");
        Map<String, Object> variables = new HashMap<>();
        variables.put("whom", content.getWhom());
        variables.put("price", content.getPrice());
        runtimeService.startProcessInstanceById(id, variables);
        return modelAndView;
    }


    @RequestMapping(value = "/mytask", method = RequestMethod.GET)
    public ModelAndView getMyTask() {
        ModelAndView modelAndView = new ModelAndView("my-task");
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned("1").active().list();
        modelAndView.addObject("taskList", taskList);
        return modelAndView;
    }

    @RequestMapping(value = "/template", method = RequestMethod.GET)
    public ModelAndView getTemplate() {
        ModelAndView modelAndView = new ModelAndView("form");
        return modelAndView;
    }
}

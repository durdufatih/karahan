package com.alle;

import com.alle.auth.UserService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Autowired
    private UserService userService;

    @RequestMapping("/home")
    public ModelAndView hello() {
        ModelAndView modelAndView = new ModelAndView("first");
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned("1").active().orderByTaskId().asc().list();
        modelAndView.addObject("taskList", taskList);
        return modelAndView;
    }

    @RequestMapping("/detail")
    public ModelAndView detail() {

        ModelAndView modelAndView = new ModelAndView("process-detail");
        modelAndView.addObject("model", new ProcessDefinitionModel());
        return modelAndView;
    }

    @RequestMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage() throws IOException {
        ProcessDiagramGenerator diagramGenerator=new DefaultProcessDiagramGenerator();
        List<ProcessDefinition> deployments = repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
        BpmnModel bpmnModel=repositoryService.getBpmnModel(deployments.get(0).getId());
        diagramGenerator.generatePngImage(bpmnModel,1);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( diagramGenerator.generatePngImage(bpmnModel,1), "jpg", baos );
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<byte[]>(imageInByte, headers, HttpStatus.OK);
    }
    @RequestMapping("/")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("first");
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(userService.findCurrentUserId()).active().list();
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
        ModelAndView modelAndView = new ModelAndView("form");
        Content content = new Content();
        content.setProcessId(id);
        modelAndView.addObject("model", content);
        return modelAndView;
    }

    @RequestMapping(value = "/start/{id}", method = RequestMethod.POST)
    public ModelAndView startProcess(@PathVariable String id, @ModelAttribute Content content) {
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        Map<String, Object> variables = new HashMap<>();
        variables.put("whom", content.getWhom());
        variables.put("price", content.getPrice());
        runtimeService.startProcessInstanceByKey(id, variables);
        return modelAndView;
    }


    @RequestMapping(value = "/mytask", method = RequestMethod.GET)
    public ModelAndView getMyTask() {
        ModelAndView modelAndView = new ModelAndView("my-task");
        List<Task> taskList = taskService.createTaskQuery().taskCandidateOrAssigned(userService.findCurrentUserId()).active().list();
        modelAndView.addObject("taskList", taskList);
        return modelAndView;
    }

    @RequestMapping(value = "/task/detail/{id}",method = RequestMethod.GET)
    public ModelAndView getTaskDetail(@PathVariable String id){
        ModelAndView modelAndView=new ModelAndView("taskdetail");
        Task task=taskService.createTaskQuery().taskId(id).active().singleResult();
        modelAndView.addObject("taskModel",task);
        modelAndView.addObject("comments",taskService.getProcessInstanceComments(task.getProcessInstanceId()));
        return modelAndView;

    }

    @RequestMapping(value = "/task/comment/{id}",method = RequestMethod.POST)
    public void addTaskComment(@RequestBody CommentDto commentDto,@PathVariable String id){
        Task task=taskService.createTaskQuery().taskId(id).active().singleResult();
        taskService.addComment(task.getId(), task.getProcessInstanceId(), commentDto.getComment());

    }
    @RequestMapping(value = "/task/complete/{id}",method = RequestMethod.GET)
    public ModelAndView completeTask(@PathVariable String id){
        ModelAndView modelAndView=new ModelAndView("redirect:/");
        taskService.complete(id);
        return modelAndView;

    }
}

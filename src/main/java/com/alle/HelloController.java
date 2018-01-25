package com.alle;

import com.alle.auth.User;
import com.alle.auth.UserService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;



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

    @RequestMapping(value = {"/", "/home"})
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("first");
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(userService.findCurrentUserId()).active().list();
        List<HistoricTaskInstance> historyList = historyService.createHistoricTaskInstanceQuery().finished().taskAssignee(userService.findCurrentUserId()).list();
        List<TaskModel> taskModelList = new ArrayList<>();
        for (Task task : taskList) {
            TaskModel taskModel = new TaskModel();
            taskModel.setTask(task);
            taskModel.setWhom(runtimeService.getVariable(task.getProcessInstanceId(), "whom").toString());
            taskModelList.add(taskModel);
        }
        modelAndView.addObject("taskList", taskModelList);
        modelAndView.addObject("historyList", historyList);

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
        ProcessInstance processInstance=runtimeService.startProcessInstanceByKey(id, variables);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().list().get(0);
        task.setName(content.getTitle());
        task.setDescription(content.getDescription());
        task.setPriority(Integer.parseInt(content.getPriority()));
        taskService.saveTask(task);
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        ModelAndView modelAndView=new ModelAndView("taskdetail");
        Task task=taskService.createTaskQuery().taskId(id).active().singleResult();
        Content content =new Content();
        content.setProcessId(task.getId());
        content.setTaskId(task.getId());
        User userAssignee=userService.findUser(Integer.parseInt(task.getAssignee()));
        content.setAssignee(userAssignee.getName()+" "+userAssignee.getLastName());
        content.setDescription(task.getDescription());
        content.setPriority(Integer.toString(task.getPriority()));
        content.setTitle(task.getName());
        content.setCreateDate(simpleDateFormat.format(task.getCreateTime()));
        modelAndView.addObject("taskModel",content);
        List<Comment> commentList = taskService.getProcessInstanceComments(task.getProcessInstanceId());
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            CommentDto commentDto = new CommentDto();
            commentDto.setComment(comment.getFullMessage());
            User user = userService.findUserByEmail(comment.getUserId());
            if (user != null)
                commentDto.setUserName(user.getName() + " " + user.getLastName());

            commentDto.setTime(simpleDateFormat.format(comment.getTime()));
            commentDtoList.add(commentDto);
        }
        modelAndView.addObject("commentModel", new CommentDto());
        modelAndView.addObject("comments", commentDtoList);

        //Burası geçmişi göstermeyi amaçlıyor
        List<HistoricActivityInstance> historicProcessInstanceList=historyService.createHistoricActivityInstanceQuery().processInstanceId(task.getProcessInstanceId()).list();
        return modelAndView;

    }

    @RequestMapping(value = "/task/comment/{id}",method = RequestMethod.POST)
    public ModelAndView addTaskComment(@ModelAttribute CommentDto commentDto, @PathVariable String id,Principal principal) {
        ModelAndView modelAndView = new ModelAndView("redirect:/task/detail/" + id);
        Task task=taskService.createTaskQuery().taskId(id).active().singleResult();
        taskService.addComment(task.getId(), task.getProcessInstanceId(), commentDto.getComment());
        User user=userService.findUserByEmail(principal.getName());
        identityService.setAuthenticatedUserId(principal.getName());
        return modelAndView;

    }
    @RequestMapping(value = "/task/complete/{id}",method = RequestMethod.GET)
    public ModelAndView completeTask(@PathVariable String id){
        ModelAndView modelAndView=new ModelAndView("redirect:/");
        taskService.complete(id);
        return modelAndView;

    }
}

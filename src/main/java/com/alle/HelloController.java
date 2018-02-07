package com.alle;

import com.alle.auth.User;
import com.alle.auth.UserService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
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
import java.util.*;

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

    @Autowired
    private FormService formService;


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

    @RequestMapping(value = {"/process"})
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("first");
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(userService.findCurrentUserId()).active().list();
        List<HistoricTaskInstance> historyList = historyService.createHistoricTaskInstanceQuery().finished().taskAssignee(userService.findCurrentUserId()).list();
        List<TaskModel> taskModelList = new ArrayList<>();
        List<ProcessDefinition> deployments = repositoryService.createProcessDefinitionQuery().latestVersion().active().list();
        for (Task task : taskList) {
            TaskModel taskModel = new TaskModel();
            taskModel.setTask(task);
            Object title = runtimeService.getVariable(task.getProcessInstanceId(), "Title");
            if (title != null)
                taskModel.setTitle(title.toString());
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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/process");
        return modelAndView;
    }

    @RequestMapping(value = "/start/{id}", method = RequestMethod.POST)
    public ModelAndView startProcess(@PathVariable String id, @ModelAttribute Content content) {
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        ProcessInstance processInstance=runtimeService.startProcessInstanceByKey(id);
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

        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formPropertyList = taskFormData.getFormProperties();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"form-group\">\n");
        stringBuilder.append("<label for=\"Title\">Konu</label>\n");
        stringBuilder.append("<input type=\"text\"");
        stringBuilder.append(" required ");
        Optional<FormProperty> itemObject = formPropertyList.stream().filter(item -> item.getName().equals("Title")).findAny();
        if (itemObject.isPresent())
            stringBuilder.append(" value=" + itemObject.get().getValue());
        stringBuilder.append(" class=\"form-control\" name=\"Title\" id=\"Title\" placeholder=\"Konu Giriniz\"/>");
        stringBuilder.append("</div>");

        for (FormProperty formProperty : formPropertyList) {
            stringBuilder.append("<div class=\"form-group\">\n");
            stringBuilder.append("<label for=\"" + formProperty.getId() + "\">" + formProperty.getName() + "</label>\n");
            //if (formProperty.isWritable()) {
            stringBuilder.append("<input type=\"text\"");
            if (!formProperty.isWritable())
                stringBuilder.append(" disabled ");
            if (formProperty.isRequired())
                stringBuilder.append(" required ");

            stringBuilder.append("class=\"form-control\" name=\"" + formProperty.getId() + "\" id=\"" + formProperty.getId() + "\" placeholder=\"" + formProperty.getName() + " Giriniz\"");
            if (formProperty.getValue() != null)
                stringBuilder.append(" value=\"" + formProperty.getValue() + "\"");
            stringBuilder.append("/>\n");
            if (!formProperty.isWritable())
                stringBuilder.append(" <input type=\"hidden\" name=" + formProperty.getId() + " value=" + formProperty.getValue() + "> ");

            stringBuilder.append("</div>");

        }

        modelAndView.addObject("form", stringBuilder.toString());
        modelAndView.addObject("commentModel", new CommentDto());
        modelAndView.addObject("comments", commentDtoList);

        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<HistoricDetail> historicDetailList = historyService.createHistoricDetailQuery().processInstanceId(task.getProcessInstanceId()).list();
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

    @RequestMapping(value = "/task/complete/{id}", method = RequestMethod.POST)
    public ModelAndView postTask(@PathVariable String id, org.apache.catalina.servlet4preview.http.HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/process");
        Map<String, Object> map = new HashMap<>();
        Map<String, String[]> parameters = request.getParameterMap();
        for (String key : parameters.keySet()) {
            String[] value = parameters.get(key);
            map.put(key, value[0]);

        }
        taskService.setVariable(id, "approve", Boolean.TRUE);
        taskService.complete(id, map);
        return modelAndView;

    }

    @RequestMapping(value = "/task/reject/{id}", method = RequestMethod.POST)
    public ModelAndView postTask(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("redirect:/process");
        Map<String, Object> map = new HashMap<>();
        map.put("approve", false);
        taskService.complete(id, map);
        return modelAndView;

    }

    @RequestMapping(value = "/task/complete/{id}", method = RequestMethod.GET)
    public ModelAndView completeTask(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView("redirect:/process");
        taskService.complete(id);
        return modelAndView;

    }
}

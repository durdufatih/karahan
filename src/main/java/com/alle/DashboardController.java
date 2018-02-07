package com.alle;

import com.alle.auth.User;
import com.alle.auth.UserService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserService userService;


    @RequestMapping("/")
    public ModelAndView getDatas() {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        DashboardModel dashboardModel = new DashboardModel();
        dashboardModel.setCompletedJobCount(historyService.createHistoricTaskInstanceQuery().finished().count());
        dashboardModel.setUnCompletedJobCount(taskService.createTaskQuery().active().count());
        dashboardModel.setCompletedProcessCount(historyService.createHistoricTaskInstanceQuery().finished().count());
        List<User> userList = userService.allUser();
        dashboardModel.setPersonCount(new Long(userList.size()));
        List<UserModel> userModelList = new ArrayList<>();
        for (User user : userList) {
            UserModel userModel = new UserModel();
            userModel.setName(user.getName() + " " + user.getLastName());
            userModel.setCompletedTaskCount(historyService.createHistoricTaskInstanceQuery().taskAssignee(Integer.toString(user.getId())).finished().count());
            userModel.setUnCompletedTask(taskService.createTaskQuery().active().taskAssignee(Integer.toString(user.getId())).count());
            List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().taskAssignee(Integer.toString(user.getId())).finished().list();
            if (historicTaskInstances.size() != 0) {
                Double time = new Double(0);
                for (HistoricTaskInstance taskInstance : historicTaskInstances) {
                    time = time + taskInstance.getDurationInMillis();
                }
                userModel.setTaskCompleteTime(Math.round(time) / historicTaskInstances.size());
            }

            userModelList.add(userModel);
        }
        dashboardModel.setUserModel(userModelList);
        modelAndView.addObject("model", dashboardModel);
        return modelAndView;
    }
}

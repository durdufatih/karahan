package com.alle;

import org.activiti.engine.task.Task;

import java.util.List;

/**
 * Created by fatih.durdu@milliyetemlak.com on 29-Sep-2017
 *
 * @author Mehmet Fatih Durdu
 */
public class TaskModel {

    private List<Task> taskList;

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
}

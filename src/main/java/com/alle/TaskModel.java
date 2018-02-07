package com.alle;

import org.activiti.engine.task.Task;

import java.util.List;

/**
 * Created by fatih.durdu@milliyetemlak.com on 29-Sep-2017
 *
 * @author Mehmet Fatih Durdu
 */
public class TaskModel  {

    private Task task;
    private String title;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

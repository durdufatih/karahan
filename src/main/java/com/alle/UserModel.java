package com.alle;

public class UserModel {
    private String name;
    private Long startProcessCount;
    private Long completedTaskCount;
    private Long unCompletedTask;
    private String taskCompleteTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStartProcessCount() {
        return startProcessCount;
    }

    public void setStartProcessCount(Long startProcessCount) {
        this.startProcessCount = startProcessCount;
    }

    public Long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public Long getUnCompletedTask() {
        return unCompletedTask;
    }

    public void setUnCompletedTask(Long unCompletedTask) {
        this.unCompletedTask = unCompletedTask;
    }

    public String getTaskCompleteTime() {
        return taskCompleteTime;
    }

    public void setTaskCompleteTime(String taskCompleteTime) {
        this.taskCompleteTime = taskCompleteTime;
    }
}

package com.alle;

import java.util.List;

public class DashboardModel {

    private Long completedJobCount;
    private Long unCompletedJobCount;
    private Long personCount;
    private Long completedProcessCount;
    private List<UserModel> userModel;


    public List<UserModel> getUserModel() {
        return userModel;
    }

    public void setUserModel(List<UserModel> userModel) {
        this.userModel = userModel;
    }

    public Long getCompletedJobCount() {
        return completedJobCount;
    }

    public void setCompletedJobCount(Long completedJobCount) {
        this.completedJobCount = completedJobCount;
    }

    public Long getUnCompletedJobCount() {
        return unCompletedJobCount;
    }

    public void setUnCompletedJobCount(Long unCompletedJobCount) {
        this.unCompletedJobCount = unCompletedJobCount;
    }

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }

    public Long getCompletedProcessCount() {
        return completedProcessCount;
    }

    public void setCompletedProcessCount(Long completedProcessCount) {
        this.completedProcessCount = completedProcessCount;
    }
}

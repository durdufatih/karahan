package com.alle;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import java.util.List;

/**
 * Created by fatih.durdu@milliyetemlak.com on 25-Sep-2017
 *
 * @author Mehmet Fatih Durdu
 */
public class DeploymentModel {

    private List<ProcessDefinition> processDefinitionList;

    public List<ProcessDefinition> getProcessDefinitionList() {
        return processDefinitionList;
    }

    public void setProcessDefinitionList(List<ProcessDefinition> processDefinitionList) {
        this.processDefinitionList = processDefinitionList;
    }
}

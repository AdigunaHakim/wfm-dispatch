package id.co.asyst.wfm.dispatch.payload;

import id.co.asyst.wfm.dispatch.model.TaskList;

import java.util.List;

public class TaskListMonitoring {

    private String employeeId;

    private String employeeName;

    private String employeeGroup;

    private List<TaskList> task;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(String employeeGroup) {
        this.employeeGroup = employeeGroup;
    }

    public List<TaskList> getTask() {
        return task;
    }

    public void setTask(List<TaskList> task) {
        this.task = task;
    }

    public void addTask(TaskList task) {
        this.task.add(task);
    }
}

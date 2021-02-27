package id.co.asyst.wfm.dispatch.scheduler;

import id.co.asyst.wfm.dispatch.model.TaskList;
import id.co.asyst.wfm.dispatch.service.TaskListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class TaskScheduler {

    @Autowired
    TaskListService taskListService;

    @Scheduled(fixedRate = 1800000)
    public void checkDelayedTask(){

        Date date = new Date();
        LocalDate endDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startDate = endDate.minusDays(1);
        LocalTime timeTreshold = LocalTime.parse("08:00:00");

        List<TaskList> taskListCheck = taskListService.getgetTaskListCheckDelayed(startDate, endDate);

        if (!taskListCheck.isEmpty()){
            for (int i = 0; i < taskListCheck.size(); i++){
                if (taskListCheck.get(i).getStartTime().isAfter(taskListCheck.get(i).getEndTime())){

                    LocalDateTime startTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate(), taskListCheck.get(i).getStartTime());
                    LocalDateTime endTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate().plusDays(1), taskListCheck.get(i).getStartTime());

                    setDelayTask(taskListCheck.get(i), startTime, endTime);
                }
                else{
                    if (taskListCheck.get(i).getShiftIndicator() != null && taskListCheck.get(i).getStartTime() != null){
                        if (taskListCheck.get(i).getShiftIndicator() == 1 && taskListCheck.get(i).getStartTime().isBefore(timeTreshold)){
                            LocalDateTime startTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate().plusDays(1), taskListCheck.get(i).getStartTime());
                            LocalDateTime endTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate().plusDays(1), taskListCheck.get(i).getStartTime());

                            setDelayTask(taskListCheck.get(i), startTime, endTime);
                        }
                        else{
                            LocalDateTime startTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate(), taskListCheck.get(i).getStartTime());
                            LocalDateTime endTime = LocalDateTime.of(taskListCheck.get(i).getShiftDate(), taskListCheck.get(i).getStartTime());

                            setDelayTask(taskListCheck.get(i), startTime, endTime);
                        }
                    }
                }
            }
        }
    }

    public void setDelayTask(TaskList taskList, LocalDateTime startTime, LocalDateTime endTime){

        Date date = new Date();
        LocalDateTime nowDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (taskList.getStatus().equals("Dispatch") || taskList.getStatus().equals("Confirm")){
            if (nowDateTime.isAfter(startTime) || nowDateTime.isAfter(endTime)){
                taskList.setStatus("Delayed");
                taskListService.saveOrUpdate(taskList);
            }
        }
        else if (taskList.getStatus().equals("Start")){
            if (nowDateTime.isAfter(endTime)){
                taskList.setStatus("Delayed");
                taskListService.saveOrUpdate(taskList);
            }
        }
    }
}

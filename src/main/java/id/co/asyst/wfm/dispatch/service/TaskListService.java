package id.co.asyst.wfm.dispatch.service;

import id.co.asyst.wfm.core.service.ServiceManager;
import id.co.asyst.wfm.dispatch.model.ActiveEnum;
import id.co.asyst.wfm.dispatch.model.TaskList;
import id.co.asyst.wfm.dispatch.util.ParsingResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TaskListService<T,ID> extends ServiceManager<T,ID> {

    //Upload Task List
    ParsingResponse storeFile2003(MultipartFile file, String employeeGroup);
    ParsingResponse storeFile2007(MultipartFile file, String employeeGroup);
    Resource loadFileAsResource(String fileName);

    //Basic Task List
    Page<TaskList> searchNull(String id, String qual, String status, String group, String attendanceStatus, ActiveEnum active, Pageable pageable);
    Page<TaskList> searchNullAndDate(String id, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable);
    Page<TaskList> search(String id, String name, String qual, String status, String group, String attendanceStatus, ActiveEnum active, Pageable pageable);
    Page<TaskList> searchAndDate(String id, String name, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable);
    List<TaskList> findByEmployeeId(String employeeId, List<String> status, ActiveEnum active);
    List<TaskList> findCurrentTask(String employeeId, LocalDate startDate, LocalDate endDate, List<String> status, ActiveEnum active);
    List<TaskList> saveAll(List<TaskList> taskLists);
    Page<TaskList> findByStatus(String status, Pageable pageable);
    Integer countTaskNotif(List<String> status, LocalDate date, String employeeId, ActiveEnum active);
    Integer countExist(LocalDate date, String id, String qualification, LocalTime time, String group);

    //Attendance Task List
    Integer countAttendanceStatus(String employeeId, LocalDate shiftDate);
    String assignAttendanceStatus(String employeeId, LocalDate shiftDate);
    String assignAttendanceReminder(String employeeId, LocalDate shiftDate);

    //Monitoring Task List
    List<TaskList> getTaskMonitoring(LocalDate startDate, LocalDate endDate, String employeeGroup);

    //Validasi Task List
    Integer countEmployeeSchedule(String employeeId, LocalDate shiftDate);
    List<String> getListFuncQual(String qualificationCode);
    Integer countEmployeeFunction(String employeeId, String functionCode);
    String getShiftSchedule(String employeeId, LocalDate shiftDate);
    LocalTime getShiftStartTime(String shiftTypeCode);
    LocalTime getShiftEndTime(String shiftTypeCode);
    Integer getShiftIndicator(String shiftTypeCode);
    Integer countShift(String shiftTypeCode);
    List<TaskList>  getTaskListCheck(String employeeId, LocalDate shiftDate, String shiftTypeCode);

    //Suggest Task List
    List<String> getEmployeeShiftDate(LocalDate shiftDate);

    //Scheduler Check Task
    List<TaskList> getgetTaskListCheckDelayed(LocalDate startDate, LocalDate endDate);
}

package id.co.asyst.wfm.dispatch.repository;

import id.co.asyst.wfm.core.repository.BaseJpaRepository;
import id.co.asyst.wfm.dispatch.model.ActiveEnum;
import id.co.asyst.wfm.dispatch.model.TaskList;
import org.apache.tomcat.jni.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TaskListRepository extends BaseJpaRepository<TaskList, Long> {

    //Basic Task List Query
    Page<TaskList> findByEmployeeIdAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndActive(String id, String qual, String status, String group, String attendanceStatus, ActiveEnum active, Pageable pageable);
    Page<TaskList> findByEmployeeIdAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndShiftDateAndActive(String id, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable);
    Page<TaskList> findByEmployeeIdContainsAndEmployeeNameContainsAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndActive(String id, String name, String qual, String status, String group, String attendanceStatus, ActiveEnum active, Pageable pageable);
    Page<TaskList> findByEmployeeIdContainsAndEmployeeNameContainsAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndShiftDateAndActive(String id, String name, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable);
    Page<TaskList> findByStatus(String status, Pageable pageable);
    List<TaskList> findByEmployeeIdAndStatusInAndActive(String employeeId, List<String> status, ActiveEnum active);
    List<TaskList> findByEmployeeIdAndShiftDateGreaterThanEqualAndShiftDateLessThanEqualAndStatusInAndActive(String employeeId, LocalDate startDate, LocalDate endDate, List<String> status, ActiveEnum active);
    Integer countByShiftDateAndEmployeeIdAndQualificationCodeAndStartTimeAndEmployeeGroup(LocalDate date, String id, String qualification, LocalTime time, String group );
    Integer countByStatusInAndShiftDateLessThanEqualAndEmployeeIdAndActive(List<String> status, LocalDate date, String employeeId, ActiveEnum active);

    @Query(value = "select * from task_list where employee_id like %?1% and employee_name like %?2% and qualification_code like %?3% " +
            "and status like %?4% and employee_group like %?5% and attendance_status like %?6% ORDER BY ?#{#pageable}",
            countQuery = "select count(*) from task_list where employee_id like %?1% and employee_name " +
                    "like %?2% and qualification_code like %?3% and status like %?4% and employee_group like %?5% " +
                    "and attendance_status like %?6% ORDER BY ?#{#pageable}",
            nativeQuery = true)
    Page<TaskList> searchAll(String id, String name, String qual, String status, String group, String attendanceStatus, Pageable pageable);

    //Parse Task Into Employee Schedule
    @Modifying
    @Query(value = "insert into employee_schedule (employee_schedule_id, employee_id, employee_name, employee_group, date, shift_type_code, shift_publish_name, shift_indicator, shift_starttime, shift_endtime, " +
            " qualification_code, attendance_reminder, attendance_status) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13)", nativeQuery = true)
    @Transactional
    void toEmployeeSchedule(String scheduleId, String employeeId, String employeeName, String employeeGroup, LocalDate shiftDate, String shiftTypeCode, String shiftPublishName, Integer shiftIndicator, LocalTime shiftStartTime, LocalTime shiftEndTime, String qualificationCode, String attendanceReminder, String attendanceStatus);

    //Attendance Task List Query
    @Query(value = "select count(*) from task_list where employee_id= ?1 and shift_date = ?2",
            nativeQuery = true)
    Integer countAttendanceStatus(String employee_id, LocalDate shiftDate);

    @Query(value = "select DISTINCT(attendance_status) from task_list where employee_id= ?1 and " +
            "shift_date = ?2",
            nativeQuery = true)
    String assignAttendanceStatus(String employee_id, LocalDate shiftDate);

    @Query(value = "select DISTINCT(attendance_reminder) from task_list where employee_id= ?1 and " +
            "shift_date = ?2",
            nativeQuery = true)
    String assignAttendanceReminder(String employee_id, LocalDate shiftDate);

    //Monitoring Task List Query
    @Query(value = "select * from task_list where shift_date >= ?1 and shift_date <= ?2 and " +
            "employee_group = ?3 order by employee_id, shift_date asc ",
            nativeQuery = true)
    List<TaskList> getTaskMonitoring(LocalDate startDate, LocalDate endDate, String employeeGroup);

    //Validasi Task List Query
    @Query(value = "select count(*) from employee_schedule a where a.employee_id = ?1 and a.date = ?2 ",
            nativeQuery = true)
    Integer countEmployeeSchedule(String employee_id, LocalDate shiftDate);

    @Query(value = "select distinct(function_code) from function_qualification  where qualification_code = ?1 ",
            nativeQuery = true)
    List<String> getListFuncQual(String qualificationCode);

    @Query(value = "select count(*) from employee_function a where a.employee_id = ?1 and a.function_code = ?2 ",
            nativeQuery = true)
    Integer countEmployeeFunction(String employee_id, String functionCode);

    @Query(value = "select a.shift_type_code from employee_schedule a where a.employee_id = ?1 and " +
            "a.date =?2 ",
            nativeQuery = true)
    String getShiftSchedule(String employeeId, LocalDate shiftDate);

    @Query(value = "select publish_name from shift_type where shift_type_code = ?1 ",
            nativeQuery = true)
    String getShiftPublishName(String shiftTypeCode);

    @Query(value = "select normal_starttime from shift_type where shift_type_code = ?1 ",
            nativeQuery = true)
    LocalTime getShiftStartTime(String shiftTypeCode);

    @Query(value = "select normal_endtime from shift_type where shift_type_code = ?1 ",
            nativeQuery = true)
    LocalTime getShiftEndTime(String shiftTypeCode);

    @Query(value = "select shift_indicator from shift_type where shift_type_code = ?1 ",
            nativeQuery = true)
    Integer getShiftIndicator(String shiftTypeCode);

    @Query(value = "select count(*) from shift_type where shift_type_code = ?1 ",
            nativeQuery = true)
    Integer countShift(String shiftTypeCode);

    @Query(value = "select * from task_list where employee_id = ?1 and shift_date = ?2 and active = 1 ",
            nativeQuery = true)
    List<TaskList> getTaskListCheck(String employeeId, LocalDate shiftDate, String shiftTypeCode);

    //Suggest Task Query
    @Query(value = "select employee_id from employee_schedule where date = ?1 ",
            nativeQuery = true)
    List<String> getEmployeeShiftDate(LocalDate shiftDate);

    //Scheduler Check Task
    @Query(value = "select * from task_list where shift_date >= ?1 and shift_date <= ?2 and active = 1 and " +
            "status not in ('Finish','Delayed')",
            nativeQuery = true)
    List<TaskList> getTaskListCheckDelayed(LocalDate startDate, LocalDate endDate);

}
package id.co.asyst.wfm.dispatch.controller;

import id.co.asyst.wfm.dispatch.model.ActiveEnum;
import id.co.asyst.wfm.dispatch.model.TaskList;
import id.co.asyst.wfm.dispatch.payload.SuggestEmployeeTask;
import id.co.asyst.wfm.dispatch.payload.TaskListMonitoring;
import id.co.asyst.wfm.dispatch.response.ResponseProfile;
import id.co.asyst.wfm.dispatch.service.TaskListService;
import id.co.asyst.wfm.dispatch.util.ParsingResponse;
import id.co.asyst.wfm.dispatch.util.UploadFileResponse;
import javafx.concurrent.Task;
import org.hibernate.mapping.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dispatch/task-list")
public class TaskListController {

    @Autowired
    TaskListService taskListService;

    private static final Logger logger = LoggerFactory.getLogger(TaskListController.class);

    @GetMapping("/list")
    public List<TaskList> getAll() {
        return (List<TaskList>) taskListService.findAll();
    }

    @GetMapping(value = "/monitoring", params = {"shiftDate","employeeGroup"})
    public List<TaskListMonitoring> getTaskMonitoring(
            @RequestParam("shiftDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date,
            @RequestParam("employeeGroup") String employeeGroup) {

        LocalDate startDate = date.minusDays(1);
        LocalDate endDate = date.plusDays(1);

        List<TaskList> taskLists = taskListService.getTaskMonitoring(startDate, endDate, employeeGroup);

        Integer i = 0; Integer j = 0;

        List<TaskListMonitoring> taskListMonitorings = new ArrayList<TaskListMonitoring>();
        TaskListMonitoring taskListMonitoring = new TaskListMonitoring();
        List<TaskList> taskListDetail = new ArrayList<TaskList>();

        if (taskLists.isEmpty() || taskLists == null){
            return taskListMonitorings = null;
        }
        else {
            String employeeIdCurent = taskLists.get(i).getEmployeeId();
            taskListMonitoring.setEmployeeId(taskLists.get(i).getEmployeeId());
            taskListMonitoring.setEmployeeName(taskLists.get(i).getEmployeeName());
            taskListMonitoring.setEmployeeGroup(taskLists.get(i).getEmployeeGroup());

            for (i=0; i< taskLists.size(); i++){
                if (employeeIdCurent.equals(taskLists.get(i).getEmployeeId())){
                    if (i == taskLists.size() -1) {
                        //Add Data to List
                        taskListDetail.add(taskLists.get(i));
                        //Save Last List
                        taskListMonitoring.setTask(taskListDetail);
                        taskListMonitorings.add(taskListMonitoring);
                    }
                    else {
                        taskListDetail.add(taskLists.get(i));
                    }
                }
                else{
                    if (i == taskLists.size() - 1){
                        //Add Data Before to List
                        taskListMonitoring.setTask(taskListDetail);
                        taskListMonitorings.add(taskListMonitoring);

                        //Set New List
                        taskListMonitoring = new TaskListMonitoring();
                        taskListDetail = new ArrayList<TaskList>();
                        taskListMonitoring.setEmployeeId(taskLists.get(i).getEmployeeId());
                        taskListMonitoring.setEmployeeName(taskLists.get(i).getEmployeeName());
                        taskListMonitoring.setEmployeeGroup(taskLists.get(i).getEmployeeGroup());
                        taskListDetail.add(taskLists.get(i));
                        //Save Last List
                        taskListMonitoring.setTask(taskListDetail);
                        taskListMonitorings.add(taskListMonitoring);
                    }
                    else {
                        taskListMonitoring.setTask(taskListDetail);
                        taskListMonitorings.add(taskListMonitoring);
                        taskListMonitoring = new TaskListMonitoring();
                        taskListDetail = new ArrayList<TaskList>();
                        taskListMonitoring.setEmployeeId(taskLists.get(i).getEmployeeId());
                        taskListMonitoring.setEmployeeName(taskLists.get(i).getEmployeeName());
                        taskListMonitoring.setEmployeeGroup(taskLists.get(i).getEmployeeGroup());
                        taskListDetail.add(taskLists.get(i));
                    }
                }
                employeeIdCurent = taskLists.get(i).getEmployeeId();
            }
            return (List<TaskListMonitoring>) taskListMonitorings;
        }
    }

    @GetMapping(value = "/list", params = {"employeeId","employeeName","qualificationCode","status","employeeGroup"})
    public Page<TaskList> getAll(@RequestParam("employeeId") String id, @RequestParam("employeeName") String name,
                                 @RequestParam("qualificationCode") String qual,
                                 @RequestParam("status") String status, @RequestParam("employeeGroup") String group,
                                 /*@RequestParam("attendanceStatus") String attendanceStatus,*/ Pageable pageable) {
        return taskListService.search(id, name, qual, status, group, "", ActiveEnum.Y, pageable);
    }

    @GetMapping(value = "/list/{shiftDate}", params = {"employeeId","employeeName","qualificationCode","status","employeeGroup"})
    public Page<TaskList> getAll(@RequestParam("employeeId") String id, @RequestParam("employeeName") String name,
                                 @RequestParam("qualificationCode") String qual,
                                 @RequestParam("status") String status, @PathVariable("shiftDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date,
                                 @RequestParam("employeeGroup") String group, //@RequestParam("attendanceStatus") String attendanceStatus,
                                 Pageable pageable) {
        return taskListService.searchAndDate(id, name, qual, status, group, "", date, ActiveEnum.Y, pageable);
    }

    @GetMapping(value = "/list-null", params = {"qualificationCode","status","employeeGroup"})
    public Page<TaskList> getAllNull(@RequestParam("qualificationCode") String qual,
                                 @RequestParam("status") String status, @RequestParam("employeeGroup") String group,
                                 /*@RequestParam("attendanceStatus") String attendanceStatus,*/ Pageable pageable) {
        return taskListService.searchNull("", qual, status, group, "", ActiveEnum.Y, pageable);
    }

    @GetMapping(value = "/list-null/{shiftDate}", params = {"qualificationCode","status","employeeGroup"})
    public Page<TaskList> getAllNull(@RequestParam("qualificationCode") String qual,
                                 @RequestParam("status") String status, @PathVariable("shiftDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date,
                                 @RequestParam("employeeGroup") String group, //@RequestParam("attendanceStatus") String attendanceStatus,
                                 Pageable pageable) {
        return taskListService.searchNullAndDate("", qual, status, group, "", date, ActiveEnum.Y, pageable);
    }

    @GetMapping("/task-notif/{employeeId}/{date}")
    public Integer getTaskNotif(@PathVariable(value = "employeeId") String employeeId,
                                       @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> status = new ArrayList<>();
        status.add("Dispatch");
        status.add("Delayed");

        return taskListService.countTaskNotif(status, date, employeeId, ActiveEnum.Y);
    }

    @GetMapping("/list/{employeeId}")
    public List<TaskList> getEmployeeTask(@PathVariable(value = "employeeId") String employeeId) {
        List<String> status = new ArrayList<>();
        status.add("Dispatch");
        status.add("Confirm");
        status.add("Start");
        status.add("Delayed");
        status.add("Finish");

        Date input = new Date();
        LocalDate endDate = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startDate = endDate.minusDays(1);

        return (List<TaskList>) taskListService.findCurrentTask(employeeId, startDate, endDate, status, ActiveEnum.Y);
    }

    @GetMapping("/list/{employeeId}/{date}")
    public List<TaskList> getEmployeeTask(@PathVariable(value = "employeeId") String employeeId,
                                          @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<String> status = new ArrayList<>();
        status.add("Dispatch");
        status.add("Confirm");
        status.add("Start");
        status.add("Delayed");
        LocalDate startDate = date.minusDays(1);

        return (List<TaskList>) taskListService.findCurrentTask(employeeId, startDate, date, status, ActiveEnum.Y);
    }

    @GetMapping("/list-all/{employeeId}/{date}")
    public List<TaskList> getEmployeeTaskAll(@PathVariable(value = "employeeId") String employeeId,
                                          @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> status = new ArrayList<>();
        status.add("Dispatch");
        status.add("Confirm");
        status.add("Start");
        status.add("Delayed");
        status.add("Finish");
        LocalDate startDate = date.minusDays(1);

        return (List<TaskList>) taskListService.findCurrentTask(employeeId, startDate, date, status, ActiveEnum.Y);
    }

    @PostMapping("/create-old")
    public ResponseEntity<?> createOld(@Valid @RequestBody TaskList taskList) {
        Integer count = taskListService.countExist(taskList.getShiftDate(), taskList.getEmployeeId(), taskList.getQualificationCode(), taskList.getStartTime(), taskList.getEmployeeGroup());
        ResponseProfile responseProfile = null;

        if(count < 1){
            Integer countAttedaceStatus = taskListService.countAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
            if (countAttedaceStatus >= 1){
                String attendanceStatus = taskListService.assignAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
                taskList.setActive(ActiveEnum.Y);
                taskList.setAttendanceStatus(attendanceStatus);
            }
            else{
                taskList.setActive(ActiveEnum.Y);
                taskList.setAttendanceStatus("Absent");
            }
            try{
                taskListService.saveOrUpdate(taskList);
                responseProfile = new ResponseProfile("00000","Success","Create Task List", taskList);
                return new ResponseEntity<>(responseProfile, HttpStatus.OK);
            }
            catch (Exception e) {
                responseProfile = new ResponseProfile("00001",e.getMessage(),"Create Task List", taskList);
                return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
            }
        }
        else{
            responseProfile = new ResponseProfile("40009","Task List Already Exist","Create Task List");
            return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createValidasi(@Valid @RequestBody TaskList taskList) {

        ResponseProfile responseProfile = null;
        Integer checkExist = taskListService.countExist(taskList.getShiftDate(), taskList.getEmployeeId().toUpperCase(), taskList.getRuleDesc(), taskList.getStartTime(), taskList.getEmployeeGroup());
        if(checkExist < 1){
            if(taskList.getEmployeeId().isEmpty()){
                try{
                    taskList.setActive(ActiveEnum.Y);
                    taskList.setAttendanceStatus("Absent");
                    taskListService.saveOrUpdate(taskList);
                    responseProfile = new ResponseProfile("00000","Success","Create Task List", taskList);
                    return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                }
                catch (Exception e) {
                    responseProfile = new ResponseProfile("00001",e.getMessage(),"Create Task List");
                    return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                }
            }
            else{
                Integer checkEmployeeSchedule = taskListService.countEmployeeSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
                if(checkEmployeeSchedule == 0){
                    responseProfile = new ResponseProfile("00001","Employee Schedule Not Available","Create Task List");
                    return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                }
                else {
                    if (taskList.getQualificationCode() == null || taskList.getQualificationCode().isEmpty()){
                        responseProfile = new ResponseProfile("00001","Please Input Qualification","Create Task List");
                        return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                    }
                    else {
                        List<String> function = taskListService.getListFuncQual(taskList.getQualificationCode());
                        Integer checkEmployeeFunction = 0; Integer i = 0;
                        while (i < function.size()){
                            checkEmployeeFunction = taskListService.countEmployeeFunction(taskList.getEmployeeId(), function.get(i));
                            if (checkEmployeeFunction == 1){
                                break;
                            }
                            i++;
                        }

                        if(checkEmployeeFunction == 0){
                            responseProfile = new ResponseProfile("00001","Employee Doesn't Have Qualifications Task","Create Task List");
                            return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                        }
                        else{
                            Boolean checkRangeShiftTask = checkShift(taskList);
                            if (checkRangeShiftTask == false){
                                responseProfile = new ResponseProfile("00001","New Task Out Of Range Shift Schedule","Create Task List");
                                return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                            }
                            else {
                                Boolean checkNoConflictTask = checkTaskConflict(taskList);
                                if (checkNoConflictTask == false){
                                    responseProfile = new ResponseProfile("00001","New Task Conflict With Existing Task","Create Task List");
                                    return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                                }
                                else{
                                    // Checking Pass And Ready To Save
                                    String shiftTypeCode = taskListService.getShiftSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
                                    Integer countAttedaceStatus = taskListService.countAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
                                    if (countAttedaceStatus >= 1){
                                        String attendanceStatus = taskListService.assignAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
                                        String attendanceReminder = taskListService.assignAttendanceReminder(taskList.getEmployeeId(), taskList.getShiftDate());
                                        taskList.setActive(ActiveEnum.Y);
                                        taskList.setAttendanceStatus(attendanceStatus);
                                        taskList.setAttendanceReminder(attendanceReminder);
                                    }
                                    else{
                                        taskList.setActive(ActiveEnum.Y);
                                        taskList.setAttendanceStatus("Absent");
                                        taskList.setAttendanceReminder("N/A");
                                    }
                                    try{
                                        taskList.setEmployeeName(taskList.getEmployeeName().toUpperCase());
                                        taskList.setShiftTypeCode(shiftTypeCode);
                                        taskListService.saveOrUpdate(taskList);
                                        responseProfile = new ResponseProfile("00000","Success","Create Task List", taskList);
                                        return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                                    }
                                    catch (Exception e) {
                                        responseProfile = new ResponseProfile("00001",e.getMessage(),"Create Task List");
                                        return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            responseProfile = new ResponseProfile("40009","Task List Already Exist","Create Task List");
            return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/suggest-employee")
    public ResponseEntity<?> suggestEmployee(@Valid @RequestBody TaskList taskList) {

        ResponseProfile responseProfile = null;
        TaskList taskListTemp = null;
        List<String> employeeSchedule = taskListService.getEmployeeShiftDate(taskList.getShiftDate());
        List<String> employeeFunction = new ArrayList<String>();
        List<String> employeeShift = new ArrayList<String>();
        List<String> employeeTask = new ArrayList<String>();
        List<SuggestEmployeeTask> employeePropose = new ArrayList<SuggestEmployeeTask>();

        if (employeeSchedule.size() == 0){
            responseProfile = new ResponseProfile("00000","Employee Schedule Not Available","Suggest Employee");
            return new ResponseEntity<>(responseProfile, HttpStatus.OK);
        }
        else {
            if (taskList.getQualificationCode() == null || taskList.getQualificationCode().isEmpty()){
                responseProfile = new ResponseProfile("00000","Please Input Qualification","Suggest Employee");
                return new ResponseEntity<>(responseProfile, HttpStatus.OK);
            }
            else {
                List<String> function = taskListService.getListFuncQual(taskList.getQualificationCode());
                for (int i=0; i < employeeSchedule.size(); i++){
                    Integer checkEmployeeFunction = 0; int j = 0;
                    while (j < function.size()){
                        checkEmployeeFunction = taskListService.countEmployeeFunction(employeeSchedule.get(i), function.get(j));
                        if (checkEmployeeFunction == 1){
                            break;
                        }
                        j++;
                    }
                    if (checkEmployeeFunction > 0){
                        employeeFunction.add(employeeSchedule.get(i));
                    }
                }

                if (employeeFunction.isEmpty()){
                    responseProfile = new ResponseProfile("00000","" +
                            "Not Match Employee Have Qualification Task","Suggest Employee");
                    return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                }
                else{
                    for (int i = 0; i < employeeFunction.size(); i++){
                        taskListTemp = taskList;
                        taskListTemp.setEmployeeId(employeeFunction.get(i));
                        Boolean checkRangeShiftTask = checkShift(taskListTemp);

                        if(checkRangeShiftTask == true){
                            employeeShift.add(employeeFunction.get(i));
                        }
                    }

                    if(employeeShift.isEmpty()){
                        responseProfile = new ResponseProfile("00000","" +
                                "There is no Employee according to The Range Shift Task","Suggest Employee");
                        return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                    }
                    else {
                        for (int i=0; i < employeeShift.size(); i++){
                            taskListTemp = taskList;
                            taskListTemp.setEmployeeId(employeeShift.get(i));
                            Boolean checkNoConflictTask = checkTaskConflict(taskListTemp);

                            if(checkNoConflictTask == true){
                                employeeTask.add(employeeShift.get(i));
                            }
                        }

                        if (employeeTask.isEmpty()){
                            responseProfile = new ResponseProfile("00000","" +
                                    "There is no Employee according to Task Time (Conflict Task)","Suggest Employee", employeeShift);
                            return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                        }
                        else {
                            for (int i = 0; i < employeeTask.size(); i++){
                                List<TaskList> taskLists = taskListService.getTaskListCheck(employeeTask.get(i), taskList.getShiftDate(), "");
                                SuggestEmployeeTask suggestEmployeeTask = new SuggestEmployeeTask();
                                suggestEmployeeTask.setEmployeeId(taskLists.get(0).getEmployeeId());
                                suggestEmployeeTask.setEmployeeName(taskLists.get(0).getEmployeeName());
                                suggestEmployeeTask.setShiftDate(taskLists.get(0).getShiftDate());
                                suggestEmployeeTask.setShiftTypeCode(taskLists.get(0).getShiftTypeCode());
                                employeePropose.add(suggestEmployeeTask);
                            }
                            responseProfile = new ResponseProfile("00000","Success","Suggest Employee", employeePropose);
                            return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                        }
                    }
                }
            }
        }
    }

    @GetMapping("/find/{id}")
    public TaskList getById(@PathVariable(value = "id") Long taskId) {
        return (TaskList) taskListService.findById(taskId);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") Long taskId,
                           @Valid @RequestBody TaskList taskList) {

        ResponseProfile responseProfile = null;
        TaskList OldTaskList = (TaskList) taskListService.findById(taskId);
        OldTaskList.setActive(ActiveEnum.N);
        taskListService.saveOrUpdate(OldTaskList);

        if(taskList.getEmployeeId().isEmpty()){
            try{
                taskList.setCreatedBy(String.valueOf(OldTaskList.getId()));
                taskList.setActive(ActiveEnum.Y);
                taskList.setAttendanceStatus("Absent");
                taskListService.saveOrUpdate(taskList);
                responseProfile = new ResponseProfile("00000","Success","Create Task List", taskList);
                return new ResponseEntity<>(responseProfile, HttpStatus.OK);
            }
            catch (Exception e) {
                OldTaskList.setActive(ActiveEnum.Y);
                taskListService.saveOrUpdate(OldTaskList);
                responseProfile = new ResponseProfile("00001",e.getMessage(),"Create Task List");
                return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
            }
        }
        else{
            Integer checkEmployeeSchedule = taskListService.countEmployeeSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
            if(checkEmployeeSchedule == 0){
                OldTaskList.setActive(ActiveEnum.Y);
                taskListService.saveOrUpdate(OldTaskList);
                responseProfile = new ResponseProfile("00001","Employee Schedule Not Available","Create Task List");
                return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
            }
            else {
                if (taskList.getQualificationCode() == null || taskList.getQualificationCode().isEmpty()){
                    OldTaskList.setActive(ActiveEnum.Y);
                    taskListService.saveOrUpdate(OldTaskList);
                    responseProfile = new ResponseProfile("00001","Please Input Qualification","Create Task List");
                    return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                }
                else {
                    List<String> function = taskListService.getListFuncQual(taskList.getQualificationCode());
                    Integer checkEmployeeFunction = 0; Integer i = 0;
                    while (i < function.size()) {
                        checkEmployeeFunction = taskListService.countEmployeeFunction(taskList.getEmployeeId(), function.get(i));
                        if (checkEmployeeFunction == 1) {
                            break;
                        }
                    }
                    if(checkEmployeeFunction == 0){
                        OldTaskList.setActive(ActiveEnum.Y);
                        taskListService.saveOrUpdate(OldTaskList);
                        responseProfile = new ResponseProfile("00001","Employee Doesn't Have Qualifications Task","Create Task List");
                        return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                    }
                    else{
                        Boolean checkRangeShiftTask = checkShift(taskList);
                        if (checkRangeShiftTask == false){
                            OldTaskList.setActive(ActiveEnum.Y);
                            taskListService.saveOrUpdate(OldTaskList);
                            responseProfile = new ResponseProfile("00001","New Task Out Of Range Shift Schedule","Create Task List");
                            return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                        }
                        else {
                            Boolean checkNoConflictTask = checkTaskConflict(taskList);
                            if (checkNoConflictTask == false){
                                OldTaskList.setActive(ActiveEnum.Y);
                                taskListService.saveOrUpdate(OldTaskList);
                                responseProfile = new ResponseProfile("00001","New Task Conflict With Existing Task","Create Task List");
                                return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                            }
                            else{
                                // Checking Passed And Ready To Save
                                String shiftTypeCode = taskListService.getShiftSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
                                Integer countAttedaceStatus = taskListService.countAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
                                if (countAttedaceStatus >= 1){
                                    String attendanceStatus = taskListService.assignAttendanceStatus(taskList.getEmployeeId(), taskList.getShiftDate());
                                    String attendanceReminder = taskListService.assignAttendanceReminder(taskList.getEmployeeId(), taskList.getShiftDate());
                                    taskList.setActive(ActiveEnum.Y);
                                    taskList.setAttendanceStatus(attendanceStatus);
                                    taskList.setAttendanceReminder(attendanceReminder);
                                }
                                else{
                                    taskList.setActive(ActiveEnum.Y);
                                    taskList.setAttendanceStatus("Absent");
                                    taskList.setAttendanceReminder("N/A");
                                }
                                try{
                                    taskList.setEmployeeName(taskList.getEmployeeName().toUpperCase());
                                    taskList.setShiftTypeCode(shiftTypeCode);
                                    taskList.setCreatedBy(String.valueOf(OldTaskList.getId()));
                                    taskListService.saveOrUpdate(taskList);
                                    responseProfile = new ResponseProfile("00000","Success","Create Task List", taskList);
                                    return new ResponseEntity<>(responseProfile, HttpStatus.OK);
                                }
                                catch (Exception e) {
                                    responseProfile = new ResponseProfile("00001",e.getMessage(),"Create Task List");
                                    return new ResponseEntity<>(responseProfile, HttpStatus.BAD_REQUEST);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/confirm/{id}")
    public TaskList confirmTask(@PathVariable(value = "id") Long taskId) {

        TaskList taskList = (TaskList) taskListService.findById(taskId);
        String status = taskList.getStatus();

        if(status.equals("Dispatch" )){
            taskList.setStatus("Confirm");
        }
        return (TaskList) taskListService.saveOrUpdate(taskList);
    }

    @PostMapping("/start/{id}")
    public TaskList startTask(@PathVariable(value = "id") Long taskId) {

        TaskList taskList = (TaskList) taskListService.findById(taskId);

        taskList.setStatus("Start");
        return (TaskList) taskListService.saveOrUpdate(taskList);
    }

    @PostMapping("/delayed/{id}")
    public TaskList delayTask(@PathVariable(value = "id") Long taskId, @Valid @RequestBody TaskList taskListDetail) {

        TaskList taskList = (TaskList) taskListService.findById(taskId);

        taskList.setDelayReason(taskListDetail.getDelayReason());
        taskList.setDelayDuration(taskListDetail.getDelayDuration());
        taskList.setStatus("Delayed");
        return (TaskList) taskListService.saveOrUpdate(taskList);
    }

    @PostMapping("/end/{id}")
    public TaskList endTask(@PathVariable(value = "id") Long taskId) {

        TaskList taskList = (TaskList) taskListService.findById(taskId);

        taskList.setStatus("Finish");
        return (TaskList) taskListService.saveOrUpdate(taskList);
    }

    @PostMapping("/change/{id}")
    public TaskList dispatchTask(@PathVariable(value = "id") Long taskId) {
        TaskList taskList = (TaskList) taskListService.findById(taskId);

        taskList.setStatus("Dispatch");
        return (TaskList) taskListService.saveOrUpdate(taskList);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long  taskId) {

        TaskList taskList = (TaskList) taskListService.findById(taskId);
        taskList.setActive(ActiveEnum.N);
        taskListService.saveOrUpdate(taskList);
        //taskListService.deleteById(taskId);
        return ResponseEntity.ok().build();
    }

    // Methode for Validation Add New Task
    public Boolean checkShift(TaskList taskList) {

        String shiftTypeCode = taskListService.getShiftSchedule(taskList.getEmployeeId().toUpperCase(), taskList.getShiftDate());

        LocalDateTime shiftDateStart = null; LocalDateTime shiftDateEnd = null;
        LocalTime shiftStartTime =  taskListService.getShiftStartTime(shiftTypeCode);
        LocalTime shiftEndTime = taskListService.getShiftEndTime(shiftTypeCode);
        LocalDateTime newDateStart = null; LocalDateTime newDateEnd = null;
        LocalTime newStartTime = taskList.getStartTime();
        LocalTime newEndTime = taskList.getEndTime();
        LocalTime timeTreshold = LocalTime.parse("08:00:00");

        if (newStartTime.isAfter(newEndTime)){
            if(shiftStartTime.isAfter(shiftEndTime)){
                shiftDateStart = LocalDateTime.of(taskList.getShiftDate(), shiftStartTime);
                shiftDateEnd = LocalDateTime.of(taskList.getShiftDate().plusDays(1), shiftEndTime);
                newDateStart = LocalDateTime.of(taskList.getShiftDate(), newStartTime);
                newDateEnd = LocalDateTime.of(taskList.getShiftDate().plusDays(1), newEndTime);

                if(newDateStart.isAfter(shiftDateStart) || newDateStart.isEqual(shiftDateStart)){
                    if(newDateEnd.isBefore(shiftDateEnd) || newDateEnd.isEqual(shiftDateEnd)){
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }
        else {
            if(shiftEndTime.isAfter(shiftStartTime)){
                shiftDateStart = LocalDateTime.of(taskList.getShiftDate(), shiftStartTime);
                shiftDateEnd = LocalDateTime.of(taskList.getShiftDate(), shiftEndTime);
                newDateStart = LocalDateTime.of(taskList.getShiftDate(), newStartTime);
                newDateEnd = LocalDateTime.of(taskList.getShiftDate(), newEndTime);

                if(newDateStart.isAfter(shiftDateStart) || newDateStart.isEqual(shiftDateStart)){
                    if(newDateEnd.isBefore(shiftDateEnd) || newDateEnd.isEqual(shiftDateEnd)){
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                else{
                    return false;
                }
            }
            else {
                return false;
            }
        }
    }

    public Boolean checkTaskConflict(@Valid @RequestBody TaskList taskList) {

        List<TaskList> taskListCheck = taskListService.getTaskListCheck(taskList.getEmployeeId(), taskList.getShiftDate(), taskList.getShiftTypeCode());
        Integer checkConflict = 0;

        for(int i = 0; i < taskListCheck.size(); i++){
            String shiftTypeCode = taskListService.getShiftSchedule(taskListCheck.get(i).getEmployeeId(), taskListCheck.get(i).getShiftDate());
            LocalDateTime shiftDateStart = null; LocalDateTime shiftDateEnd = null;
            LocalTime shiftStartTime =  taskListService.getShiftStartTime(shiftTypeCode);
            LocalTime shiftEndTime = taskListService.getShiftEndTime(shiftTypeCode);
            LocalTime timeTreshold = LocalTime.parse("08:00:00");

            // New Task
            LocalDateTime newDateStart = null; LocalDateTime newDateEnd = null;
            LocalTime newStartTime = taskList.getStartTime();
            LocalTime newEndTime = taskList.getEndTime();

            // Existing Task
            LocalDateTime exDateStart = null; LocalDateTime exDateEnd = null;
            LocalTime exStartTime = taskListCheck.get(i).getStartTime();
            LocalTime exEndTime = taskListCheck.get(i).getEndTime();

            if (newStartTime.isAfter(newEndTime)){
                newDateStart = LocalDateTime.of(taskList.getShiftDate(), newStartTime);
                newDateEnd = LocalDateTime.of(taskList.getShiftDate().plusDays(1), newEndTime);
            }
            else {
                newDateStart = LocalDateTime.of(taskList.getShiftDate(), newStartTime);
                newDateEnd = LocalDateTime.of(taskList.getShiftDate(), newEndTime);
            }

            if (exStartTime.isAfter(exEndTime)){
                exDateStart = LocalDateTime.of(taskList.getShiftDate(), exStartTime);
                exDateEnd = LocalDateTime.of(taskList.getShiftDate().plusDays(1), exEndTime);
            }
            else{
                if(shiftStartTime.isAfter(shiftEndTime)){
                    if (exStartTime.isBefore(timeTreshold)){
                        exDateStart = LocalDateTime.of(taskList.getShiftDate().plusDays(1), exStartTime);
                        exDateEnd = LocalDateTime.of(taskList.getShiftDate().plusDays(1), exEndTime);
                    }
                    else {
                        exDateStart = LocalDateTime.of(taskList.getShiftDate(), exStartTime);
                        exDateEnd = LocalDateTime.of(taskList.getShiftDate(), exEndTime);
                    }
                }
                else {
                    exDateStart = LocalDateTime.of(taskList.getShiftDate(), exStartTime);
                    exDateEnd = LocalDateTime.of(taskList.getShiftDate(), exEndTime);
                }
            }

            if(newDateStart.isBefore(exDateStart) && newDateEnd.isBefore(exDateStart)){
                checkConflict = checkConflict + 1;
            }
            else if(newDateStart.isBefore(exDateStart) && newDateEnd.isEqual(exDateStart)){
                checkConflict = checkConflict + 1;
            }
            else if (newDateStart.isEqual(exDateEnd) || newDateStart.isAfter(exDateEnd)){
                checkConflict = checkConflict + 1;
            }
            else{
                System.out.println("New Task : "+newDateStart+"/"+newDateEnd+ " Existing Task : "+exDateStart+"/"+exDateEnd);
                checkConflict = checkConflict + 0;
            }
        }
        if (checkConflict == taskListCheck.size()){
            return true;
        }
        else {
            return false;
        }
    }
}

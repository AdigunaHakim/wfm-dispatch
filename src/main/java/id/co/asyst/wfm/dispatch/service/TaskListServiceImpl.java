package id.co.asyst.wfm.dispatch.service;

import id.co.asyst.wfm.core.service.BaseServiceManager;
import id.co.asyst.wfm.dispatch.config.FileStorageProperties;
import id.co.asyst.wfm.dispatch.exception.FileStorageException;
import id.co.asyst.wfm.dispatch.exception.MyFileNotFoundException;
import id.co.asyst.wfm.dispatch.model.ActiveEnum;
import id.co.asyst.wfm.dispatch.model.TaskList;
import id.co.asyst.wfm.dispatch.repository.TaskListRepository;
import id.co.asyst.wfm.dispatch.util.ParsingResponse;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class TaskListServiceImpl extends BaseServiceManager<TaskList, Long> implements TaskListService<TaskList, Long>{

    @Autowired
    TaskListRepository taskListRepository;

    private final Path fileStorageLocation;

    @Autowired
    public TaskListServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    //Upload
    @Override
    public ParsingResponse storeFile2003(MultipartFile file, String employeeGroup) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //Start import excel
            List<TaskList> taskListUpload = new ArrayList<>();
            int i = 12; Integer totalRecord = 0; Integer successRecord = 0; Integer failedRecord = 0;
            InputStream excelRead = new FileInputStream(String.valueOf(targetLocation));
            HSSFWorkbook workbook = new HSSFWorkbook(excelRead);
            HSSFSheet worksheet = workbook.getSheetAt(0);

            // Reads the data in excel file until last row is encountered
            while (i <= worksheet.getLastRowNum()-4) {

                // Creates an object for the UserInfo Model
                TaskList taskList = new TaskList();
                // Creates an object representing a single row in excel
                HSSFRow row = worksheet.getRow(i++);

                String indicator, qualification, ruleDesc, airline, acType;
                int tripNumber;

                //set indicator
                if(row.getCell(6) == null){
                    indicator = "";
                }
                else{
                    indicator = row.getCell(6).getStringCellValue();
                }

                //set qualification
                if(row.getCell(11) == null){
                    qualification = "";
                }
                else{
                    qualification = row.getCell(11).getStringCellValue();
                }

                //set rule desc
                if(row.getCell(12) == null){
                    ruleDesc = "";
                }
                else{
                    ruleDesc = row.getCell(12).getStringCellValue();
                }

                //set airline
                if(row.getCell(13) == null){
                    airline = "";
                }
                else{
                    airline = row.getCell(13).getStringCellValue();
                }

                //set tripNumber
                if(row.getCell(14) == null){
                    tripNumber = 0;
                }
                else{
                    tripNumber = Integer.parseInt(row.getCell(14).getStringCellValue());
                }

                //set acType
                if(row.getCell(15) == null){
                    acType ="";
                }
                else{
                    acType = row.getCell(15).getStringCellValue();
                }

                DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                Date shiftDate = format.parse(row.getCell(1).getStringCellValue());
                taskList.setShiftDate(shiftDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                taskList.setShiftTypeCode(row.getCell(2, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                taskList.setEmployeeId(row.getCell(3, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                String employeeName = row.getCell(4, Row.RETURN_BLANK_AS_NULL).getStringCellValue();
                taskList.setEmployeeName(employeeName.substring(0, employeeName.length() -1));
                taskList.setIndicator(indicator);
                taskList.setTaskType(row.getCell(7, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                taskList.setStartTime(LocalTime.parse(row.getCell(8, Row.RETURN_BLANK_AS_NULL).getStringCellValue()));
                taskList.setEndTime(LocalTime.parse(row.getCell(9, Row.RETURN_BLANK_AS_NULL).getStringCellValue()));
                taskList.setQualificationCode(qualification);
                taskList.setRuleDesc(ruleDesc);
                taskList.setAirline(airline);
                taskList.setTripNumber(tripNumber);
                taskList.setAcType(acType);
                taskList.setStatus("Planned");
                taskList.setAttendanceStatus("Absent");
                taskList.setAttendanceReminder("N/A");
                taskList.setActive(ActiveEnum.Y);

                //Set Shift Indicator
                Integer checkShift = taskListRepository.countShift(taskList.getShiftTypeCode());
                if(checkShift > 0){
                    Integer shiftIndicator = taskListRepository.getShiftIndicator(taskList.getShiftTypeCode());
                    taskList.setShiftIndicator(shiftIndicator);
                }

                //Parse Task to Employee Schedule
                String scheduleId = taskList.getShiftDate().toString().replaceAll("-","").concat(taskList.getEmployeeId());
                Integer checkExistSchedule = taskListRepository.countEmployeeSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
                if (checkExistSchedule < 1){
                    LocalTime shiftStartTime = null ; LocalTime shiftEndTime = null; Integer shiftIndicator = null; String shiftPublishName = null;
                    if(checkShift > 0){
                        shiftIndicator = taskListRepository.getShiftIndicator(taskList.getShiftTypeCode());
                        shiftPublishName = taskListRepository.getShiftPublishName(taskList.getShiftTypeCode());
                        shiftStartTime = taskListRepository.getShiftStartTime(taskList.getShiftTypeCode());
                        shiftEndTime = taskListRepository.getShiftEndTime(taskList.getShiftTypeCode());
                    }

                    taskListRepository.toEmployeeSchedule(scheduleId, taskList.getEmployeeId(), taskList.getEmployeeName(), taskList.getEmployeeGroup(), taskList.getShiftDate(), taskList.getShiftTypeCode(), shiftPublishName,
                            shiftIndicator, shiftStartTime, shiftEndTime, taskList.getQualificationCode(), "N/A", "Absent");
                }

                // persist data into database in here
                Integer count = taskListRepository.countByShiftDateAndEmployeeIdAndQualificationCodeAndStartTimeAndEmployeeGroup(shiftDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),row.getCell(3, Row.RETURN_BLANK_AS_NULL).getStringCellValue(),qualification,LocalTime.parse(row.getCell(8, Row.RETURN_BLANK_AS_NULL).getStringCellValue()),employeeGroup);

                if(count < 1){
                    saveOrUpdate(taskList);
                    successRecord++;
                }
                else{
                    failedRecord++;
                }
                totalRecord++;
            }
            workbook.close();

            return new ParsingResponse(fileName, totalRecord, successRecord, failedRecord);
        } catch (IOException | NullPointerException | ParseException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public ParsingResponse storeFile2007(MultipartFile file, String employeeGroup) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //Start import excel
            List<TaskList> taskListUpload = new ArrayList<>();
            int i = 12; Integer totalRecord = 0; Integer successRecord = 0; Integer failedRecord = 0;
            InputStream excelRead = new FileInputStream(String.valueOf(targetLocation));
            XSSFWorkbook workbook = new XSSFWorkbook(excelRead);
            XSSFSheet worksheet = workbook.getSheetAt(0);

            // Reads the data in excel file until last row is encountered
            while (i <= worksheet.getLastRowNum()-4) {

                // Creates an object for the UserInfo Model
                TaskList taskList = new TaskList();
                // Creates an object representing a single row in excel
                XSSFRow row = worksheet.getRow(i++);

                String indicator, qualification, ruleDesc, airline, acType;
                int tripNumber;

                //set indicator
                if(row.getCell(6) == null){
                    indicator = "";
                }
                else{
                    indicator = row.getCell(6).getStringCellValue();
                }

                //set qualification
                if(row.getCell(11) == null){
                    qualification = "";
                }
                else{
                    qualification = row.getCell(11).getStringCellValue();
                }

                //set rule desc
                if(row.getCell(12) == null){
                    ruleDesc = "";
                }
                else{
                    ruleDesc = row.getCell(12).getStringCellValue();
                }

                //set airline
                if(row.getCell(13) == null){
                    airline = "";
                }
                else{
                    airline = row.getCell(13).getStringCellValue();
                }

                //set tripNumber
                if(row.getCell(14) == null){
                    tripNumber = 0;
                }
                else{
                    tripNumber = Integer.parseInt(row.getCell(14).getStringCellValue());
                }

                //set acType
                if(row.getCell(15) == null){
                    acType ="";
                }
                else{
                    acType = row.getCell(15).getStringCellValue();
                }

                taskList.setEmployeeGroup(employeeGroup);
                DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                Date shiftDate = format.parse(row.getCell(1).getStringCellValue());
                taskList.setShiftDate(shiftDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                taskList.setShiftTypeCode(row.getCell(2, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                taskList.setEmployeeId(row.getCell(3, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                String employeeName = row.getCell(4, Row.RETURN_BLANK_AS_NULL).getStringCellValue();
                taskList.setEmployeeName(employeeName.substring(0, employeeName.length() -1));
                taskList.setIndicator(indicator);
                taskList.setTaskType(row.getCell(7, Row.RETURN_BLANK_AS_NULL).getStringCellValue());
                taskList.setStartTime(LocalTime.parse(row.getCell(8, Row.RETURN_BLANK_AS_NULL).getStringCellValue()));
                taskList.setEndTime(LocalTime.parse(row.getCell(9, Row.RETURN_BLANK_AS_NULL).getStringCellValue()));
                taskList.setQualificationCode(qualification);
                taskList.setRuleDesc(ruleDesc);
                taskList.setAirline(airline);
                taskList.setTripNumber(tripNumber);
                taskList.setAcType(acType);
                taskList.setStatus("Planned");
                taskList.setAttendanceStatus("Absent");
                taskList.setAttendanceReminder("N/A");
                taskList.setActive(ActiveEnum.Y);

                //Set Shift Indicator
                Integer checkShift = taskListRepository.countShift(taskList.getShiftTypeCode());
                if(checkShift > 0){
                    Integer shiftIndicator = taskListRepository.getShiftIndicator(taskList.getShiftTypeCode());
                    taskList.setShiftIndicator(shiftIndicator);
                }

                //Parse Task to Employee Schedule
                String scheduleId = taskList.getShiftDate().toString().replaceAll("-","").concat(taskList.getEmployeeId());
                Integer checkExistSchedule = taskListRepository.countEmployeeSchedule(taskList.getEmployeeId(), taskList.getShiftDate());
                if (checkExistSchedule < 1){
                    LocalTime shiftStartTime = null ; LocalTime shiftEndTime = null; Integer shiftIndicator = null; String shiftPublishName = null;
                    if(checkShift > 0){
                        shiftIndicator = taskListRepository.getShiftIndicator(taskList.getShiftTypeCode());
                        shiftPublishName = taskListRepository.getShiftPublishName(taskList.getShiftTypeCode());
                        shiftStartTime = taskListRepository.getShiftStartTime(taskList.getShiftTypeCode());
                        shiftEndTime = taskListRepository.getShiftEndTime(taskList.getShiftTypeCode());
                    }

                    taskListRepository.toEmployeeSchedule(scheduleId, taskList.getEmployeeId(), taskList.getEmployeeName(), taskList.getEmployeeGroup(), taskList.getShiftDate(), taskList.getShiftTypeCode(), shiftPublishName,
                            shiftIndicator, shiftStartTime, shiftEndTime, taskList.getQualificationCode(), "N/A", "Absent");
                }

                // persist data into database in here
                Integer count = taskListRepository.countByShiftDateAndEmployeeIdAndQualificationCodeAndStartTimeAndEmployeeGroup(shiftDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), row.getCell(3, Row.RETURN_BLANK_AS_NULL).getStringCellValue(), qualification, LocalTime.parse(row.getCell(8, Row.RETURN_BLANK_AS_NULL).getStringCellValue()), employeeGroup);

                if(count < 1){
                    //taskListUpload.add(taskList);
                    saveOrUpdate(taskList);
                    successRecord++;
                }
                else{
                    failedRecord++;
                }
                totalRecord++;
            }
            workbook.close();
            //taskListRepository.saveAll(taskListUpload);
            //taskListRepository.flush();

            return new ParsingResponse(fileName, totalRecord, successRecord, failedRecord);
        } catch (IOException | NullPointerException | ParseException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    @Override
    public Page<TaskList> searchNull(String id, String qual, String status, String group, String attendanceStatus, ActiveEnum active, Pageable pageable) {
        return taskListRepository.findByEmployeeIdAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndActive(id, qual, status, group, attendanceStatus, active, pageable);
    }

    @Override
    public Page<TaskList> searchNullAndDate(String id, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable) {
        return taskListRepository.findByEmployeeIdAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndShiftDateAndActive(id, qual, status, group, attendanceStatus, date, active, pageable);
    }

    //Basic Task List
    @Override
    public Collection<TaskList> findAll() {
        return (Collection<TaskList>) taskListRepository.findAll();
    }

    @Override
    public TaskList findById(Long aLong) {
        return taskListRepository.findById(aLong).get();
    }

    @Override
    public TaskList saveOrUpdate(TaskList taskList) {
        return taskListRepository.saveAndFlush(taskList);
    }

    @Override
    public void deleteById(Long aLong) {
        taskListRepository.deleteById(aLong);
    }

    @Override
    public void delete(TaskList taskList) {
        taskListRepository.delete(taskList);
    }

    @Override
    public Page<TaskList> search(String id, String name, String qual, String status, String group, String attendanceStatus, ActiveEnum active,  Pageable pageable) {
        return taskListRepository.findByEmployeeIdContainsAndEmployeeNameContainsAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndActive
                (id, name, qual, status, group, attendanceStatus, active, pageable);
    }

    @Override
    public Page<TaskList> searchAndDate(String id, String name, String qual, String status, String group, String attendanceStatus, LocalDate date, ActiveEnum active, Pageable pageable) {
        return taskListRepository.findByEmployeeIdContainsAndEmployeeNameContainsAndQualificationCodeContainsAndStatusContainsAndEmployeeGroupContainsAndAttendanceStatusContainsAndShiftDateAndActive
                (id, name, qual, status, group, attendanceStatus, date, active, pageable);
    }

    @Override
    public Page<TaskList> findByStatus(String status, Pageable pageable) {
        return taskListRepository.findByStatus(status, pageable);
    }

    @Override
    public List<TaskList> findByEmployeeId(String employeeId, List<String> status, ActiveEnum active) {
        return taskListRepository.findByEmployeeIdAndStatusInAndActive(employeeId, status, active);
    }

    @Override
    public List<TaskList> findCurrentTask(String employeeId, LocalDate startDate, LocalDate endDate, List<String> status, ActiveEnum active) {
        return taskListRepository.findByEmployeeIdAndShiftDateGreaterThanEqualAndShiftDateLessThanEqualAndStatusInAndActive(employeeId, startDate, endDate, status, active);
    }

    @Override
    public List<TaskList> saveAll(List<TaskList> taskLists) {
        return taskListRepository.saveAll(taskLists);
    }

    @Override
    public Integer countTaskNotif(List<String> status, LocalDate date, String employeeId, ActiveEnum active) {
        return taskListRepository.countByStatusInAndShiftDateLessThanEqualAndEmployeeIdAndActive(status, date, employeeId, active);
    }

    @Override
    public Integer countExist(LocalDate date, String id, String qualification, LocalTime time, String group) {
        return taskListRepository.countByShiftDateAndEmployeeIdAndQualificationCodeAndStartTimeAndEmployeeGroup(date, id, qualification, time, group);
    }

    // Attendance Task List
    @Override
    public Integer countAttendanceStatus(String employeeId, LocalDate shiftDate) {
        return taskListRepository.countAttendanceStatus(employeeId, shiftDate);
    }

    @Override
    public String assignAttendanceStatus(String employeeId, LocalDate shiftDate) {
        return taskListRepository.assignAttendanceStatus(employeeId, shiftDate);
    }

    @Override
    public String assignAttendanceReminder(String employeeId, LocalDate shiftDate) {
        return taskListRepository.assignAttendanceReminder(employeeId, shiftDate);
    }

    // Monitoring Task List
    @Override
    public List<TaskList> getTaskMonitoring(LocalDate startDate, LocalDate endDate, String employeeGroup) {
        return taskListRepository.getTaskMonitoring(startDate, endDate, employeeGroup);
    }

    // Validasi Task List
    @Override
    public Integer countEmployeeSchedule(String employeeId, LocalDate shiftDate) {
        return taskListRepository.countEmployeeSchedule(employeeId, shiftDate);
    }

    @Override
    public List<String> getListFuncQual(String qualificationCode) {
        return taskListRepository.getListFuncQual(qualificationCode);
    }

    @Override
    public Integer countEmployeeFunction(String employeeId, String functionCode) {
        return taskListRepository.countEmployeeFunction(employeeId, functionCode);
    }

    @Override
    public String getShiftSchedule(String employeeId, LocalDate shiftDate) {
        return taskListRepository.getShiftSchedule(employeeId, shiftDate);
    }

    @Override
    public LocalTime getShiftStartTime(String shiftTypeCode) {
        return taskListRepository.getShiftStartTime(shiftTypeCode);
    }

    @Override
    public LocalTime getShiftEndTime(String shiftTypeCode) {
        return taskListRepository.getShiftEndTime(shiftTypeCode);
    }

    @Override
    public Integer getShiftIndicator(String shiftTypeCode) {
        return taskListRepository.getShiftIndicator(shiftTypeCode);
    }

    @Override
    public Integer countShift(String shiftTypeCode) {
        return taskListRepository.countShift(shiftTypeCode);
    }

    @Override
    public List<TaskList> getTaskListCheck(String employeeId, LocalDate shiftDate, String shiftTypeCode) {
        return taskListRepository.getTaskListCheck(employeeId, shiftDate, shiftTypeCode);
    }

    @Override
    public List<String> getEmployeeShiftDate(LocalDate shiftDate) {
        return taskListRepository.getEmployeeShiftDate(shiftDate);
    }

    //Scheduler Task Check
    @Override
    public List<TaskList> getgetTaskListCheckDelayed(LocalDate startDate, LocalDate endDate) {
        return taskListRepository.getTaskListCheckDelayed(startDate, endDate);
    }
}

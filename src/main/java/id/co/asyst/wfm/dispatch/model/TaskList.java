package id.co.asyst.wfm.dispatch.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import id.co.asyst.wfm.core.model.BaseModel;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "TASK_LIST")
public class TaskList extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*Employee Data*/
    private String employeeGroup;
    private String employeeId;
    private String employeeName;
    private String function;

    /*Task & Shift Data*/
    private LocalDate shiftDate;
    private String shiftTypeCode;
    private Integer shiftIndicator;
    private String taskType;
    private String indicator;
    private String qualificationCode;
    private String ruleDesc;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime endTime;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime startShiftTime;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime endShiftTime;
    private String status;
    private String attendanceStatus;
    private String attendanceReminder;
    private String delayReason;
    private Integer delayDuration;

    /*Flight Data*/
    private String airline;
    private Integer tripNumber;
    private String AcType;
    private String station;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime arrivalTime;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING, timezone="CET")
    private LocalTime departureTime;
    private String arrivalRoute;
    private String departureRoute;
    private ActiveEnum active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeGroup() {
        return employeeGroup;
    }

    public void setEmployeeGroup(String employeeGroup) {
        this.employeeGroup = employeeGroup;
    }

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

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftTypeCode() {
        return shiftTypeCode;
    }

    public void setShiftTypeCode(String shiftTypeCode) {
        this.shiftTypeCode = shiftTypeCode;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getQualificationCode() {
        return qualificationCode;
    }

    public void setQualificationCode(String qualificationCode) {
        this.qualificationCode = qualificationCode;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalTime getStartShiftTime() {
        return startShiftTime;
    }

    public void setStartShiftTime(LocalTime startShiftTime) {
        this.startShiftTime = startShiftTime;
    }

    public LocalTime getEndShiftTime() {
        return endShiftTime;
    }

    public void setEndShiftTime(LocalTime endShiftTime) {
        this.endShiftTime = endShiftTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public Integer getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(Integer tripNumber) {
        this.tripNumber = tripNumber;
    }

    public String getAcType() {
        return AcType;
    }

    public void setAcType(String acType) {
        AcType = acType;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalRoute() {
        return arrivalRoute;
    }

    public void setArrivalRoute(String arrivalRoute) {
        this.arrivalRoute = arrivalRoute;
    }

    public String getDepartureRoute() {
        return departureRoute;
    }

    public void setDepartureRoute(String departureRoute) {
        this.departureRoute = departureRoute;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public ActiveEnum getActive() {
        return active;
    }

    public void setActive(ActiveEnum active) {
        this.active = active;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public Integer getShiftIndicator() {
        return shiftIndicator;
    }

    public void setShiftIndicator(Integer shiftIndicator) {
        this.shiftIndicator = shiftIndicator;
    }

    public Integer getDelayDuration() {
        return delayDuration;
    }

    public void setDelayDuration(Integer delayDuration) {
        this.delayDuration = delayDuration;
    }

    public String getAttendanceReminder() {
        return attendanceReminder;
    }

    public void setAttendanceReminder(String attendanceReminder) {
        this.attendanceReminder = attendanceReminder;
    }
}


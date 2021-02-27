package id.co.asyst.wfm.dispatch.payload;

import java.time.LocalDate;

public class SuggestEmployeeTask {

    private String employeeId;

    private String employeeName;

    private LocalDate shiftDate;

    private String shiftTypeCode;

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
}

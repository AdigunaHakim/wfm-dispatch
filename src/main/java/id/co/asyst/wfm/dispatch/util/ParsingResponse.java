package id.co.asyst.wfm.dispatch.util;

public class ParsingResponse {

    private String filename;

    private Integer totalRecord;

    private Integer successRecord;

    private Integer failedRecord;

    ParsingResponse(){

    }

    public ParsingResponse(String filename, Integer totalRecord, Integer successRecord, Integer failedRecord){

        this.filename = filename;
        this.totalRecord = totalRecord;
        this.successRecord = successRecord;
        this.failedRecord = failedRecord;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getSuccessRecord() {
        return successRecord;
    }

    public void setSuccessRecord(Integer successRecord) {
        this.successRecord = successRecord;
    }

    public Integer getFailedRecord() {
        return failedRecord;
    }

    public void setFailedRecord(Integer failedRecord) {
        this.failedRecord = failedRecord;
    }

    public Integer getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(Integer totalRecord) {
        this.totalRecord = totalRecord;
    }
}

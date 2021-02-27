package id.co.asyst.wfm.dispatch.util;

public class UploadFileResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private Integer totalRecord;
    private Integer successRecord;
    private Integer failedRecord;

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size, Integer totalRecord, Integer successRecord, Integer failedRecord) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
        this.totalRecord = totalRecord;
        this.successRecord = successRecord;
        this.failedRecord = failedRecord;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDownloadUri() {
        return fileDownloadUri;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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


package id.co.asyst.wfm.dispatch.response;

public class ResponseProfile {

    private String messageCode;

    private String message;

    private String service;

    private Object data;

    public ResponseProfile(){
    }

    public ResponseProfile(String messageCode, String message, String service){
        this.messageCode = messageCode;
        this.message = message;
        this.service = service;
    }

    public ResponseProfile(String messageCode, String message, String service, Object data){

        this.messageCode = messageCode;
        this.message = message;
        this.service = service;
        this.data = data;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}


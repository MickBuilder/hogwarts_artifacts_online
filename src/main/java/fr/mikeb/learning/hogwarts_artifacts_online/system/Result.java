package fr.mikeb.learning.hogwarts_artifacts_online.system;

/**
 * This class defines the schema of the response. It is used to encapsulate data prepared by
 * the server side, this object will be serialized to JSON before sent back to the client end.
 */
public class Result<T> {
    private boolean flag; // Two values: true means success, false means not success
    private Integer code; // Status code. e.g., 200
    private String message; // Response message
    private T data; // The response payload

    public Result() {}

    public Result(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public Result(boolean flag, Integer code, String message, T data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
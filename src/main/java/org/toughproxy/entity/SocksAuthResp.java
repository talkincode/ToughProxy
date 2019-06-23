package org.toughproxy.entity;

public class SocksAuthResp {

    public final static SocksAuthResp SUCCESS = new SocksAuthResp(0,"SUCCESS");
    public final static SocksAuthResp UNKNOW_ERROR = new SocksAuthResp(1,"UNKNOW ERROR");
    public final static SocksAuthResp USER_NOT_EXISTS = new SocksAuthResp(1,"USER NOT EXISTS");
    public final static SocksAuthResp USER_PASSWD_ERROR = new SocksAuthResp(1,"USER PASSWD ERROR");
    public final static SocksAuthResp USER_EXPIRE = new SocksAuthResp(1,"USER EXPIRE");
    public final static SocksAuthResp USER_STATUS_ERROR = new SocksAuthResp(1,"USER STATUS ERROR");
    public final static SocksAuthResp USER_GROUP_STATUS_ERROR = new SocksAuthResp(1,"USER GROUP STATUS ERROR");

    private int code;
    private String message;
    private int upLimit;
    private int downLimit;
    private int maxSession;

    public SocksAuthResp() {
    }

    public SocksAuthResp(int code) {
        this.code = code;
    }

    public SocksAuthResp(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUpLimit() {
        return upLimit;
    }

    public void setUpLimit(int upLimit) {
        this.upLimit = upLimit;
    }

    public int getDownLimit() {
        return downLimit;
    }

    public void setDownLimit(int downLimit) {
        this.downLimit = downLimit;
    }

    public int getMaxSession() {
        return maxSession;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    @Override
    public String toString() {
        return "SocksAuthResp{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", upLimit=" + upLimit +
                ", downLimit=" + downLimit +
                ", maxSession=" + maxSession +
                '}';
    }
}

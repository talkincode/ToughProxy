package org.toughsocks.form;

import org.toughsocks.common.DateTimeUtil;
import org.toughsocks.common.ValidateUtil;
import org.toughsocks.entity.User;

public class UserForm {

    private Long id;
    private Long groupId;
    private String username;
    private String realname;
    private String email;
    private String mobile;
    private String password;
    private String cpassword;
    private Integer status;
    private String beginTime;
    private String expireTime;
    private String createTime;
    private String updateTime;
    private String remark;

    private String userPrefix;
    private int openNum;
    private int randPasswd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCpassword() {
        return cpassword;
    }

    public void setCpassword(String cpassword) {
        this.cpassword = cpassword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserPrefix() {
        return userPrefix;
    }

    public void setUserPrefix(String userPrefix) {
        this.userPrefix = userPrefix;
    }

    public int getOpenNum() {
        return openNum;
    }

    public void setOpenNum(int openNum) {
        this.openNum = openNum;
    }

    public int getRandPasswd() {
        return randPasswd;
    }

    public void setRandPasswd(int randPasswd) {
        this.randPasswd = randPasswd;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public User getUserData(){
        User subs = new User();
        subs.setId(getId());
        subs.setUsername(getUsername());
        if(ValidateUtil.isNotEmpty(getExpireTime()) && getExpireTime().length() == 16)
            subs.setExpireTime(DateTimeUtil.toTimestamp(getExpireTime()+":00"));
        subs.setUpdateTime(DateTimeUtil.nowTimestamp());
        subs.setCreateTime(DateTimeUtil.nowTimestamp());
        subs.setRealname(getRealname());
        subs.setEmail(getEmail());
        subs.setMobile(getMobile());
        subs.setStatus(getStatus());
        subs.setRemark(getRemark());
        subs.setPassword(getPassword());
        return subs;
    }

}

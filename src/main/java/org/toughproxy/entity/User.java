package org.toughproxy.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Timestamp;

public class User {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long groupId;
    private Integer groupPolicy;
    private String realname;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private Integer status;
    private Integer upLimit;
    private Integer downLimit;
    private Integer maxSession;
    private Integer maxClient;
    private Integer groupStatus;
    private Integer groupUpLimit;
    private Integer groupDownLimit;
    private Integer groupMaxSession;
    private Timestamp expireTime;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String remark;

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

    public Integer getGroupPolicy() {
        return groupPolicy;
    }

    public void setGroupPolicy(Integer groupPolicy) {
        this.groupPolicy = groupPolicy;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getUpLimit() {
        return upLimit;
    }

    public void setUpLimit(Integer upLimit) {
        this.upLimit = upLimit;
    }

    public Integer getDownLimit() {
        return downLimit;
    }

    public void setDownLimit(Integer downLimit) {
        this.downLimit = downLimit;
    }

    public Integer getMaxSession() {
        return maxSession;
    }

    public void setMaxSession(Integer maxSession) {
        this.maxSession = maxSession;
    }

    public Integer getMaxClient() {
        return maxClient;
    }

    public void setMaxClient(Integer maxClient) {
        this.maxClient = maxClient;
    }

    public Timestamp getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Timestamp expireTime) {
        this.expireTime = expireTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(Integer groupStatus) {
        this.groupStatus = groupStatus;
    }

    public Integer getGroupUpLimit() {
        return groupUpLimit;
    }

    public void setGroupUpLimit(Integer groupUpLimit) {
        this.groupUpLimit = groupUpLimit;
    }

    public Integer getGroupDownLimit() {
        return groupDownLimit;
    }

    public void setGroupDownLimit(Integer groupDownLimit) {
        this.groupDownLimit = groupDownLimit;
    }

    public Integer getGroupMaxSession() {
        return groupMaxSession;
    }

    public void setGroupMaxSession(Integer groupMaxSession) {
        this.groupMaxSession = groupMaxSession;
    }
}

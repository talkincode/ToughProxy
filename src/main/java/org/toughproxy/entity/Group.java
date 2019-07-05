package org.toughproxy.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class Group {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer status;
    private Integer upLimit;
    private Integer downLimit;
    private Integer maxSession;
    private Integer maxClient;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

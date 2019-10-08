package org.toughproxy.entity;

import java.sql.Timestamp;

public class TsIpaddrStat {

    private Long id;
    /**
     * 本地拨号获取IP
     */
    private String ipaddr;

    /**
     * ip提取次数
     */
    private Integer pickTimes;

    /**
     * 开始时间
     */
    private Timestamp beginTime;
    /**
     * 结束时间
     */
    private Timestamp endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public Integer getPickTimes() {
        return pickTimes;
    }

    public void setPickTimes(Integer pickTimes) {
        this.pickTimes = pickTimes;
    }

    public Timestamp getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}

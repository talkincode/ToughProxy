package org.toughproxy.entity;

import java.io.Serializable;
import java.util.Date;

public class TsPppItem implements Serializable {
    /**
     * 拨号ID
     */
    private Long id;

    /**
     * 服务器名称
     */
    private String poolname;

    /**
     * ppp 名称
     */
    private String name;

    /**
     * 本地拨号获取IP
     */
    private String ipaddr;

    /**
     * 对端地址
     */
    private String peer;

    /**
     * IP时间类型 1 短效IP， 2 长效IP
     */
    private Integer timeType;

    /**
     * 最后拨号时间
     */
    private Date lastDia;

    /**
     * 拨号次数
     */
    private Integer diaTimes;

    private String areaCode;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoolname() {
        return poolname;
    }

    public void setPoolname(String poolname) {
        this.poolname = poolname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }

    public Integer getTimeType() {
        return timeType;
    }

    public void setTimeType(Integer timeType) {
        this.timeType = timeType;
    }

    public Date getLastDia() {
        return lastDia;
    }

    public void setLastDia(Date lastDia) {
        this.lastDia = lastDia;
    }

    public Integer getDiaTimes() {
        return diaTimes;
    }

    public void setDiaTimes(Integer diaTimes) {
        this.diaTimes = diaTimes;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}
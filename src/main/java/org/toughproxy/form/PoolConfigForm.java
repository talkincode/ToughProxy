package org.toughproxy.form;

public class PoolConfigForm {
    private String poolname;
    private String dialupInterval;
    private String ipaddrType;
    private String areaCode;

    public String getPoolname() {
        return poolname;
    }

    public void setPoolname(String poolname) {
        this.poolname = poolname;
    }

    public String getDialupInterval() {
        return dialupInterval;
    }

    public void setDialupInterval(String dialupInterval) {
        this.dialupInterval = dialupInterval;
    }

    public String getIpaddrType() {
        return ipaddrType;
    }

    public void setIpaddrType(String ipaddrType) {
        this.ipaddrType = ipaddrType;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}

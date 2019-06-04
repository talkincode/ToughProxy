package org.toughsocks.entity;

public class SocksSession implements Cloneable{

    private String username;
    private String srcAddr;
    private int srcPort;
    private String dstAddr;
    private int dstPort;
    private long upBytes;
    private long downBytes;
    private String startTime;
    private String endTime;

    public String getKey(){
        return srcAddr + ":"+srcPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public void setSrcAddr(String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public long getUpBytes() {
        return upBytes;
    }

    public void setUpBytes(long upBytes) {
        this.upBytes = upBytes;
    }

    public long getDownBytes() {
        return downBytes;
    }

    public void setDownBytes(long downBytes) {
        this.downBytes = downBytes;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    private static String safestr(Object src){
        if(src==null){
            return "";
        }
        return String.valueOf(src);
    }

    public SocksSession clone() throws CloneNotSupportedException {
        return (SocksSession) super.clone();
    }


    public static String getHeaderString(){
        StringBuilder buff = new StringBuilder();
        buff.append("username").append(",");
        buff.append("srcAddr").append(",");
        buff.append("srcPort").append(",");
        buff.append("dstAddr").append(",");
        buff.append("dstPort").append(",");
        buff.append("upBytes").append(",");
        buff.append("downBytes").append(",");
        buff.append("startTime").append(",");
        buff.append("endTime");
        return buff.toString();
    }

    public String toString(){
        StringBuilder buff = new StringBuilder();
        buff.append(username).append(",");
        buff.append(srcAddr).append(",");
        buff.append(safestr(srcPort)).append(",");
        buff.append(dstAddr).append(",");
        buff.append(safestr(dstPort)).append(",");
        buff.append(safestr(upBytes)).append(",");
        buff.append(safestr(downBytes)).append(",");
        buff.append(startTime).append(",");
        buff.append(endTime);
        return buff.toString();
    }


    public static SocksSession fromString(String line) {
        try{
            String [] strs = line.trim().split(",");
            if(strs.length!=9){
                return null;
            }
            SocksSession log = new SocksSession();
            log.setUsername(strs[0]);
            log.setSrcAddr(strs[1]);
            log.setSrcPort(Integer.valueOf(strs[2]));
            log.setDstAddr(strs[3]);
            log.setDstPort(Integer.valueOf(strs[4]));
            log.setUpBytes(Long.valueOf(strs[5]));
            log.setDownBytes(Long.valueOf(strs[6]));
            log.setStartTime(strs[7]);
            log.setEndTime(strs[8]);
            return log;
        } catch(Exception e){
            return null;
        }
    }

}

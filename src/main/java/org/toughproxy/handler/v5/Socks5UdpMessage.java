package org.toughproxy.handler.v5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.NetUtil;
import org.toughproxy.common.NetUtils;

public class Socks5UdpMessage {

    private int rsv;
    private int frag;
    private int atype;
    private String dstAddr;
    private int dstPort;
    private byte[] data;

    public Socks5UdpMessage() {
    }

    public Socks5UdpMessage(ByteBuf buff) {
        setRsv(buff.readShort());
        setFrag(buff.readByte());
        setAtype(buff.readByte());
        if(getAtype()==1){
            byte[] dstAddr = new byte[4];
            buff.readBytes(dstAddr);
            setDstAddr(NetUtil.bytesToIpAddress(dstAddr));
        }else if(getAtype()==4){
            byte[] dstAddr = new byte[16];
            buff.readBytes(dstAddr);
            setDstAddr(NetUtil.bytesToIpAddress(dstAddr));
        }
        setDstPort(buff.readShort());
        buff.discardReadBytes();
        byte[] data = new byte[buff.readableBytes()];
        buff.readBytes(data);
        setData(data);
    }

    public ByteBuf encode(){
        int len = 4;
        if(getAtype()==1){
            len += 4;
        }else if(getAtype()==4){
            len += 16;
        }
        len += (2+data.length);
        ByteBuf buff = Unpooled.buffer(len);
        buff.writeShort((short)rsv);
        buff.writeByte((byte)frag);
        buff.writeByte((byte)atype);
        if(getAtype()==1){
            buff.writeBytes(NetUtils.encodeIpV4(dstAddr));
        } else if(getAtype()==4){
            buff.writeBytes(NetUtils.encodeIpV6(dstAddr));
        }
        buff.writeShort((short)dstPort);
        buff.writeBytes(data);
        return buff;
    }

    public int getRsv() {
        return rsv;
    }

    public void setRsv(int rsv) {
        this.rsv = rsv;
    }

    public int getFrag() {
        return frag;
    }

    public void setFrag(int frag) {
        this.frag = frag;
    }

    public int getAtype() {
        return atype;
    }

    public void setAtype(int atype) {
        this.atype = atype;
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

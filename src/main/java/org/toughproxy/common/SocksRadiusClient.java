package org.toughproxy.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusEndpoint;
import org.tinyradius.util.RadiusException;
import org.toughproxy.config.Constant;
import org.toughproxy.entity.SocksAuthResp;

import java.io.IOException;

public class SocksRadiusClient extends RadiusClient implements Constant {

    private Log logger = LogFactory.getLog(SocksRadiusClient.class);

    private int port;

    public SocksRadiusClient(String hostName, String sharedSecret) {
        super(hostName, sharedSecret);
    }

    public SocksRadiusClient(String hostName, int port, String sharedSecret) {
        super(hostName, sharedSecret);
        this.port = port;
    }

    public SocksRadiusClient(RadiusEndpoint client) {
        super(client);
    }

    public SocksAuthResp doAuth(String nasid, String username, String password) throws Exception {
        try {
            AccessRequest request = new AccessRequest();
            request.setUserName(username);
            request.setUserPassword(password);
            request.setAuthProtocol(AccessRequest.AUTH_CHAP);
            request.addAttribute("Service-Type","Framed-User");
            request.addAttribute("Framed-Protocol","PPP");
            request.addAttribute("NAS-IP-Address",SystemUtil.getLocalAddress());
            request.addAttribute("Calling-Station-Id","00:00:00:00:00:00");
            request.addAttribute("NAS-Identifier",nasid);
            request.addAttribute("NAS-Port-Id","toughproxy");
            RadiusPacket resp = this.communicate(request,port);
            SocksAuthResp sresp = new SocksAuthResp();
            sresp.setCode(resp.getPacketType() == RadiusPacket.ACCESS_ACCEPT?0:1);

            try{
                sresp.setUpLimit(resp.getVendorAttribute(RADIUS_VENDOR,RADIUS_UP_LIMIT_ATTR_TYPE).getIntValue());
            }catch (Exception e){
                logger.error("RADIUS 认证请求读取上行限速失败");
            }

            try{
                sresp.setDownLimit(resp.getVendorAttribute(RADIUS_VENDOR,RADIUS_DOWN_LIMIT_ATTR_TYPE).getIntValue());
            }catch (Exception e){
                logger.error("RADIUS 认证请求读取下行限速失败");
            }

            try{
                sresp.setMaxSession(resp.getVendorAttribute(RADIUS_VENDOR,RADIUS_MAX_SESSION_ATTR_TYPE).getIntValue());
            }catch (Exception e){
                logger.error("RADIUS 认证请求读取用户并发数失败");
            }

            try{
                sresp.setMessage(resp.getAttribute(18).getStringValue());
            }catch (Exception ignore){
            }

            return sresp;
        } catch (IOException | RadiusException e) {
            logger.error("RADIUS 认证请求失败",e);
            throw e;
        }
    }


}

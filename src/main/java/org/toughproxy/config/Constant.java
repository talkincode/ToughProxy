package org.toughproxy.config;

public interface Constant {

    public static final String SESSION_USER_KEY = "SESSION_USER_KEY";

    public final static String SYSTEM_MODULE = "system";
    public final static String SYSTEM_USERNAME = "systemUsername";
    public final static String SYSTEM_USERPWD = "systemUserpwd";
    public final static String SYSTEM_TICKET_HISTORY_DAYS = "systemTicketHistoryDays";
    public final static String SYSTEM_SOCKS_USER_AUTH_MODE = "systemSocksUserAuthMode";
    public final static String SYSTEM_SOCKS_RADIUS_AUTH_SERVER = "systemSocksRadiusAuthServer";
    public final static String SYSTEM_SOCKS_RADIUS_AUTH_PORT = "systemSocksRadiusAuthPort";
    public final static String SYSTEM_SOCKS_RADIUS_AUTH_SECRET = "systemSocksRadiusAuthSecret";
    public final static String SYSTEM_SOCKS_RADIUS_NASID = "systemSocksRadiusNasid";

    public final static String SOCKS_AUTH_FREE_MODE = "free";
    public final static String SOCKS_AUTH_LOCAL_MODE = "local";
    public final static String SOCKS_AUTH_RADIUS_MODE = "radius";

    public final static String API_MODULE = "api";
    public final static String API_TYPE = "apiType";
    public final static String API_USERNAME = "apiUsername";
    public final static String API_PASSWD = "apiPasswd";
    public final static String API_ALLOW_IPLIST = "apiAllowIplist";
    public final static String API_BLACK_IPLIST = "apiBlackIplist";

    public final static String SMS_MODULE = "sms";
    public final static String SMS_GATEWAY = "smsGateway";
    public final static String SMS_APPID = "smsAppid";
    public final static String SMS_APPKEY = "smsAppkey";
    public final static String SMS_VCODE_TEMPLATE = "smsVcodeTemplate";

    public final static String POOL_MODULE = "pool";
    public final static String POOL_NAME_FILE = "/var/toughproxy/poolname";
    public final static String POOL_IPADDR_TYPE_FILE = "/var/toughproxy/ipaddrType";
    public final static String POOL_DIAUP_INTERVAL_FILE = "/var/toughproxy/dialupInterval";
    public final static String POOL_AREA_CODE_FILE = "/var/toughproxy/areaCode";


    public final static int RADIUS_VENDOR = 18168;
    public final static int RADIUS_UP_LIMIT_ATTR_TYPE = 1;
    public final static int RADIUS_DOWN_LIMIT_ATTR_TYPE = 2;
    public final static int RADIUS_MAX_SESSION_ATTR_TYPE = 3;
    public final static int RADIUS_MAX_CLIENT_ATTR_TYPE = 4;

    public final static String ACL_ACCEPT_DESC = "accept";
    public final static String ACL_REJECT_DESC = "reject";


}



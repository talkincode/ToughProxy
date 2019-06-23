package org.toughproxy.form;

public class SystemConfigForm {

    private String systemTicketHistoryDays;
    private String systemSocksRadiusNasid;
    private String systemSocksUserAuthMode;
    private String systemSocksRadiusAuthServer;
    private String systemSocksRadiusAuthPort;
    private String systemSocksRadiusAuthSecret;

    public String getSystemTicketHistoryDays() {
        return systemTicketHistoryDays;
    }

    public void setSystemTicketHistoryDays(String systemTicketHistoryDays) {
        this.systemTicketHistoryDays = systemTicketHistoryDays;
    }

    public String getSystemSocksRadiusNasid() {
        return systemSocksRadiusNasid;
    }

    public void setSystemSocksRadiusNasid(String systemSocksRadiusNasid) {
        this.systemSocksRadiusNasid = systemSocksRadiusNasid;
    }

    public String getSystemSocksUserAuthMode() {
        return systemSocksUserAuthMode;
    }

    public void setSystemSocksUserAuthMode(String systemSocksUserAuthMode) {
        this.systemSocksUserAuthMode = systemSocksUserAuthMode;
    }

    public String getSystemSocksRadiusAuthServer() {
        return systemSocksRadiusAuthServer;
    }

    public void setSystemSocksRadiusAuthServer(String systemSocksRadiusAuthServer) {
        this.systemSocksRadiusAuthServer = systemSocksRadiusAuthServer;
    }

    public String getSystemSocksRadiusAuthPort() {
        return systemSocksRadiusAuthPort;
    }

    public void setSystemSocksRadiusAuthPort(String systemSocksRadiusAuthPort) {
        this.systemSocksRadiusAuthPort = systemSocksRadiusAuthPort;
    }

    public String getSystemSocksRadiusAuthSecret() {
        return systemSocksRadiusAuthSecret;
    }

    public void setSystemSocksRadiusAuthSecret(String systemSocksRadiusAuthSecret) {
        this.systemSocksRadiusAuthSecret = systemSocksRadiusAuthSecret;
    }
}

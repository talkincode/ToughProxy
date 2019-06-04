package org.toughsocks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.toughsocks.common.CoderUtil;
import org.toughsocks.common.DateTimeUtil;
import org.toughsocks.common.RestResult;
import org.toughsocks.common.ValidateUtil;
import org.toughsocks.component.ConfigService;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.config.ApplicationConfig;
import org.toughsocks.config.Constant;
import org.toughsocks.entity.Config;
import org.toughsocks.entity.MenuItem;
import org.toughsocks.entity.SessionUser;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController implements Constant {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    private ApplicationConfig appconfig;

    @Autowired
    private ConfigService configService;


    /**
     * 构造界面菜单数据
     */
    public static List<MenuItem> getMenuData() {
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        MenuItem dashboardItem = new MenuItem("dashboard", "dashboard", "控制面板");
        MenuItem cfgItem = new MenuItem("config", "cogs", "系统设置");
        MenuItem userItem = new MenuItem("user", "users", "用户管理");
        MenuItem sessionItem = new MenuItem("session", "user-circle", "连接查询");
        MenuItem ticketItem = new MenuItem("ticket", "table", "网络日志");
        MenuItem syslogItem = new MenuItem("syslog", "hdd-o", "系统日志");
        menuItems.add(dashboardItem);
        menuItems.add(cfgItem);
        menuItems.add(userItem);
        menuItems.add(sessionItem);
        menuItems.add(ticketItem);
        menuItems.add(syslogItem);
        return menuItems;
    }

    @GetMapping(value = {"/admin/login"})
    public String loginPage(){
        return "/static/login.html";
    }

    @GetMapping(value = {"/admin","/"})
    public String indexPage(){
        return "/static/index.html";
    }

    @GetMapping(value = "/admin/session")
    @ResponseBody
    public RestResult sessionHandeler(HttpSession session, HttpServletRequest request){
        SessionUser user = (SessionUser) session.getAttribute(SESSION_USER_KEY);
        if(user==null){
            return new RestResult(1, "用户未登录或登录已经过期");
        }
        RestResult result = new RestResult(0,"ok");
        Map<String, Object> child = new HashMap<>();
        String localAddr = request.getLocalAddr();
        child.put("menudata", getMenuData());
        child.put("username", user.getUsername());
        child.put("lastLogin", user.getLastLogin());
        child.put("level", "super");
        child.put("system_name","ToughSOCKS");
        child.put("version", appconfig.getVersion());
        child.put("ipaddr", localAddr);
        result.setData(child);
        return  result;
    }



    @PostMapping("/admin/login")
    @ResponseBody
    public RestResult loginHandler(String username, String password, HttpSession session) {
        try {
            String sysUserName = configService.getStringValue(SYSTEM_MODULE,SYSTEM_USERNAME);
            String sysUserPwd = configService.getStringValue(SYSTEM_MODULE,SYSTEM_USERPWD);
            if(ValidateUtil.isEmpty(sysUserName)){
                sysUserName = "admin";
                configService.updateConfig(new Config(SYSTEM_MODULE,SYSTEM_USERNAME,sysUserName,""));
            }
            if(ValidateUtil.isEmpty(sysUserPwd)){
                sysUserPwd = CoderUtil.md5Salt("root");
                configService.updateConfig(new Config(SYSTEM_MODULE,SYSTEM_USERPWD,sysUserPwd,""));
            }

            if(username.equals(sysUserName) && CoderUtil.md5Salt(password).equals(sysUserPwd)){
                SessionUser suser = new SessionUser(sysUserName);
                suser.setLastLogin(DateTimeUtil.getDateTimeString());
                session.setAttribute(SESSION_USER_KEY, suser);
                return RestResult.SUCCESS;
            }else{
                return  new RestResult(1,"用户名密码错误");
            }
        } catch (Exception e) {
            logger.error("登录失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"login failure");
        }
    }


    @GetMapping("/admin/logout")
    public String LogoutHandler(HttpSession session) {
        session.invalidate();
        return "/static/login.html";
    }


}

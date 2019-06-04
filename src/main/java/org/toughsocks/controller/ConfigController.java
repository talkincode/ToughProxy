package org.toughsocks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.toughsocks.common.RestResult;
import org.toughsocks.component.ConfigService;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.config.Constant;
import org.toughsocks.entity.Config;
import org.toughsocks.form.ApiConfigForm;
import org.toughsocks.form.SmsConfigForm;
import org.toughsocks.form.SystemConfigForm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ConfigController implements Constant {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    private ConfigService configService;

    @GetMapping(value = {"/api/v6/config/load/{module}","/admin/config/load/{module}"})
    @ResponseBody
    public Map loadRadiusConfig(@PathVariable(name = "module") String module){
        Map result = new HashMap();
        try{
            List<Config> cfgs = configService.queryForList(module);
            for (Config cfg : cfgs){
                result.put(cfg.getName(),cfg.getValue());
            }
        }catch(Exception e){
            logger.error("query config error",e, Memarylogger.SYSTEM);
        }
        return result;
    }

    /**
     * RADIUS 配置更新
     * @param form
     * @return
     */
    @PostMapping(value = {"/api/system/update","/admin/config/system/update"})
    @ResponseBody
    public RestResult updateRadiusConfig(SystemConfigForm form){
        try{
            configService.updateConfig(new Config(SYSTEM_MODULE,SYSTEM_TICKET_HISTORY_DAYS,form.getSystemTicketHistoryDays()));
        }catch(Exception e){
            logger.error("update config error",e, Memarylogger.SYSTEM);
        }
        return new RestResult(0,"update system config done");
    }

    /**
     * 短信配置更新呢
     * @param form
     * @return
     */
    @PostMapping(value = {"/api/sms/update","/admin/config/sms/update"})
    @ResponseBody
    public RestResult updateSmsConfig(SmsConfigForm form){
        try{
            configService.updateConfig(new Config(SMS_MODULE,SMS_GATEWAY,form.getSmsGateway()));
            configService.updateConfig(new Config(SMS_MODULE,SMS_APPID,form.getSmsAppid()));
            configService.updateConfig(new Config(SMS_MODULE,SMS_APPKEY,form.getSmsAppkey()));
            configService.updateConfig(new Config(SMS_MODULE,SMS_VCODE_TEMPLATE,form.getSmsVcodeTemplate()));
        }catch(Exception e){
            logger.error("update config error",e, Memarylogger.SYSTEM);
        }
        return new RestResult(0,"update sms config done");
    }

    /**
     * API 配置更新呢
     * @param form
     * @return
     */
    @PostMapping(value = {"/admin/config/api/update"})
    @ResponseBody
    public RestResult updateApiConfig(ApiConfigForm form){
        try{
            configService.updateConfig(new Config(API_MODULE,API_TYPE,form.getApiType()));
            configService.updateConfig(new Config(API_MODULE,API_USERNAME,form.getApiUsername()));
            configService.updateConfig(new Config(API_MODULE,API_PASSWD,form.getApiPasswd()));
            configService.updateConfig(new Config(API_MODULE,API_ALLOW_IPLIST,form.getApiAllowIplist()));
            configService.updateConfig(new Config(API_MODULE,API_BLACK_IPLIST,form.getApiBlackIplist()));
        }catch(Exception e){
            logger.error("update config error",e, Memarylogger.SYSTEM);
        }
        return new RestResult(0,"update api config done");
    }



}


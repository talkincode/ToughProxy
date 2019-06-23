package org.toughproxy.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.toughproxy.component.SocksStat;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.RestResult;
import org.toughproxy.common.SystemUtil;
import org.toughproxy.component.TrafficStat;

import java.util.Map;

/**
 * 控制面板
 */
@Controller
public class DashboardController {

    @Autowired
    private SocksStat socks5Stat;
    @Autowired
    private TrafficStat trafficStat;

    @GetMapping({"/api/socks/stat","/admin/socks/stat"})
    @ResponseBody
    public Map querySocksStat(){
        return socks5Stat.getData();
    }

    @GetMapping({"/api/traffic/stat","/admin/traffic/stat"})
    @ResponseBody
    public Map queryTrafficStat(){
        return trafficStat.getData();
    }


    @GetMapping({"/api/cpuuse","/admin/dashboard/cpuuse"})
    @ResponseBody
    public RestResult cpuuse(){
        return new RestResult(0,"ok", SystemUtil.getCpuUsage());
    }

    @GetMapping(value = {"/api/memuse","/admin/dashboard/memuse"})
    @ResponseBody
    public RestResult memuse(){
        return new RestResult(0,"ok", SystemUtil.getMemUsage());
    }

    @GetMapping({"/api/diskuse","/admin/dashboard/diskuse"})
    @ResponseBody
    public RestResult diskuse(){
        try {
            return new RestResult(0,"ok", SystemUtil.getDiskUsage());
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResult(0,"ok", 0);
        }
    }

    @GetMapping({"/admin/dashboard/uptime"})
    @ResponseBody
    public String uptime(){
        return String.format("<i class='fa fa-bar-chart'></i> 应用系统运行时长 %s ", DateTimeUtil.formatSecond(SystemUtil.getUptime()/1000));
    }
}

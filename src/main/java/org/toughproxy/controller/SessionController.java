package org.toughproxy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.toughproxy.common.PageResult;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.component.SessionCache;
import org.toughproxy.entity.SocksSession;

import java.util.ArrayList;


@RestController
public class SessionController {

    @Autowired
    private SessionCache sessionCache;
    @Autowired
    private Memarylogger logger;

    @GetMapping({"/api/sessiion/query","/admin/session/query"})
    public PageResult<SocksSession> queryTicket(@RequestParam(defaultValue = "0") int start,
                                                @RequestParam(defaultValue = "40") int count,
                                                String username, String srcAddr, Integer srcPort,
                                                String dstAddr, Integer dstPort, String startTime, String endtime){

        try {
            return sessionCache.querySessionPage(start,count,username,srcAddr,srcPort,dstAddr,dstPort,null);
        } catch (Exception e) {
            logger.error(String.format("/admin 查询连接发生错误, %s", e.getMessage()),e, Memarylogger.SYSTEM);
            return new PageResult<>(start,0, new ArrayList<>());
        }
    }


}


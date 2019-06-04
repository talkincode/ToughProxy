package org.toughsocks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.toughsocks.common.PageResult;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.entity.TraceMessage;


@RestController
public class SyslogController {

    @Autowired
    private Memarylogger logger;

    @GetMapping({"/api/syslog/query","/admin/syslog/query"})
    public PageResult<TraceMessage> queryTraceMessage(@RequestParam(defaultValue = "0") int start,
                                                      @RequestParam(defaultValue = "40") int count,
                                                      String startDate,
                                                      String endDate,
                                                      String type,
                                                      String username,
                                                      String keyword){
        return logger.queryMessage(start,count,startDate,endDate,type, username,keyword);
    }

}

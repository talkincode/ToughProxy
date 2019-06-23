package org.toughproxy.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.toughproxy.common.RestResult;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;


@RestController
public class TestController {

    @GetMapping("/socktest")
    @ResponseBody
    public RestResult sessionHandeler(HttpServletRequest request) throws IOException {
        System.out.println("\n-----------------------------------------------------------");
        System.out.println("remote ip:"+request.getRemoteAddr());
        System.out.println("remote user:"+request.getRemoteUser());
        Enumeration<String> heaaders = request.getHeaderNames();
        while(heaaders.hasMoreElements()){
            String name = heaaders.nextElement();;
            System.out.println(name+":"+request.getHeader(name));
        }
        System.out.println("-----------------------------------------------------------\n");
        return RestResult.SUCCESS;
    }


}

package com.wsf.netty.rpc.consumer.controller;

import com.wsf.netty.rpc.consumer.client.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wsf
 * @since 20220526
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private TestService testService;

    @ResponseBody
    @RequestMapping("/hello")
    private String hello() {
        return testService.hello();
    }

}

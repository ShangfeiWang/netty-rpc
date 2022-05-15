package com.wsf.netty.rpc.provider.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wsf
 * @since 20220526
 */
@RestController
@RequestMapping("/test")
public class HealthController {

    @ResponseBody
    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

}

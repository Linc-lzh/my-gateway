package com.gateway.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GatewayController {

    @RequestMapping(value = "/{command}", method = {RequestMethod.POST, RequestMethod.GET})
    public String transferCommand(@PathVariable String command, HttpServletRequest request) {
        //String retStr = dealCommand.executeCommand(command, request);
        return "Hello world";
    }
}
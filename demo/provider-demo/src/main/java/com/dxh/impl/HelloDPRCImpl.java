package com.dxh.impl;

import com.dxh.HelloDRPC;
import com.dxh.annotation.DrpcApi;

@DrpcApi(group = "primary")
public class HelloDPRCImpl implements HelloDRPC {
    @Override
    public String sayHello(String msg) {
        return "hi, consumer:" + msg;
    }
}

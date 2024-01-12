package com.dxh.impl;

import com.dxh.HelloDRPC;
import com.dxh.HelloDRPC2;
import com.dxh.annotation.DrpcApi;

@DrpcApi
public class HelloDPRCImpl2 implements HelloDRPC2 {
    @Override
    public String sayHello(String msg) {
        return "hi, consumer:" + msg;
    }
}

package com.dxh.impl;

import com.dxh.HelloDRPC;

public class HelloDPRCImpl implements HelloDRPC {
    @Override
    public String sayHello(String msg) {
        return "hi, consumer:" + msg;
    }
}

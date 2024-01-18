package com.dxh;

import com.dxh.annotation.DrpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @DrpcService
    private HelloDRPC helloDRPC;

    @GetMapping("hello")
    public String hello(){
        return helloDRPC.sayHello("provider");
    }

}

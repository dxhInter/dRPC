package com.dxh;

import com.dxh.annotation.TryTimes;

public interface HelloDRPC {
    /**
     *
     * @param msg
     * @return
     */
    @TryTimes(tryTimes = 3,intervalTime = 3000)
    String sayHello(String msg);
}

package com.dxh;

import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

/**
 * 使用雪花算法生成请求id
 */
public class IdGenerator {
//    private static LongAdder id = new LongAdder();
//
//    public static Long getId(){
//        id.increment();
//        return id.sum();
//    }

    //雪花算法
    // 包括机房号（5bit） 32
    // 机器号（5bit）32
    // 时间戳（long）42bit，重新选择一个起始时间
    // 序列号 12 bit 5+5+12+42 = 64

    //起始时间戳
    private static final long START_STAMP = DateUtils.get("2022-01-01").getTime();
    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

    //最大值31 -1左移五位
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    //时间戳 + 机房号 + 机器号 + 序列号
    //时间戳要左移22位
    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();

    //上一次的时间戳
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        //检查机房号和机器号是否超过最大值
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX){
            throw new IllegalArgumentException("dataCenterId can't be greater than DATA_CENTER_MAX and machineId can't be greater than MACHINE_MAX");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId(){
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;
        //如果当前时间小于上一次的时间戳，说明系统时钟回退了，抛出异常
        if (timeStamp < lastTimeStamp){
            throw new RuntimeException("clock is moving backwards. Rejecting requests until " + lastTimeStamp);
        }

        //如果当前时间等于上一次的时间戳, sequenceId需要自增
        if (timeStamp == lastTimeStamp) {
            //sequenceId ++ 会有线程不安全问题
            sequenceId.increment();
            //如果sequenceId超过了最大值，需要获取下一个时间戳
            if (sequenceId.sum() >= SEQUENCE_MAX){
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        }else{
            sequenceId.reset();
        }
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT | machineId << MACHINE_LEFT | sequence;

    }

    private long getNextTimeStamp() {
        //获取当前时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        while (current == lastTimeStamp){
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1, 2);
        for (int i = 0; i < 1000; i++) {
            new Thread(()->{
                System.out.println(idGenerator.getId());
            }).start();
        }
    }

}


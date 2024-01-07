package com.dxh.channelHandler.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.ServiceConfig;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<DrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DrpcRequest drpcRequest) throws Exception {
        // 1. 获取负载内容
        RequestPayload requestPayload = drpcRequest.getPayload();
        // 2. 根据负载内容，找到对应的服务进行调用
        Object object = callTargetMethod(requestPayload);
        // 3. 封装响应

        // 4. 写回响应
        channelHandlerContext.channel().writeAndFlush(object);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parameterTypes = requestPayload.getParameterTypes();
        Object[] parameterValue = requestPayload.getParameterValue();

        // 从注册中心中暴露服务寻找实现
        ServiceConfig<?> serviceConfig = DrpcBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        // 通过反射调用方法 1. 方法名 2. 参数类型 3. 参数值
        Object returnValue;
        try {
            Class<?> refImplClass = refImpl.getClass();
            Method method = refImplClass.getMethod(methodName, parameterTypes);
            returnValue = method.invoke(refImpl, parameterValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("call interface [{}] method [{}] error",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}

package com.dxh.channelhandler.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.ServiceConfig;
import com.dxh.enumeration.RequestType;
import com.dxh.enumeration.ResponseCode;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.DrpcResponse;
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
        Object result = null;
        if (!(drpcRequest.getRequestType() == RequestType.HEARTBEAT.getId())){
            result = callTargetMethod(requestPayload);
            if (log.isDebugEnabled()){
                log.debug("request [{}] is already called,call method [{}] return value [{}]",drpcRequest.getRequestId(),requestPayload.getMethodName(),result);
            }
        }
        // 3. 封装响应
        DrpcResponse drpcResponse = new DrpcResponse();
        drpcResponse.setCode(ResponseCode.SUCCESS.getCode());
        drpcResponse.setRequestId(drpcRequest.getRequestId());
        drpcResponse.setCompressType(drpcRequest.getCompressType());
        drpcResponse.setSerializerType(drpcRequest.getSerializerType());
        drpcResponse.setBody(result);

        // 4. 写回响应
        channelHandlerContext.channel().writeAndFlush(drpcResponse);
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

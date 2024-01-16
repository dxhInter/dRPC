package com.dxh.config;

import com.dxh.comperss.Compressor;
import com.dxh.comperss.CompressorFactory;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.serialize.Serializer;
import com.dxh.serialize.SerializerFactory;
import com.dxh.spi.SpiHandler;

import java.util.List;

public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        //将其放入工厂
        if(loadBalancerWrappers != null && loadBalancerWrappers.size() > 0){
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> objectWrapperList = SpiHandler.getList(Compressor.class);
        if (objectWrapperList != null){
            objectWrapperList.forEach(objectWrapper -> {
                CompressorFactory.addCompressor(objectWrapper);
            });
        }

        List<ObjectWrapper<Serializer>> serializerList = SpiHandler.getList(Serializer.class);
        if (serializerList != null){
            serializerList.forEach(objectWrapper -> {
                SerializerFactory.addSerializer(objectWrapper);
            });
        }
    }
}

package com.dxh.config;

import com.dxh.enumeration.comperss.Compressor;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.serialize.Serializer;
import com.dxh.spi.SpiHandler;

public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        if (loadBalancer != null) {
            configuration.setLoadBalancer(loadBalancer);
        }

        Compressor compressor = SpiHandler.get(Compressor.class);
        if (compressor != null){
            configuration.setCompressor(compressor);
        }

        Serializer serializer = SpiHandler.get(Serializer.class);
        if (serializer != null){
            configuration.setSerializer(serializer);
        }
    }
}

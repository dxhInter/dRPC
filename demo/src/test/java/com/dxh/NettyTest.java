package com.dxh;

import io.netty.buffer.*;
import org.junit.Test;

public class NettyTest {

    @Test
    public void testCompositeByteBuf(){
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        CompositeByteBuf messageBuf = Unpooled.compositeBuffer();
        messageBuf.addComponents(header, body);
    }

}

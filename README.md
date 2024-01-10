This a ROC framework wrote by me
* Implementation of a simple RPC framework, including basic mechanisms such as serialization, deserialization, and network communication.
* Implementation of load balancing, including policies such as polling, minimum number of active calls, etc.
* Implementation of automatic restart and failover, including mechanisms such as network outage detection, service recovery, etc.
1、服务调用方
* 发送报文 writeAndFlush(object) 请求
* 此object应该是什么?应该包含一些什么样的信息?
* YrpcRequest
* 1、请求id
* 2、压缩类型 (1byte)
* 3、序列化的方式(1byte)
* 4、消息类型(普通请求,心跳检测请求)
* 5、负载 payload(接口的名字,方法的名字,参数列表,返回值类型))
* pipeline就生效了,报文开始出站
* - 第一个处理器 log
* - 第二个处理器(编码器out)(转化 object->msg(请求报文), 序列化, 压缩)

2、服务提供方
* - 第一个处理器 in/out log
* - 第二个处理器(解码器in)(解压缩, 反序列化, msg->object(请求报文))

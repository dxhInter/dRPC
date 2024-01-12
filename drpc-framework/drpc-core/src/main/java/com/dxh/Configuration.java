package com.dxh;

import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.loadbalancer.impl.RoundRobinLoadBalancer;
import com.dxh.serialize.Serializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局的配置信息, 代码配置-->xml配置-->spi配置-->默认配置
 */
@Data
@Slf4j
public class Configuration {
    //配置信息 -> 服务端口号
    private int port = 8083;
    //配置信息 -> 应用名称
    private String applicationName = "default";
    //配置信息 -> 注册中心
    private RegistryConfig registryConfig;
    //配置信息 -> 序列化协议
    private ProtocolConfig protocolConfig;
    //配置信息 -> 序列化类型
    private String serializeType = "jdk";
    //配置信息 -> 压缩类型
    private String compressType = "gzip";
    //配置信息 -> ID生成器
    private final IdGenerator idGenerator = new IdGenerator(1,2);
    //配置信息 -> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();


    // 读xml
    public Configuration() {
        //通过xml配置文件读取配置信息
        loadFromXml(this);
    }

    /**
     * 从xml配置文件中读取配置信息 dom4j
     * @param configuration
     */
    private void loadFromXml(Configuration configuration) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);

            // 2、获取一个xpath解析器
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            String expression = "/configuration/serializer";
            //解析表达式
            Serializer serializer = parseObject(doc, xpath, expression, null);
        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            log.info("load configuration from xml failed",e);
        }
    }

    private <T> T parseObject(Document doc, XPath xpath,String expression, Class<?>[] paramType, Object... param) throws XPathExpressionException {
        try{
            XPathExpression expr = xpath.compile(expression);
            //获取节点
            Node targetName = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetName.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null){
                instance = aClass.getConstructor().newInstance();
            }else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        }catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            log.error("parse object failed",e);
        }
        return null;
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }
}

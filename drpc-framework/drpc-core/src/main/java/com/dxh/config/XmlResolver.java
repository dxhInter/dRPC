package com.dxh.config;

import com.dxh.IdGenerator;
import com.dxh.ProtocolConfig;
import com.dxh.RegistryConfig;
import com.dxh.comperss.Compressor;
import com.dxh.comperss.CompressorFactory;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.serialize.Serializer;
import com.dxh.serialize.SerializerFactory;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 全局的配置信息, 代码配置-->xml配置-->spi配置-->默认配置
 * 读取xml配置文件
 */
@Slf4j
public class XmlResolver {
    /**
     * 从xml配置文件中读取配置信息
     * @param configuration
     */
    public void loadFromXml(Configuration configuration) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            // 禁用外部实体解析：可以通过调用setFeature(String name, boolean value)方法并将“http://apache.org/xml/features/nonvalidating/load-external-dtd”设置为“false”来禁用外部实体解析。
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);

            // 2、获取一个xpath解析器
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setApplicationName(resolveAppName(doc, xpath));

            configuration.setIdGenerator(resolveIdGenerator(doc, xpath));

            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));
            configuration.setSerializeType(resolveSerializeType(doc, xpath));
            configuration.setCompressType(resolveCompressType(doc, xpath));
            ObjectWrapper<Compressor> compressorObjectWrapper = resolveCompressor(doc, xpath);
            CompressorFactory.addCompressor(compressorObjectWrapper);

            ObjectWrapper<Serializer> serializerObjectWrapper = resolveSerializer(doc, xpath);
            SerializerFactory.addSerializer(serializerObjectWrapper);

            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));


        } catch (ParserConfigurationException | IOException | SAXException e) {
            log.info("load configuration from xml failed",e);
        }
    }

    private ObjectWrapper<Serializer> resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/serializer";
        Serializer serializer = parseObject(doc, xpath, expression, null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
        String name = parseString(doc, xpath, expression, "name");
        return new ObjectWrapper<>(code, name, serializer);
    }


    /**
     * 解析压缩器
     * @param doc
     * @param xpath
     * @return
     */
    private ObjectWrapper<Compressor> resolveCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/compressor";
        Compressor compressor = parseObject(doc, xpath, expression, null);
        Byte code = Byte.valueOf(Objects.requireNonNull(parseString(doc, xpath, expression, "code")));
        String name = parseString(doc, xpath, expression, "name");
        return new ObjectWrapper<>(code, name, compressor);
    }

    /**
     * 解析端口号
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xpath) {
        String expression = "/configuration/port";
        String portString = parseString(doc, xpath, expression);
        return Integer.parseInt(portString);
    }

    /**
     * 解析应用名称
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 应用名
     */
    private String resolveAppName(Document doc, XPath xpath) {
        String expression = "/configuration/appName";
        return parseString(doc, xpath, expression);
    }

    /**
     * 解析负载均衡器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 负载均衡器实例
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析id发号器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return id发号器实例
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xpath, expression, "class");
        String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
        String machineId = parseString(doc, xpath, expression, "MachineId");

        try {
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析注册中心
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return RegistryConfig
     */
    private RegistryConfig resolveRegistryConfig(Document doc, XPath xpath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xpath, expression, "url");
        return new RegistryConfig(url);
    }



    /**
     * 解析压缩的算法名称
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 压缩算法名称
     */
    private String resolveCompressType(Document doc, XPath xpath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 解析序列化的方式
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化的方式
     */
    private String resolveSerializeType(Document doc, XPath xpath) {
        String expression = "/configuration/serializeType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 获取一个节点的文本值 例如：<port>8083</port>，获取8083
     * @param doc
     * @param xpath
     * @param expression
     * @return
     */
    private String parseString(Document doc, XPath xpath,String expression){
        try{
            XPathExpression expr = xpath.compile(expression);
            //获取节点
            Node targetName = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetName.getTextContent();
        }catch (XPathExpressionException e) {
            log.error("parse string failed",e);
        }
        return null;
    }

    /**
     * 获得一个节点属性的值，返回字符串
     * @param doc xml文档
     * @param xpath xpath解析器
     * @param expression xpath表达式
     * @param attributeName 节点名称
     * @return
     */
    private String parseString(Document doc, XPath xpath,String expression, String attributeName){
        try{
            XPathExpression expr = xpath.compile(expression);
            //获取节点
            Node targetName = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetName.getAttributes().getNamedItem(attributeName).getNodeValue();
        }catch (XPathExpressionException e) {
            log.error("parse string failed",e);
        }
        return null;
    }

    /**
     * 解析一个节点，返回一个实例
     * @param doc xml文档
     * @param xpath xpath解析器
     * @param expression xpath表达式
     * @param paramType 构造函数的参数列表
     * @param param 构造函数的参数
     * @return 配置的实例
     * @param <T>
     */

    private <T> T parseObject(Document doc, XPath xpath,String expression, Class<?>[] paramType, Object... param){
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
                InvocationTargetException | XPathExpressionException e) {
            log.error("parse object failed",e);
        }
        return null;
    }
}

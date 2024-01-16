package com.dxh.spi;

import com.dxh.config.ObjectWrapper;
import com.dxh.exceptions.SpiException;
import com.dxh.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实现spi机制加载配置
 */
@Slf4j
public class SpiHandler {

    //spi文件的基础路径
    public static final String BASE_PATH = "META-INF/drpc-services";
    //缓存spi文件中的内容
    public static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);
    //缓存每一个接口所对应的实现的实例
    public static final Map<Class<?>,List<ObjectWrapper<?>>> SPI_IMPLMENTS = new ConcurrentHashMap<>(32);
    //静态方法块, 在类加载的时候执行
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getPath());
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File child : listFiles) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }

    /**
     * 获取spi文件中的内容
     * @param child
     * @return
     */
    private static List<String> getImplNames(File child) {
        try(
                FileReader fileReader = new FileReader(child);
                //设计模式：装饰器模式
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                ){
            List<String> implNames = new ArrayList<>();
            while (true){
                String name = bufferedReader.readLine();
                if (name == null || "".equals(name)){
                    break;
                }
                implNames.add(name);
            }
            return implNames;
        } catch (IOException e) {
            log.error("read spi file failed",e);
        }
        return null;
    }

    /**
     * 获取一个接口的实例
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> ObjectWrapper<T> get(Class<T> clazz) {
        //优先从缓存中获取
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLMENTS.get(clazz);
        if (objectWrappers != null && !objectWrappers.isEmpty()) {
            return (ObjectWrapper<T>) objectWrappers.get(0);
        }
        //否则需要建立缓存
        buildCache(clazz);
        List<ObjectWrapper<?>> result = SPI_IMPLMENTS.get(clazz);
        if (result == null || result.isEmpty()) {
            return null;
        }
        //再次从缓存中获取
        return (ObjectWrapper<T>)result.get(0);
    }


    /**
     * 获取所有和当前服务相关的的实例
     * @param clazz
     * @return
     * @param <T>
     */
    public synchronized static<T> List<ObjectWrapper<T>> getList(Class<T> clazz) {
        //优先从缓存中获取
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLMENTS.get(clazz);
        if (objectWrappers != null && !objectWrappers.isEmpty()) {
            return objectWrappers.stream().map(e -> (ObjectWrapper<T>)e).collect(Collectors.toList());
        }
        buildCache(clazz);
        objectWrappers = SPI_IMPLMENTS.get(clazz);
        if (objectWrappers != null && !objectWrappers.isEmpty()) {
            return objectWrappers.stream().map(e -> (ObjectWrapper<T>)e).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 建立class相关的缓存
     * @param clazz
     */
    private static void buildCache(Class<?> clazz) {

        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if (implNames == null || implNames.isEmpty()) {
            return;
        }
        //实例化所有的实现
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames) {
            Class<?> aClass = null;
            try {
                String[] split = implName.split("-");
                if(split.length != 3){
                    throw new SpiException("spi file format error");
                }
                Byte code = Byte.valueOf(split[0]);
                String type = split[1];
                String implementName = split[2];
                aClass = Class.forName(implementName);
                Object impl = aClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, impl);
                impls.add(objectWrapper);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.error("get spi instance failed",e,implName);
            }
        }
        SPI_IMPLMENTS.put(clazz,impls);
    }
}

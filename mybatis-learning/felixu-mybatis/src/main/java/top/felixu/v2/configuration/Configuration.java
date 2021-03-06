package top.felixu.v2.configuration;

import top.felixu.v2.annotation.ISelect;
import top.felixu.v2.mapper.MapperRegistry;
import top.felixu.v2.proxy.MapperProxy;
import top.felixu.v2.sqlsession.SqlSession;
import top.felixu.v2.utils.PackageUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author felixu
 * @datetime 2018/4/2 09:25
 */
public class Configuration {

    private String mapperScan;
    private MapperRegistry mapperRegistry = new MapperRegistry();

    public Configuration mapperScan(String path) {
        this.mapperScan = path;
        return this;
    }

    public Configuration build() {
        try {
            List<String> classNames = PackageUtil.getClassName(mapperScan, true);
            Optional.ofNullable(classNames)
                    .ifPresent(className -> className.stream().forEach(clazz -> registryMapper(clazz)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }


    public <T> T getMapper(Class<T> claszz, SqlSession sqlSession) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{claszz}, new MapperProxy(sqlSession));
    }

    private void registryMapper(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            Map<String, String> methodSql = new HashMap<>();
            Arrays.stream(methods)
                    .filter(method -> method.isAnnotationPresent(ISelect.class))
                    .forEach(method -> {
                ISelect annotation = method.getAnnotation(ISelect.class);
                Class<?> returnType = method.getReturnType();
                methodSql.put(method.getName(),annotation.value());
            });
            mapperRegistry.putObject(className, methodSql);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }
}

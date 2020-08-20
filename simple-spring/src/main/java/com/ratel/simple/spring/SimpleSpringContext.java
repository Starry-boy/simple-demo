package com.ratel.simple.spring;

import com.ratel.simple.spring.anno.SimpleAutowired;
import com.ratel.simple.spring.anno.SimpleComponent;
import com.ratel.simple.spring.anno.SimpleComponentScan;
import com.ratel.simple.spring.anno.SimpleConfiguartion;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author ratel
 * @date 2020/8/20
 */
public class SimpleSpringContext {
    private List<Object> beans;
    private List<ObjectTemplate> waitDIBeans = new ArrayList<>();
    private Map<Class<?>, Object> beanCache = new HashMap<>();

    public <T> T getBean(Class<T> cls){
        return cls.cast(beanCache.get(cls));
    }

    public SimpleSpringContext(Class<?> configClass) throws Exception {
        //do something
        initContext();
        List<String> beanNames = scanBeans(configClass);
        //do something
        beans = initBean(beanNames);
        //do something
        initDependencies(beans);
        //do something
        setBeanDependencies(waitDIBeans);
    }

    private List<String> scanBeans(Class<?> configClass) throws Exception {
        SimpleComponentScan annotation = configClass.getAnnotation(SimpleComponentScan.class);
        if (annotation == null) {
            throw new RuntimeException("配置类必须标记 @SimpleComponent 注解");
        }
        String value = annotation.value();
        ClassLoader classLoader = this.getClass().getClassLoader();
        //项目根目录
        String projectRootPath = classLoader.getResource("").getPath();
        //扫描所有class
        LinkedList<File> directories = new LinkedList();
        File file = new File(projectRootPath);
        directories.add(file);
        List<String> waitInitBeanNames = new ArrayList<>();

        //扫描并获取符合规则的类
            while (!directories.isEmpty()){
                file = directories.removeFirst();
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null && files.length != 0){
                        directories.addAll(Arrays.asList(files));
                    }
                } else {
                    String className = file.getAbsolutePath().replace(projectRootPath, "")
                            .replace("/", ".").replace(".class", "");
                    if (className.contains(value)) {
                        waitInitBeanNames.add(className);
                    }
                }
            }

        return waitInitBeanNames;
    }

    private void initDependencies(List<Object> objects) throws IllegalAccessException {
        List<ObjectTemplate> ots = new ArrayList<>(objects.size());
        objects.forEach(obj -> ots.add(new ObjectTemplate(obj)));
        for (ObjectTemplate ot : ots) {
            boolean flag = false;
            Field[] fields = ot.target.getClass().getFields();
            for (Field field : fields) {
                if (null == field.getAnnotation(SimpleAutowired.class)) {
                    continue;
                }
                for (int i = 0; i < objects.size(); i++) {
                    Object b = objects.get(i);
                    if (field.getType().equals(b.getClass())) {
                        field.set(ot.target,b);
                        break;
                    }
                    if (i == objects.size() -1){
                        ot.waitDIFields.add(field);
                        flag = true;
                    }
                }
            }
            if (flag){
                waitDIBeans.add(ot);
            }
        }

    }

    private List<Object> initBean(List<String> beanNames) throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        List<Object> list = new ArrayList<>(beanNames.size());
        List<Class<?>> classes = Arrays.asList(SimpleSpringContext.class, SimpleAutowired.class, SimpleComponent.class, SimpleComponentScan.class, SimpleConfiguartion.class);
        for (String beanName : beanNames) {
            Class<?> bClass = classLoader.loadClass(beanName);
            if (classes.contains(bClass)){
                continue;
            }
            Object o = bClass.newInstance();
            list.add(o);
            beanCache.put(bClass,o);
        }
        return list;
    }

    private void initContext() {
        //todo
    }

    private boolean setBeanDependencies(List<ObjectTemplate> ots) throws Exception {
        ots.forEach(ot ->{
            for (Object bean : beans) {
                for (Field waitDIField : ot.waitDIFields) {
                    if (waitDIField.getDeclaringClass().equals(bean.getClass())) {
                        try {
                            waitDIField.set(ot.target,bean);
                            return;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            throw new RuntimeException("not dependencies Bean:" + ot.target.getClass().getSimpleName());
        });
        return true;
    }

    private SimpleSpringContext() {
        throw new RuntimeException("不能使用无参构造");
    }
}

package com.ratel.simple.spring;

/**
 * @author ratel
 * @date 2020/8/20
 */
public class App {
    public static void main(String[] args) throws Exception {

        SimpleSpringContext simpleSpringContext = new SimpleSpringContext(AppConfig.class);
        Bean1 bean = simpleSpringContext.getBean(Bean1.class);
        System.out.println(bean.bean2);
        System.out.println(bean.bean3);
        Bean2 bean2 = simpleSpringContext.getBean(Bean2.class);
        System.out.println(bean2.bean3);
        Bean3 bean3 = simpleSpringContext.getBean(Bean3.class);
        System.out.println(bean3.bean1);
        System.out.println(bean3.bean3);

    }
}

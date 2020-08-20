package com.ratel.simple.spring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ratel
 * @date 2020/8/20
 */
public class ObjectTemplate {
    /** 依赖对象 */
    public List<Field> waitDIFields = new ArrayList<>();
    public Object target;

    public ObjectTemplate(Object target) {
        this.target = target;
    }

    public ObjectTemplate() {
    }
}

package com.ratel.simple.spring;

import com.ratel.simple.spring.anno.SimpleAutowired;

/**
 * @author ratel
 * @date 2020/8/20
 */
public class Bean3 {
    @SimpleAutowired
    public Bean1 bean1;
    @SimpleAutowired
    public Bean3 bean3;
}

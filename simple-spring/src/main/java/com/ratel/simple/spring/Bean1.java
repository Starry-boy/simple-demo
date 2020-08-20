package com.ratel.simple.spring;

import com.ratel.simple.spring.anno.SimpleAutowired;
import com.ratel.simple.spring.anno.SimpleComponent;

/**
 * @author ratel
 * @date 2020/8/20
 */
@SimpleComponent
public class Bean1 {
    @SimpleAutowired
    public Bean2 bean2;
    @SimpleAutowired
    public Bean3 bean3;
}

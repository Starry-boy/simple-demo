package com.ratel.simple.spring.anno;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SimpleComponentScan {
    String value() default "";
}

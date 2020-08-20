package com.ratel.simple.jdk.proxy;

import java.lang.reflect.Method;

/**
 * @author ratel
 * @date 2020/8/19
 */
public interface InvocationHandler {

	Object invocation(Object proxy, Method method, Object... args) throws Exception;
}

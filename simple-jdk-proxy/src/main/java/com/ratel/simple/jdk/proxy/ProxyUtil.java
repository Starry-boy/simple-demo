package com.ratel.simple.jdk.proxy;


import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author ratel
 * @date 2020/8/19
 */
public class ProxyUtil {

	public static Object newProsyInstance(Object targetObj,Class<?>[] interfaces,InvocationHandler invocationHandler) throws Exception{
		ClassTemplate classTemplate = new ClassTemplate(targetObj);
		String path = targetObj.getClass().getResource("").getPath()+classTemplate.realSimpleClassName+ ".java";
		System.out.println(path);
		File file = new File(path);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		fileOutputStream.write(classTemplate.toString().getBytes());
		fileOutputStream.flush();

		// 使用JavaCompiler 编译java文件
		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = jc.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(path);
		JavaCompiler.CompilationTask cTask = jc.getTask(null, fileManager, null, null, null, fileObjects);
		cTask.call();
		fileManager.close();

		URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new URL("file:"+path.replace(".java",".class"))});
		Class<?> aClass = urlClassLoader.loadClass(classTemplate.realClassName);
		System.out.println(aClass.getName());
		Constructor<?> constructor = aClass.getConstructor();
		Object o = constructor.newInstance();
		Class<?> aClass1 = o.getClass();
		aClass1.getField("h").set(o,invocationHandler);
		aClass1.getField("targetObj").set(o,targetObj);
		Field[] fields = aClass1.getFields();
		for (Field field : fields) {
			ClassTemplate.MethodTemplate methodTemplate = classTemplate.methodMap2.get(field.getName());
			if (methodTemplate != null){
				field.set(o,classTemplate.methodMap.get(methodTemplate));
			}
		}

		return o;
	}

	public static void main(String[] args) throws Exception {
		UserServiceImpl obj = new UserServiceImpl();
		UserService o = (UserService)newProsyInstance(obj, obj.getClass().getInterfaces(), new InvocationHandler() {
			@Override
			public Object invocation(Object proxy, Method method, Object... args) throws Exception{
				System.out.println("query before");
				Object obj = method.invoke(proxy,args);
				System.out.println("query after");
				return obj;
			}
		});
		System.out.println(o.query(111));
	}
}

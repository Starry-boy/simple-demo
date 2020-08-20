package com.ratel.simple.jdk.proxy;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author ratel
 * @date 2020/8/19
 */
public class ClassTemplate {
	public String packageContext;
	public String className;
	public String simpleClassName;
	public MethodTemplate[] methodTemplates;
	public Object targetObj;
	public String realClassName;
	public String realSimpleClassName;
	public Map<MethodTemplate,Method> methodMap = new HashMap<>();
	public Map<String,MethodTemplate> methodMap2 = new HashMap<>();
	public String[] interfaces;

	class MethodTemplate{
		public String methodName;
		public String returnType;
		public String[] argsTypes;
		public String exceptionStr;

		public MethodTemplate(String methodName, String returnType, String[] argsTypes,String exceptionStr) {
			this.methodName = methodName;
			this.returnType = returnType;
			this.argsTypes = argsTypes;
			this.exceptionStr = exceptionStr;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			MethodTemplate that = (MethodTemplate) o;
			return Objects.equals(methodName, that.methodName) &&
					Objects.equals(returnType, that.returnType) &&
					Arrays.equals(argsTypes, that.argsTypes);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(methodName, returnType);
			result = 31 * result + Arrays.hashCode(argsTypes);
			return result;
		}
	}

	public ClassTemplate(Object targetObj) {
		Class<?> aClass = targetObj.getClass();
		Class<?>[] interfaces = aClass.getInterfaces();
		this.interfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = interfaces[0].getName();
		}
		//包名
		this.packageContext = aClass.getName().substring(0,aClass.getName().lastIndexOf("."));
		//类名
		this.className = aClass.getName();
		this.simpleClassName = aClass.getSimpleName();
		String realClassNameSuffix = "$"+new Random().nextInt(100);
		this.realClassName = this.className + realClassNameSuffix;
		this.realSimpleClassName = this.simpleClassName + realClassNameSuffix;
		Method[] methods = aClass.getMethods();

		List<MethodTemplate> mts = new ArrayList<>();
		List<String> skipMethod = Arrays.asList("toString", "hashCode");
		for (int i = 0; i < methods.length; i++) {
			//只会重写 public 方法
			if (methods[i].getModifiers() != 1 || skipMethod.contains(methods[i].getName())) {
				continue;
			}
			Class<?>[] parameterTypes = methods[i].getParameterTypes();
			String[] args = null;
			if (parameterTypes != null && parameterTypes.length > 0){
				args = new String[parameterTypes.length];
				for (int j = 0; j < parameterTypes.length; j++) {
					args[j] = parameterTypes[j].getName();
				}
			}
			Class<?>[] exceptionTypes = methods[i].getExceptionTypes();
			String exceptionStr = "";
			if (exceptionTypes != null && exceptionTypes.length > 0){
				for (int i1 = 0; i1 < exceptionTypes.length; i1++) {
					exceptionStr += exceptionTypes[i1].getName() + ((exceptionTypes.length-1 == i1) ? "" : ",");
				}
			}
			MethodTemplate methodTemplate = new MethodTemplate(methods[i].getName(), methods[i].getReturnType().getName(), args,exceptionStr);
			methodMap.put(methodTemplate,methods[i]);
			mts.add(methodTemplate);
		}
		this.methodTemplates = mts.toArray(new MethodTemplate[mts.size()]);
	}

	@Override
	public String toString() {
		//写入包名
		String packageStr = "package " + packageContext + ";\r\n";
		String importStr = "import com.ratel.simple.jdk.proxy.InvocationHandler;\r\nimport java.lang.reflect.Method;\r\n";
		String classPrefix = "public class " + realSimpleClassName +" implements "+ interfaces[0] +" {\r\n";
		String memberVariable = "public InvocationHandler h;\r\n" + "public Object targetObj;\n";
		String memberMethodVariable = "";
		String constructStr = "public " + realSimpleClassName + "(){}\r\n";
		String classSuffix = "}";
		String methodStr = "";
		for (int i = 0; i < methodTemplates.length; i++) {
			MethodTemplate methodTemplate = methodTemplates[i];
			String argsStr = "";
			String invocationStr = "";
			String retType = methodTemplate.returnType == null ? "void" : methodTemplate.returnType;
			if (methodTemplate.argsTypes != null) {
				for (int j = 0; j < methodTemplate.argsTypes.length; j++) {
					String str = methodTemplate.argsTypes[j];
					String simpleArgName = str.substring(str.lastIndexOf(".") + 1);
					boolean isLastArg = j == methodTemplate.argsTypes.length - 1;
					argsStr += (methodTemplate.argsTypes[j] + " " + "var" + j + (isLastArg ? "" : ",") );
					invocationStr += "var" + j + (j == methodTemplate.argsTypes.length - 1 ? "" : ", ");
				}
			}
			memberMethodVariable += "public Method m"+i + ";\r\n";

			String methodPrefix = String.format("public %s %s(%s) %s {\r\n",retType,methodTemplate.methodName,argsStr,"".equals(methodTemplate.exceptionStr) ? "":"throws " + methodTemplate.exceptionStr );
			String invokeStr = retType.equals("void") ? "" : String.format("(%s)",retType);
			String methodContent = String.format("try { %s%s h.invocation(targetObj,%s,%s); } catch (Exception e) {throw new RuntimeException(e);}"
					,retType.equals("void") ? "" :"return ",invokeStr,"m"+i,invocationStr.equals("") ? "new Object[0]":invocationStr);
			String methodSuffix = "}\r\n";
			methodStr += methodPrefix + methodContent + methodSuffix;
			methodMap2.put("m"+i,methodTemplate);
		}
		return packageStr + importStr + classPrefix + memberVariable + memberMethodVariable + methodStr + constructStr + classSuffix;
	}
}

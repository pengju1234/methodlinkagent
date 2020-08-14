package com.boc.accuratetest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class MyTransformer implements ClassFileTransformer {
	Map<String,String> argsMap = null;
	/*static FileWriter fileWriter = null;
    static {
    	String gen = System.getProperty("user.dir");
		String filename = gen+"/allMethodInfo.txt";
		try {
			fileWriter = new FileWriter(new File(filename),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }*/
    public MyTransformer(Map<String,String> argsMap) {
    	this.argsMap = argsMap;
	}
    public MyTransformer() {
	}
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    	// com/example/controller/LiuyanController
        className = className.replace("/", ".");
        // 指定了哪些类需要插桩
    	if(!argsMap.isEmpty()) {
    		String packageUrl = argsMap.get("includes");
    		if(null != packageUrl && !"".equals(packageUrl)) {
    			if(!className.matches(packageUrl)) {
    				return null;
    			}
    		}
    	}
    	// 不管是否指定哪些类需要插桩，都过滤一下，防止传参没有剔除某些类，比如传参：com.*，没有剔除com.mysql
        if(className.startsWith("java") || className.startsWith("sun") || className.startsWith("jdk")
        		|| className.startsWith("org") || className.startsWith("ch") || className.startsWith("com.sun")
        		|| className.startsWith("com.fasterxml") || className.startsWith("freemarker")
        		|| className.startsWith("com.mysql") || className.startsWith("com.alibaba")
        		|| className.startsWith("net") || className.startsWith("jxl") || className.startsWith("com.zaxxer")) {
        	return null;
        }
        //com/example/biz/impl/LiuyanBizImpl$$FastClassBySpringCGLIB$$5f9ab983 从类池中获取不到这样的类
        if(className.contains("$$")) {
        	return null;
        }
        try {
        	ClassPool classPool = ClassPool.getDefault();
        	//classPool.appendClassPath(new LoaderClassPath(loader)); // 指定类加载器
        	//CtClass ctclass = classPool.get(className);
        	CtClass ctclass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            for(CtMethod ctMethod : ctclass.getDeclaredMethods()){
            	//  修改被插桩项目代码的字节码。相当于切面编程aop
            	String classname = ctclass.getName();
                String methodName = ctMethod.getName();
                if("$jacocoInit".equals(methodName)) {
                	continue;
                }
				CtClass[] parameterTypes = ctMethod.getParameterTypes();
				StringBuilder params = new StringBuilder();
				for (int i=0;i<parameterTypes.length;i++) {
					//params.append(parameterTypes[i].getName().toString());
					params.append(parameterTypes[i].getSimpleName().toString());
					if(parameterTypes.length-1 != i) {
						params.append(",");
					}
				}
                if(!ctMethod.isEmpty()) { // 有方法体
                	// 使用javaassist的反射方法获取方法的参数名  
                    /*MethodInfo methodInfo = ctMethod.getMethodInfo();  
                    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();  
                    LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute  
                            .getAttribute(LocalVariableAttribute.tag);  
                    if (attr == null) {
                        // exception  
                    	System.out.println("attr为null");
                    }
                    int length = ctMethod.getParameterTypes().length;
                    System.out.println("length:"+length);
                    System.out.println(classname);
                    System.out.println(methodName);
                    if(length >= 0) {
                    	String[] paramNames = new String[ctMethod.getParameterTypes().length];
                    	int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;  
                    	for (int i = 0; i < paramNames.length; i++)
                    		paramNames[i] = attr.variableName(i + pos);
                    	// paramNames即参数名  
                    	for (int i = 0; i < paramNames.length; i++) {  
                    		System.out.println("参数名" + i + ":" + paramNames[i]);  
                    	}
                    }*/
                	// 获取全部方法
                	/*fileWriter.write(classname+"."+methodName+"("+params.toString()+")");
                	fileWriter.write("\r\n");
                	fileWriter.flush();*/
                	// 插桩，调用链暂定输出到文件
                	String insertMethod = insertMethod_currentParams(classname,methodName,params.toString());
                	ctMethod.insertBefore(insertMethod);
                	/*String insertMethod2 = insertMethod(classname,methodName+"_2",params.toString());
                	ctMethod.insertAfter(insertMethod2, true);*/
                }
                // 能得到参数值吗？
            }
            return ctclass.toBytecode();
        } catch (Exception e) {
        	System.err.println("e:"+e.getMessage());
        	e.printStackTrace();
		}
        return null;
	}
	/**
	 * 	the chazhuang method
	 * @param classname
	 * @param methodName
	 * @param params
	 * @return
	 */
	public static String insertMethod(String classname,String methodName,String params) {
		return  "java.io.FileWriter fw1234 = null;\r\n" + 
				"		String gen1234 = System.getProperty(\"user.dir\");\r\n" + 
				"		String pathname1234 = gen1234+\"/chazhuang.txt\";\r\n" + 
				"		java.io.File filename1234 = new java.io.File(pathname1234);\r\n" + 
				"		StackTraceElement[] stackTrace1234 = Thread.currentThread().getStackTrace();\r\n" + 
				"		try {\r\n" + 
				"	        for (int i=stackTrace1234.length-1;i>=0;i--) {\r\n" + 
				"	        	String className1234 = stackTrace1234[i].getClassName();\r\n" + 
				"	        	String methodName1234 = stackTrace1234[i].getMethodName();\r\n" + 
				"	        	int lineNumber1234 = stackTrace1234[i].getLineNumber();\r\n" + 
				"	        	if(className1234.startsWith(\"com.boc\") \r\n" + 
				"	        			|| (\"java.lang.Thread\".equals(className1234) && \"getStackTrace\".equals(methodName1234) ) ) {\r\n" + 
				"	        		fw1234 = new java.io.FileWriter(filename1234,true);\r\n" + 
				"	        		fw1234.write(className1234+\".\"+methodName1234+\".\"+lineNumber1234);\r\n" + 
				"	    			fw1234.write(\"\\r\\n\");\r\n" + 
				"	    			fw1234.flush();\r\n" + 
				"	        	}else {\r\n" + 
				"	        		continue;\r\n" + 
				"	        	}\r\n" + 
				"			}\r\n" + 
				"		} catch (Exception e) {\r\n" + 
				"			System.err.println(\"error,the target project method end:\"+e.getMessage());\r\n" + 
				"		}finally {\r\n" + 
				"			try {\r\n" + 
				"				fw1234.close();\r\n" + 
				"		} catch (java.io.IOException e) {\r\n" + 
				"		}\r\n" + 
				"		}";
	}
	public static String insertMethod_currentParams(String classname,String methodName,String params) {
		return  " java.io.FileWriter fw1234 = null;\r\n" + 
				"		String gen1234 = System.getProperty(\"user.dir\");\r\n" + 
				"		String pathname1234 = gen1234+\"/chazhuang.txt\";\r\n" + 
				"		java.io.File filename1234 = new java.io.File(pathname1234);\r\n" + 
				"		StackTraceElement[] stackTrace1234 = Thread.currentThread().getStackTrace();\r\n" + 
				"		String currentTimeMillis1234 = String.valueOf(System.currentTimeMillis());\r\n" + 
				"		try {\r\n" + 
				
				"	       fw1234 = new java.io.FileWriter(filename1234,true);\r\n" + 
				
				"	        for (int i=stackTrace1234.length-1;i>=0;i--) {\r\n" + 
				"	        	String className1234 = stackTrace1234[i].getClassName();\r\n" + 
				"	        	String methodName1234 = stackTrace1234[i].getMethodName();\r\n" + 
				"	        	//int lineNumber1234 = stackTrace1234[i].getLineNumber();\r\n" + 
				"	        	if(className1234.startsWith(\"com.boc\") \r\n" + 
				"	        			|| (\"java.lang.Thread\".equals(className1234) && \"getStackTrace\".equals(methodName1234) ) ) {\r\n" + 
				"	        		if(i == 1) { // current method of this stack run at\r\n" + 
				"	        			fw1234.write(currentTimeMillis1234+\".\"+className1234+\".\"+methodName1234+\"("+params+")\");\r\n" + 
				"	        		}else if(i == 0){ // stacktrace method \r\n"+
				"						java.lang.StringBuilder sb = new java.lang.StringBuilder();\r\n" + 
				"						for(int j=0;j<5;j++){\r\n" + 
				"							int r = (int)((java.lang.Math.random()*9)+1);\r\n" + 
				"							sb.append(r);\r\n" + 
				"						}\r\n"+
				" fw1234.write(sb.toString()+\"&\"+currentTimeMillis1234+\".\"+className1234+\".\"+methodName1234);\r\n" +
				"					}else{\r\n" + 
				"	        			fw1234.write(currentTimeMillis1234+\".\"+className1234+\".\"+methodName1234);\r\n" + 
				"	        		}\r\n" + 
				"	    			fw1234.write(\"\\r\\n\");\r\n" + 
				"	    			fw1234.flush();\r\n" + 
				"	        	}else {\r\n" + 
				"	        		continue;\r\n" + 
				"	        	}\r\n" + 
				"			}\r\n" + 
				
				"		} catch (Exception e) {\r\n" + 
				"			System.err.println(\"error,the target project method end:\"+e.getMessage());\r\n" + 
				"		}finally {\r\n" + 
				"			try {\r\n" + 
				"				fw1234.close();\r\n" + 
				"		} catch (java.io.IOException e) {\r\n" + 
				"		}\r\n" + 
				"		}";
	}
	public static String insertMethod_21(String classname,String methodName,String params) {
		return  "				java.util.Date now1234 = new java.util.Date();\r\n" + 
				"				String currentTimeMillis1234 = String.valueOf(System.currentTimeMillis());\r\n" + 
				"				String xinxi1234 = currentTimeMillis1234+\".\"	\r\n" + 
				"				+\""+ classname +"."+ methodName	+"("+params+")\";\r\n" + 
				"				// output to this file\r\n" + 
				"				java.io.FileWriter fw1234 = null;\r\n" + 
				"				try {\r\n" + 
				"					String gen1234 = System.getProperty(\"user.dir\");\r\n" + 
				"					String pathname1234 = gen1234+\"/chazhuang.txt\";\r\n" + 
				"					java.io.File filename1234 = new java.io.File(pathname1234);\r\n" + 
				"					fw1234 = new java.io.FileWriter(filename1234,true);\r\n" + 
				"					fw1234.write(xinxi1234);\r\n" + 
				"					fw1234.write(\"\\r\\n\");\r\n" + 
				"					fw1234.flush();\r\n" + 
				"				} catch (Exception e) {\r\n" + 
				"					System.err.println(\"error,the target project method end:\"+e.getMessage());\r\n" + 
				"				}finally {\r\n" + 
				"					try {\r\n" + 
				"						fw1234.close();\r\n" + 
				"				} catch (java.io.IOException e) {\r\n" + 
				"				}\r\n"+
				"				}";
	}
	public static String insertMethod_beifen(String classname,String methodName,String params) {
		return  "				java.util.Date now1234 = new java.util.Date();\r\n" + 
				"				String currentTimeMillis1234 = String.valueOf(System.currentTimeMillis());\r\n" + 
				"				String xinxi1234 = currentTimeMillis1234+\".\"	\r\n" + 
				"				+\""+ classname +"."+ methodName	+"("+params+")\";\r\n" + 
				"				// output to this file\r\n" + 
				"				java.io.FileWriter fw1234 = null;\r\n" + 
				"				try {\r\n" + 
				"					String gen1234 = System.getProperty(\"user.dir\");\r\n" + 
				"					String pathname1234 = gen1234+\"/chazhuang.txt\";\r\n" + 
				"					java.io.File filename1234 = new java.io.File(pathname1234);\r\n" + 
				"					fw1234 = new java.io.FileWriter(filename1234,true);\r\n" + 
				"					fw1234.write(xinxi1234);\r\n" + 
				"					fw1234.write(\"\\r\\n\");\r\n" + 
				"					fw1234.flush();\r\n" + 
				"				} catch (Exception e) {\r\n" + 
				"					System.err.println(\"error,the target project method end:\"+e.getMessage());\r\n" + 
				"				}finally {\r\n" + 
				"					try {\r\n" + 
				"						fw1234.close();\r\n" + 
				"				} catch (java.io.IOException e) {\r\n" + 
				"				}\r\n"+
				"				}";
	}
	public static void ttt(String params) {
	}
	public static void t() {
		
	}
	public static void main(String[] args) throws NotFoundException, CannotCompileException {
		String insertMethod = insertMethod_currentParams("classname", "methodName", "params");
		System.out.println(insertMethod);
	}
}


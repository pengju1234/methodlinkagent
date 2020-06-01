package test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;

public class MyTransformer implements ClassFileTransformer {
	/*需要插桩的代码的包路径，只需要包路径的最前面部分即可*/
    private final static String need01 = "com/example"; 
    
    /*不需要插桩的代码的包路径*/
    private final static String[] noNeeds = {"com/example/acore","$$","com/example/acl"
    		,"com/example/annotaton"};
    
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    	// Class<? extends ClassLoader> class1 = loader.getClass();
    	// System.out.println("class1:"+class1);
        if(!className.startsWith(need01)){
        	return null;
        }
        /*com/example/biz/impl/LiuyanBizImpl$$FastClassBySpringCGLIB$$5f9ab983 这样的className，从类池中获取不到类
	         * 	根据调试，被插桩的项目启动时，这样的className也有少量的，但是当客户端访问服务端时，都是这样的className。
	         * 	添加下面的判断，可以做到当客户端访问服务端时，不再修改项目的class文件。这样也防止了ClassPool中没有找到该类的异常。
        */
        for(String str : noNeeds) {
        	if(className.contains(str)) {
            	return null;
            }
        }
        // com/example/controller/LiuyanSaoleiController
        className = className.replace("/", ".");
        try {
        	
        	ClassPool classPool = ClassPool.getDefault();
        	/* 解决javassist.NotFoundException: com.example.biz.impl.UserBizImpl
        	 	在springboot项目有自己的类加载器，当在linux上运行被插桩项目时，使用的是springboot的类加载器。
        	 	而classPool找类时，好像是从默认的类加载器加载到的位置去找的。而该默认的类加载器是springboot加载器的父类，父看不到子加载器加载的类。
        	 	这一行代码表示：到该类加载器中寻找类。 loader：该参数，当前项目的运行环境使用的哪个类加载器，就入参哪个类加载器
        	 */
        	classPool.appendClassPath(new LoaderClassPath(loader)); // 指定类加载器
        	CtClass ctclass = classPool.get(className);
            for(CtMethod ctMethod : ctclass.getDeclaredMethods()){
            	//  修改被插桩项目代码的字节码。相当于切面编程aop
                String methodName = ctMethod.getName();
                String classname = ctclass.getName();
                StringBuilder before = new StringBuilder();
                before.append("System.err.println(\"=============="+classname+"."+methodName+" ==============\");\n");
                // 暂定输出到文件
                
                ctMethod.insertBefore(before.toString());
            }
            // 呼应上面的classpool指定的类加载器，百度到一篇文章说需要加下面的代码，表示该类由给定的类加载器加载。
            // 但是调试时，却报错：多个重复的类？ 注掉这一行代码之后，没有报错了。
            // ctclass.toClass(loader, ctclass.getClass().getProtectionDomain());
            return ctclass.toBytecode();
        } catch (Exception e) {
        	System.err.println("e:"+e.getMessage());
        	e.printStackTrace();
		}
        return null;
	}

}


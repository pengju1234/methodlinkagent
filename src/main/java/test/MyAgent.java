package test;

import java.lang.instrument.Instrumentation;

public class MyAgent {
	public static void premain(String args, Instrumentation inst){
        System.out.println("Hi, I'm agent!");
        System.out.println("args:"+args);
        // 参数赋值，经测试可以避免两个代理的冲突，原因不明。但这时jacoco.exec覆盖率文件增大了一倍，原因不明。
        // java -javaagent:jacocoagent.jar -javaagent:myagent.jar=myagent -jar saolei.jar
        if(args != null && args.equals("myagent")) {
        	inst.addTransformer(new MyTransformer());
        }
    }
}

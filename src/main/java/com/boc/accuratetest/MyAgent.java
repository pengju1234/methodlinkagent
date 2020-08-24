package com.boc.accuratetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MyAgent {
	public static void premain(String args, Instrumentation inst){
		/**
		 * 	传参说明：includes可以传，推荐传。端口目前不传：8765
		 * -javaagent:C:\Users\tom\Desktop\jacocoagent.jar=includes=com.example.*,port=8765
		 */
        System.out.println("Hi, I'm methodlinkagent!");
        System.out.println("args:"+args); // 示例：myagent=1,a=2,b=3
        Map<String,String> argsMap = new HashMap<>();
        if(null != args) {
        	String[] split = args.split(",");
        	for (String s : split) {
        		String[] split2 = s.split("=");
        		argsMap.put(split2[0], split2[1]);
			}
        }
        inst.addTransformer(new MyTransformer(argsMap)); // 类转换器
        OutPutMethodLink(); // 输出方法链
    }
	/**
	 * 输出记录方法链的文件
	 */
	private static void OutPutMethodLink() {
		new Thread(new Runnable() {
			public void run() {
				try {
		        	ServerSocket server = new ServerSocket(8765);
		        	System.out.println("=========socket server启动了，methodlinkagent支持=========");
		        	int NumberOfVisits = 1;
		        	String lastLine = null;
		        	while(true) {
		        		Socket socket = server.accept();
		        		System.out.println("NumberOfVisits:"+NumberOfVisits);
		        		InputStream is = socket.getInputStream();// 字节输入流            
		        		InputStreamReader isr = new InputStreamReader(is);// 将字节流转为            
		        		BufferedReader br = new BufferedReader(isr);// 为输入流添加缓冲    
		        		StringBuilder sb = new StringBuilder();
		        		String info = null;
		        		while ((info = br.readLine()) != null) {
		        			System.out.println("我是服务器，客户端说" + info);
		        			sb.append(info);
		        		}
		        		socket.shutdownInput();// 关闭输入流 
		        		OutputStream os = socket.getOutputStream(); // 4.获取输出流   
		        		PrintWriter pw = new PrintWriter(os); // 包装打印流       
		        		// 
		        		System.out.println("我是agent，最后一行lastLine:"+lastLine);
		        		if("chazhuang.txt".equals(sb.toString())) {
		        			// 获取插桩文件
		        			String gen = System.getProperty("user.dir");
		        			BufferedReader reader = new BufferedReader(new FileReader(new File(gen+"/chazhuang.txt")));
		        	        String tempStr = null;
		        	        StringBuilder sbWrite = new StringBuilder();
		        	        boolean start = false;
		        	        /*
		        	         * 如果点击开始、结束直接，服务器没有被访问过，那么点击开始时，只会发送lastLine这最后一行
		        	         * 无论什么情况，点击结束时，只会发送lastLine、和测试用例调用到的方法
		        	         */
		        	        while ((tempStr = reader.readLine()) != null) { // 读整个文件，从最开始读
		        	        	if(NumberOfVisits == 1) { // 首次访问
		        	        		sbWrite.append(tempStr+"\n");
		        	        	}else {
		        	        		if(lastLine.equals(tempStr)) {
		        	        			// 开始收集
		        	        			start = true;
		        	        			sbWrite.append(tempStr+"\n");
		        	        		}
		        	        		if(start == true) {
		        	        			sbWrite.append(tempStr+"\n");
		        	        		}
		        	        	}
		        	            lastLine = tempStr;// 记录最后一行
		        	        }
		        	        pw.write(sbWrite.toString());
        	        		pw.flush();
		        	        reader.close();
		        	        NumberOfVisits++;
		        		}
		        		/*else if("allMethodInfo.txt".equals(sb.toString())) {
		        			// 获取目标项目所有的方法信息
		        			String gen = System.getProperty("user.dir");
		        			BufferedReader reader = new BufferedReader(new FileReader(new File(gen+"/allMethodInfo.txt")));
		        	        String tempStr = null;
		        	        while ((tempStr = reader.readLine()) != null) {
		        	        	pw.write(tempStr+"\n");
		                		pw.flush();
		        	        }
		        	        reader.close();
		        		}*/
		        		socket.shutdownOutput();
		        		// 5.关闭资源            
		        		pw.close();
		        		br.close();
		        		isr.close();
		        		is.close();
		        		//server.close();
		        	}
		        } catch (IOException e) {
	
		        }finally {
		        	
		        }
			}
		}).start();
	}
}

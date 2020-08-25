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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		        	         * 	1、第一次访问，即第一次点击开始时，只会发送lastLine这最后一行
		        	         * 	2、第二次点击开始时，发送两次用例执行之间的方法，和lastLine最后一行
		        	         * 	3、正常点击结束时，只会发送lastLine、和测试用例调用到的方法
		        	         */
		        	        String lastLineTemp = null;
		        	        while ((tempStr = reader.readLine()) != null) { // 读整个文件，从最开始读
		        	        	if(NumberOfVisits == 1) { // 首次访问，首次获取数据（只能是首次点击开始）
		        	        		//
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
		        	        	lastLineTemp = tempStr;// 临时记录最后一行
		        	        }
		        	        if(NumberOfVisits == 1) { // 首次访问，首次获取数据（只能是首次点击开始）
	        	        		sbWrite.append(lastLine+"\n");
	        	        	}
		        	        pw.write(sbWrite.toString());
        	        		pw.flush();
		        	        reader.close();
		        	        lastLine = lastLineTemp;// 记录最后一行
		        	        NumberOfVisits++;
		        		}
		        		/** 从测试服务器获取已测试方法	*/
		        		if("chazhuang.txt_testedmethod".equals(sb.toString())) {
		        			// 获取插桩文件
		        			String gen = System.getProperty("user.dir");
		        			BufferedReader reader = new BufferedReader(new FileReader(new File(gen+"/chazhuang.txt")));
		        	        String tempStr = null;
		        	        StringBuilder sbWrite = new StringBuilder();
		        	        while ((tempStr = reader.readLine()) != null) { // 读整个文件，从最开始读
		        	        	sbWrite.append(tempStr+"\r\n");
		        	        }
		        	        String contents = appendParams(sbWrite.toString()); // 补参数类型
		        	        String[] lines = contents.split("\r\n"); // 原始数据的每一行（包前面的时间等等已经去掉了）
		                    Set<String> linesSet = new HashSet<>(); // 去重
		                    for (String line : lines) {
		                    	if(lines.equals("java.lang.Thread.getStackTrace()")) {
		                    		continue;
		                    	}
		                    	linesSet.add(line);
		        			}
		                    StringBuilder sb2 = new StringBuilder();
		        	        for (String line : linesSet) {
		        	        	sb2.append(line+"\r\n");
							}
		                    
		        	        pw.write(sb2.toString());
        	        		pw.flush();
		        	        reader.close();
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
	/**
	 * 	收集的原始数据，补上参数类型。
	 * @param contents
	 * @param testExampleId
	 * @return
	 */
	public static String appendParams(String contents) {
		StringBuilder sb = new StringBuilder();
		String[] lines = contents.split("\r\n");
		List<String> linesForFindParams = new ArrayList<>();
		for (int i=0;i<lines.length;i++) { // 每一个方法的堆栈链 中 的每一行
			// 截取（不要随机数、时间）
			String line = lines[i].substring(lines[i].indexOf(".")+1, lines[i].length());
			linesForFindParams.add(line);
			if(!line.contains("(") && !line.contains("java.lang.Thread.getStackTrace")) {
				// 补齐参数类型：后头找，使用第二个遇到的同包、同类、同方法名，并且有参数类型的（重载方法嵌套调用的、异步方法调用的，不能这样找，以后再解决）
				for(int j=linesForFindParams.size()-1;j>=0;j--) {
					if(linesForFindParams.get(j).contains(line+"(")) {
						line = linesForFindParams.get(j);
					}
				}
			}
			if(!line.contains("(")) { // 没找到，暂时默认没有
				line = line+"()";
			}
			sb.append(line+"\r\n");
		}
		return sb.toString();
	}
}

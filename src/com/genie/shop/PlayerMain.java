package com.genie.shop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


public class PlayerMain {
	public static void main(String[] args) throws Exception {
		
		if (args.length <2){
			System.out.println("required shopId , shopPassword!!");
			System.exit(0);
		}
		
		 ApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");

		// ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("taskExecutor");
		 
		 ShopNGenieController obj = (ShopNGenieController) context.getBean("shopNGenieController");
		 
	     obj.setShopId(args[0]);
	     obj.setShopPasswd(args[1]);

	     obj.loadConfig();
	     obj.checkConfiguration();		
	     obj.run();
	     
	   }
	
	
}

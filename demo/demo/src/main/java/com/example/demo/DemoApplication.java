package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableScheduling
@EnableRabbit
/**
 * 短鏈接服務應用程式主類
 * 啟動Spring Boot應用，啟用定時任務和RabbitMQ功能
 */
public class DemoApplication {

	/**
	 * 應用程式入口點
	 * 啟動Spring Boot應用程式
	 * 
	 * @param args 命令行參數
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

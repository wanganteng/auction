package com.auction;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 拍卖系统主启动类
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.auction.mapper")
@EnableScheduling
@EnableAsync
public class AuctionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionSystemApplication.class, args);
        System.out.println("=================================");
        System.out.println("拍卖系统启动成功！");
        System.out.println("后台管理地址: http://localhost:8080/auction/admin");
        System.out.println("API文档地址: http://localhost:8080/auction/swagger-ui/");
        System.out.println("=================================");
    }
}

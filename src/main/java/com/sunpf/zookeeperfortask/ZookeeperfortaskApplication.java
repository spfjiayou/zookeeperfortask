package com.sunpf.zookeeperfortask;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sunpf.zookeeperfortask.inter.mapper")
public class ZookeeperfortaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperfortaskApplication.class, args);
    }

}

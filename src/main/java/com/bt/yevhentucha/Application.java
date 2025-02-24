package com.bt.yevhentucha;

import com.bt.yevhentucha.service.WarehouseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseService.class, args);
    }
}

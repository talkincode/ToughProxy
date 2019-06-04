package org.toughsocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Configuration
@EnableCaching
public class ToughsocksApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToughsocksApplication.class, args);
    }

}

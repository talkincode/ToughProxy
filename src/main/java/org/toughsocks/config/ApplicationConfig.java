package org.toughsocks.config;

import com.google.gson.Gson;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.servlet.MultipartConfigElement;

@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationConfig {

    private String version;
    private String ticketDir;


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("1024000KB");
        factory.setMaxRequestSize("102400000KB");
        return factory.createMultipartConfig();
    }

    @Bean
    public ThreadPoolTaskExecutor systaskExecutor(){
        ThreadPoolTaskExecutor sysTaskExecutor = new ThreadPoolTaskExecutor();
        sysTaskExecutor.setCorePoolSize(32);
        sysTaskExecutor.setMaxPoolSize(512);
        sysTaskExecutor.setQueueCapacity(100000);
        sysTaskExecutor.setKeepAliveSeconds(60);
        sysTaskExecutor.setThreadNamePrefix("TASK_EXECUTOR");
        return sysTaskExecutor;
    }


    @Bean
    public Gson gson(){
        return new Gson();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTicketDir() {
        return ticketDir;
    }

    public void setTicketDir(String ticketDir) {
        this.ticketDir = ticketDir;
    }
}

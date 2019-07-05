package org.toughproxy.config;

import com.google.gson.Gson;
import org.apache.catalina.mbeans.JmxRemoteLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.toughproxy.common.MockRemoteExporter;
import org.toughproxy.common.MockRmiProxyFactoryBean;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.component.LocalSessionCache;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.component.SessionCache;

import javax.servlet.MultipartConfigElement;

@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationConfig {

    private String version;
    private String ticketDir;
    private String rmiMaster;
    private String rmiRole;
    private int rmiPort;

    @Autowired
    Memarylogger memarylogger;

    @Autowired
    ApplicationContext ctx;

    @Bean
    public RemoteExporter registerRMIExporter() {
        if(getRmiPort()>0){
            RmiServiceExporter exporter = new RmiServiceExporter();
            exporter.setServiceName("sessioncache");
            exporter.setServiceInterface(SessionCache.class);
//            exporter.setRegistryHost(getRmiHost());
            exporter.setRegistryPort(getRmiPort());
            exporter.setService(new LocalSessionCache(memarylogger));
            return exporter;
        }else{
            return new MockRemoteExporter();
        }
    }

    @Bean
    public RmiProxyFactoryBean rmiProxyFactoryBean() {
        if("client".equals(getRmiRole()) && ValidateUtil.isNotEmpty(getRmiMaster())){
            RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
            rmiProxyFactoryBean.setServiceUrl(getRmiMaster());
            rmiProxyFactoryBean.setServiceInterface(SessionCache.class);
            rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
            return rmiProxyFactoryBean;
        }else{
            DefaultListableBeanFactory beanFactory=(DefaultListableBeanFactory)ctx.getAutowireCapableBeanFactory();
            beanFactory.registerSingleton("sessionCache",new LocalSessionCache(memarylogger));
            return new MockRmiProxyFactoryBean();
        }
    }


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

    public String getRmiMaster() {
        return rmiMaster;
    }

    public void setRmiMaster(String rmiMaster) {
        this.rmiMaster = rmiMaster;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public String getRmiRole() {
        return rmiRole;
    }

    public void setRmiRole(String rmiRole) {
        this.rmiRole = rmiRole;
    }

}

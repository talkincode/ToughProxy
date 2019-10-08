package org.toughproxy.component;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.toughproxy.common.*;
import org.toughproxy.common.shell.ExecuteResult;
import org.toughproxy.common.shell.LocalCommandExecutor;
import org.toughproxy.entity.TsPppItem;
import org.toughproxy.mapper.TsPppItemMapper;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.toughproxy.config.Constant.*;

@Component
public class PoolTaskScheduler {

    @Autowired
    private Memarylogger logger;

    private Long lastRedialup = System.currentTimeMillis();

    @Autowired
    private ThreadPoolTaskExecutor systaskExecutor;

    @Autowired
    private LocalCommandExecutor localCommandExecutor;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;


    @Scheduled(fixedDelay = 30 * 1000)
    public void redialupPPPInterface(){
        long timesec = 300;
        try {
            timesec = Long.valueOf(FileUtil.getFileContent(POOL_DIAUP_INTERVAL_FILE).trim());
        } catch (IOException e) {
            logger.error("IP池重拨间隔没有设置或设置不正确",Memarylogger.SYSTEM);
        }

        if((System.currentTimeMillis() -  lastRedialup) < (timesec*1000)){
            return;
        }
        systaskExecutor.execute(()->{
            lastRedialup = System.currentTimeMillis();
            ExecuteResult ert = localCommandExecutor.executeCommand("/usr/local/bin/rediappp --all",300*1000);
            RestResult result = new RestResult(ert.getExitCode(), ert.getExecuteOut());
            logger.info("IP池重拨结果："+result.toString(),Memarylogger.SYSTEM);
        });

    }

    @Scheduled(fixedDelay = 15 * 1000)
    public void loadPPPInterface(){
        try {
            String poolname = FileUtil.getFileContent(POOL_NAME_FILE).trim();
            String areaCode = FileUtil.getFileContent(POOL_AREA_CODE_FILE).trim();
            String ipaddrType = FileUtil.getFileContent(POOL_IPADDR_TYPE_FILE).trim();
            Integer timeType = Integer.valueOf(ValidateUtil.isNotEmpty(ipaddrType) && ValidateUtil.isInteger(ipaddrType)?"1":ipaddrType);
            List<String[]> pppitems = new ArrayList<>();
            if(ValidateUtil.isEmpty(poolname)){
                logger.error("没有配置IP池名称",Memarylogger.SYSTEM);
                return;
            }
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if(address instanceof Inet4Address) {
                        if(networkInterface.getName().startsWith("ppp")){
                            pppitems.add(new String[]{networkInterface.getName(), address.getHostAddress()});
                        }
                    }
                }
            }

            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                TsPppItemMapper mapper = sqlSession.getMapper(TsPppItemMapper.class);
                StringBuilder logstr = new StringBuilder();
                for(String[] items : pppitems){
                    TsPppItem item =  mapper.getByPoolnameAndName(poolname,items[0]);
                    String name = items[0];
                    String ipaddr = items[1] == null?"":items[1];
                    if(item!=null){
                        if(!ipaddr.equals(item.getIpaddr())){
                            item.setIpaddr(items[1]);
                            if(ValidateUtil.isNotEmpty(ipaddr)){
                                item.setDiaTimes(item.getDiaTimes()+1);
                                item.setLastDia(DateTimeUtil.nowTimestamp());
                            }else{
                                systaskExecutor.execute(()->{
                                    ExecuteResult ert = localCommandExecutor.executeCommand("/usr/local/bin/upppp --startone "+name,120);
                                    logger.info(ert.getExecuteOut(),Memarylogger.SYSTEM);
                                });
                            }
                            mapper.updateByPrimaryKey(item);
                            logstr.append("update pool item "+item.toString()+"\n");
                        }
                    }else{
                        if(ValidateUtil.isNotEmpty(ipaddr)){
                            item = new TsPppItem();
                            item.setPoolname(poolname);
                            item.setName(name);
                            item.setIpaddr(ipaddr);
                            item.setTimeType(timeType);
                            item.setAreaCode(areaCode);
                            item.setPeer("");
                            item.setDiaTimes(1);
                            item.setLastDia(DateTimeUtil.nowTimestamp());
                            mapper.insert(item);
                            logstr.append("insert pool item "+item.toString()+"\n");
                        }
                    }
                }
                sqlSession.commit();
                logger.info("更新ip池:"+poolname+": "+logstr.toString(), Memarylogger.SYSTEM);
            } catch (Exception ee) {
                logger.error("更新IP池失败", ee,Memarylogger.SYSTEM);
            }

        } catch (IOException e) {
            logger.error("更新IP池失败", e,Memarylogger.SYSTEM);
        }
    }
}

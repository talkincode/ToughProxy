package org.toughproxy.component;

import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.PageResult;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.entity.SocksSession;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理会话缓存， 记录更新连接信息
 */
public class LocalSessionCache implements SessionCache {

    private final static Map<String, SocksSession> cacheData = new ConcurrentHashMap<>();
    private final static Map<String, String> usernameCache = new ConcurrentHashMap<>();


    private Memarylogger logger;

    public LocalSessionCache(Memarylogger logger) {
        this.logger = logger;
    }

    @Override
    public Map<String, SocksSession> getCacheData() {
        return cacheData;
    }

    @Override
    public void setUsername(String key, String username){
        usernameCache.put(key,username);
    }

    @Override
    public String getUsername(String key){
        String username = usernameCache.get(key);
        return ValidateUtil.isEmpty(username)?"anonymous":username;
    }


    @Override
    public void addSession(SocksSession session){
        cacheData.put(session.getKey(),session);
    }

    @Override
    public SocksSession remove(String key){
        return cacheData.remove(key);
    }

    @Override
    public void updateUpBytes(String key, Long bytes){
        SocksSession session = cacheData.get(key);
        if(session!=null)
            session.setUpBytes(session.getUpBytes()+bytes);
    }

    @Override
    public void updateDownBytes(String key, Long bytes){
        SocksSession session = cacheData.get(key);
        if(session!=null)
            session.setDownBytes(session.getDownBytes()+bytes);
    }

    @Override
    public void updateUpBytes(InetSocketAddress clientaddr, Long bytes){
        try{
            String key = clientaddr.getHostString()+ ":"+clientaddr.getPort();
            updateUpBytes(key,bytes);
        }catch (Exception ignore){}
    }

    @Override
    public void updateDownBytes(InetSocketAddress clientaddr, Long bytes){
        try{
            String key = clientaddr.getHostString()+ ":"+clientaddr.getPort();
            updateDownBytes(key,bytes);
        }catch (Exception ignore){}
    }

    @Override
    public SocksSession stopSession(String key){
        SocksSession session = cacheData.remove(key);
        if(session!=null){
            session.setEndTime(DateTimeUtil.getDateTimeString());
            usernameCache.remove(key);
        }
        return session;
    }

    @Override
    public SocksSession getSession(InetSocketAddress addr){
        String key = addr.getHostString()+ ":"+addr.getPort();
        return cacheData.get(key);
    }

    @Override
    public SocksSession stopSession(InetSocketAddress addr){
        String key = addr.getHostString()+ ":"+addr.getPort();
        SocksSession session = cacheData.remove(key);
        if(session!=null){
            session.setEndTime(DateTimeUtil.getDateTimeString());
            usernameCache.remove(key);
        }
        return session;
    }

    /** 查询上网帐号并发数 是否超过限制， 超过预设即返回， 避免多次循环*/
    @Override
    public boolean isLimitOver(String userName, int limit)
    {
        if(limit==0){
            return false;
        }
        int onlineNum = 0;
        List<SocksSession> copyList = new ArrayList<>(cacheData.values());
        for (SocksSession session : copyList) {
            if (userName.equals(session.getUsername())){
                onlineNum++;
            }
            if (onlineNum >= limit){
                return true;
            }
        }
        return onlineNum >= limit;
    }


    /**
     * 删除过期无效的会话
     * 允许迭代快速失败
     */
    @Override
    public void clearExpireSession(){
        String ctime = DateTimeUtil.getDateTimeString();
        cacheData.values().removeIf(x-> x.getUpBytes() == 0 && x.getDownBytes() == 0 && DateTimeUtil.compareSecond(ctime,x.getStartTime())>60);
    }


    private boolean filterSession(SocksSession online, String username,String srcAddr,Integer srcPort,String dstAddr,Integer dstPort) {

        if (ValidateUtil.isNotEmpty(username) && !online.getUsername().contains(username))
            return false;

        if (ValidateUtil.isNotEmpty(srcAddr) && !srcAddr.equals(online.getSrcAddr()))
            return false;

        if (srcPort!=null&& !srcPort.equals(online.getSrcPort()))
            return false;

        if (ValidateUtil.isNotEmpty(dstAddr) && !dstAddr.equals(online.getDstAddr()))
            return false;

        if (dstPort!=null&& !dstPort.equals(online.getDstPort()))
            return false;

        return true;
    }

    /**
     * 查询会会话列表
     * @param pos
     * @param count
     * @param username
     * @param srcAddr
     * @param srcPort
     * @param dstAddr
     * @param dstPort
     * @param sort
     * @return
     */
    @Override
    public PageResult<SocksSession> querySessionPage(int pos, int count, String username, String srcAddr, Integer srcPort, String dstAddr,
                                                     Integer dstPort, String sort){
        int total = 0;
        int start = pos+1;
        int end = pos +  count ;
        List<SocksSession> copyList = new ArrayList<>(cacheData.values());
        List<SocksSession> onlineList = new ArrayList<>();
        for (SocksSession session : copyList) {
            if (!this.filterSession(session, username, srcAddr,srcPort, dstAddr, dstPort)) {
                continue;
            }
            total++;
            if (total >= start && total <= end) {
                try {
                    onlineList.add(session.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        return new PageResult<>(pos, total, onlineList);
    }



}

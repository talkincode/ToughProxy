package org.toughproxy.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.toughproxy.common.DateTimeUtil;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.entity.User;
import org.toughproxy.mapper.UserMapper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户缓存
 */
@Service
public class UserCache {

    private final static ConcurrentHashMap<String,CacheObject> cacheData = new ConcurrentHashMap<String,CacheObject>();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Memarylogger logger;

    public ConcurrentHashMap<String,CacheObject> getCacheData(){
        return cacheData;
    }

    public int size()
    {
        return cacheData.size();
    }

    /**
     *  获取缓存用户
     * @param username
     * @return
     */
    public User findGUser(String username){
        if(ValidateUtil.isNotEmpty(username) && cacheData.containsKey(username)){
            CacheObject co = cacheData.get(username);
            return co.getUser();
        }

        User subs = userMapper.findGUser(username);
        if(subs!=null){
            cacheData.put(username, new CacheObject(username, subs));
        }
        return subs;
    }

    /**
     *  是否存在缓存用户
     * @param username
     * @return
     */
    public boolean exists(String username){
        return ValidateUtil.isNotEmpty(username) && cacheData.containsKey(username);
    }

    public void remove(String username){
        cacheData.remove(username);
    }

    protected void reloadUser(String username){
        User subs = userMapper.findGUser(username);
        if(subs!=null){
            synchronized (cacheData)
            {
                if(cacheData.containsKey(username)){
                    CacheObject co = cacheData.get(username);
                    co.setUser(subs);
                }else{
                    cacheData.put(username, new CacheObject(username, subs));
                }
            }
        }
    }

    public void  updateUserCache(){
        long start = System.currentTimeMillis();
        List<User> subslist = userMapper.findLastUpdateUser(DateTimeUtil.getPreviousDateTimeBySecondString(300));
        int count = 0;
        for(User subs : subslist){
            String username = subs.getUsername();
            UserCache.CacheObject co = getCacheData().get(username);
            User cacheUser = co!=null?co.getUser():null;
            if(cacheUser!=null && DateTimeUtil.compareSecond(cacheUser.getUpdateTime(), subs.getUpdateTime()) == 0 ){
                continue;
            }
            count ++;
            reloadUser(username);
            if(count % 1000 == 0){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
        }
        logger.print(String.format("update user total = %s, cast %s ms ", count, System.currentTimeMillis()-start));
    }



    class CacheObject {

        private String key;
        private User user;
        private long lastUpdate;

        public CacheObject(String key, User user) {
            this.key = key;
            this.user = user;
            this.setLastUpdate(System.currentTimeMillis());
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
            this.setLastUpdate(System.currentTimeMillis());
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

}

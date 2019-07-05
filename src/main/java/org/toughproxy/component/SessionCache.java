package org.toughproxy.component;

import org.toughproxy.common.PageResult;
import org.toughproxy.entity.SocksSession;

import java.net.InetSocketAddress;
import java.util.Map;

public interface SessionCache {
    Map<String, SocksSession> getCacheData();

    void setUsername(String key, String username);

    String getUsername(String key);

    void addSession(SocksSession session);

    SocksSession remove(String key);

    void updateUpBytes(String key, Long bytes);

    void updateDownBytes(String key, Long bytes);

    void updateUpBytes(InetSocketAddress clientaddr, Long bytes);

    void updateDownBytes(InetSocketAddress clientaddr, Long bytes);

    SocksSession stopSession(String key);

    SocksSession getSession(InetSocketAddress addr);

    SocksSession stopSession(InetSocketAddress addr);

    boolean isLimitOver(String userName, int limit);

    void clearExpireSession();

    PageResult<SocksSession> querySessionPage(int pos, int count, String username, String srcAddr, Integer srcPort, String dstAddr,
                                              Integer dstPort, String sort);
}

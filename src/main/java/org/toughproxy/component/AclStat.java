package org.toughproxy.component;

import org.springframework.stereotype.Component;
import org.toughproxy.common.SpinLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AclStat {

    private final static SpinLock lock = new SpinLock();
    public final static String ACL_ACCEPT = "acl_accept";
    public final static String ACL_REJECT = "acl_reject";

    private ConcurrentLinkedDeque<long[]> AclAcceptStat = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<long[]> AclRejectStat= new ConcurrentLinkedDeque<>();

    public Map getData(){
        try{
            lock.lock();
            Map data = new HashMap();
            data.put("AclAcceptStat", AclAcceptStat.toArray());
            data.put("AclRejectStat", AclRejectStat.toArray());
            return data;
        }finally {
            lock.unLock();
        }
    }

    private AtomicInteger AclAccept = new AtomicInteger(0);
    private AtomicInteger AclReject = new AtomicInteger(0);

    public void runStat(){
        try{
            lock.lock();
            long ctime =  System.currentTimeMillis();
            AclAcceptStat.addLast(new long[]{ctime,AclAccept.getAndSet(0)});
            if(AclAcceptStat.size()>720){
                AclAcceptStat.removeFirst();
            }
            AclRejectStat.addLast(new long[]{ctime,AclReject.getAndSet(0)});
            if(AclRejectStat.size()>720){
                AclRejectStat.removeFirst();
            }
        }finally {
            lock.unLock();
        }

    }

    public void incrementAclAccept(){
        AclAccept.incrementAndGet();
    }

    public void incrementAclReject(){
        AclReject.incrementAndGet();
    }


}

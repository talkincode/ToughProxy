package org.toughsocks.component;

import org.springframework.stereotype.Component;
import org.toughsocks.common.SpinLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SocksStat {

    private final static SpinLock lock = new SpinLock();

    public final static String AUTH_SUCCESS = "auth_success";
    public final static String AUTH_FAIILURE = "auth_failure";
    public final static String NOT_SUPPORT = "not_support";
    public final static String CONNECT_SUCCESS = "connect_success";
    public final static String CONNECT_FAILURE = "connect_failure";
    public final static String OTHER_ERR = "other_err";
    public final static String DROP = "drop";


    private ConcurrentLinkedDeque<long[]> AuthSuccessStat = new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> AuthFailureStat= new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> NotSupportStat = new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> ConnectSuccessStat = new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> ConnectFailureStat = new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> OtherErrStat = new ConcurrentLinkedDeque<long[]>();
    private ConcurrentLinkedDeque<long[]> DropStat = new ConcurrentLinkedDeque<long[]>();

    public Map getData(){
        try{
            lock.lock();
            Map data = new HashMap();
            data.put("AuthSuccessStat", AuthSuccessStat.toArray());
            data.put("AuthFailureStat", AuthFailureStat.toArray());
            data.put("NotSupportStat", NotSupportStat.toArray());
            data.put("ConnectSuccessStat", ConnectSuccessStat.toArray());
            data.put("ConnectFailureStat", ConnectFailureStat.toArray());
            data.put("OtherErrStat", OtherErrStat.toArray());
            data.put("DropStat", DropStat.toArray());
            return data;
        }finally {
            lock.unLock();
        }
    }

    private AtomicInteger authSuccess = new AtomicInteger(0);
    private AtomicInteger authFailure = new AtomicInteger(0);
    private AtomicInteger notSupport = new AtomicInteger(0);
    private AtomicInteger connectSuccess = new AtomicInteger(0);
    private AtomicInteger connectFailure = new AtomicInteger(0);
    private AtomicInteger othertErr = new AtomicInteger(0);
    private AtomicInteger drop = new AtomicInteger(0);

    public void runStat(){
        try{
            lock.lock();
            long ctime =  System.currentTimeMillis();
            AuthSuccessStat.addLast(new long[]{ctime,authSuccess.getAndSet(0)});
            if(AuthSuccessStat.size()>720){
                AuthSuccessStat.removeFirst();
            }
            AuthFailureStat.addLast(new long[]{ctime,authFailure.getAndSet(0)});
            if(AuthFailureStat.size()>720){
                AuthFailureStat.removeFirst();
            }
            NotSupportStat.addLast(new long[]{ctime,notSupport.getAndSet(0)});
            if(NotSupportStat.size()>720){
                NotSupportStat.removeFirst();
            }
            ConnectSuccessStat.addLast(new long[]{ctime,connectSuccess.getAndSet(0)});
            if(ConnectSuccessStat.size()>720){
                ConnectSuccessStat.removeFirst();
            }
            ConnectFailureStat.addLast(new long[]{ctime,connectFailure.getAndSet(0)});
            if(ConnectFailureStat.size()>720){
                ConnectFailureStat.removeFirst();
            }
            OtherErrStat.addLast(new long[]{ctime,othertErr.getAndSet(0)});
            if(OtherErrStat.size()>720){
                OtherErrStat.removeFirst();
            }
            DropStat.addLast(new long[]{ctime,drop.getAndSet(0)});
            if(DropStat.size()>720){
                DropStat.removeFirst();
            }
        }finally {
            lock.unLock();
        }

    }

    public void update(String type){
        switch (type) {
            case AUTH_SUCCESS:
                authSuccess.incrementAndGet();
                break;
            case AUTH_FAIILURE:
                authFailure.incrementAndGet();
                break;
            case NOT_SUPPORT:
                notSupport.incrementAndGet();
                break;
            case CONNECT_SUCCESS:
                connectSuccess.incrementAndGet();
                break;
            case CONNECT_FAILURE:
                connectFailure.incrementAndGet();
                break;
            case OTHER_ERR:
                othertErr.incrementAndGet();
                break;
            case DROP:
                drop.incrementAndGet();
                break;
        }
    }
}

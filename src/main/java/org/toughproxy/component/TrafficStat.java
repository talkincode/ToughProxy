package org.toughproxy.component;

import org.springframework.stereotype.Component;
import org.toughproxy.common.SpinLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TrafficStat {

    private final static SpinLock lock = new SpinLock();

    private AtomicLong writeTotal = new AtomicLong(0);
    private AtomicLong readTotal = new AtomicLong(0);

    private final ConcurrentLinkedDeque<long[]> writeStat = new ConcurrentLinkedDeque<long[]>();
    private final ConcurrentLinkedDeque<long[]> readStat = new ConcurrentLinkedDeque<long[]>();

    public Map getData(){
        try{
            lock.lock();
            Map data = new HashMap();
            data.put("writeStat", writeStat.toArray());
            data.put("readStat", readStat.toArray());
            return data;
        }finally {
            lock.unLock();
        }
    }

    public void updateRead(long bs){
        try{
            lock.lock();
            long ctime =  System.currentTimeMillis();
            long value = bs - readTotal.longValue();
            if(value<0){
                value = 0;
            }
            readTotal.set(bs);
            readStat.addLast(new long[]{ctime,value});
            if(readStat.size() >= 720){
                readStat.removeFirst();
            }
        }finally {
            lock.unLock();
        }
    }

    public void updateWrite(long bs){
        try{
            lock.lock();
            long ctime =  System.currentTimeMillis();
            long value = bs - writeTotal.longValue();
            if(value<0){
                value = 0;
            }
            writeTotal.set(bs);
            writeStat.addLast(new long[]{ctime,value});
            if(writeStat.size() >= 720){
                writeStat.removeFirst();
            }
        }finally {
            lock.unLock();
        }
    }

}

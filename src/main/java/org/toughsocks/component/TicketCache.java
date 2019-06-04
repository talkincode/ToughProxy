package org.toughsocks.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.toughsocks.common.DateTimeUtil;
import org.toughsocks.common.PageResult;
import org.toughsocks.common.ValidateUtil;
import org.toughsocks.config.ApplicationConfig;
import org.toughsocks.entity.SocksSession;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class TicketCache {

    private Log logger = LogFactory.getLog(TicketCache.class);
    private final static ConcurrentLinkedDeque<SocksSession> queue = new  ConcurrentLinkedDeque<>();

    @Autowired
    private ApplicationConfig applicationConfig;

    public void addTicket(SocksSession ticket)
    {
        queue.addFirst(ticket);
    }

    public void syncData(){
        try {
            List<SocksSession> logs = new ArrayList<>();
            int count = 0;
            while(queue.size() > 0 && count <= 4096){
                logs.add(queue.removeFirst());
                count++;
            }
            File logdir = new File(applicationConfig.getTicketDir());
            if(!logdir.exists()){
                logdir.mkdirs();
            }
            BufferedOutputStream out = null;
            try {
                String filename = String.format("%s/socks-ticket.%s.txt",applicationConfig.getTicketDir(), DateTimeUtil.getDateString());
                File tfile = new File(filename);
                boolean isnew = !tfile.exists();
                out = new BufferedOutputStream(new FileOutputStream(tfile, true));
                if(isnew){
                    out.write(SocksSession.getHeaderString().getBytes("utf-8"));
                    out.write("\n".getBytes());
                }
                for(SocksSession ticket : logs){
                    out.write(ticket.toString().getBytes("utf-8"));
                    out.write("\n".getBytes());
                }
            } catch (Exception e) {
                logger.error("上网日志写入出错",e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error("Sync ticket error:",e);
        }
    }

    public PageResult<SocksSession> queryTicket(int start,int count,String username,String srcAddr,Integer srcPort,
                                                String dstAddr,Integer dstPort, String startTime, String endtime) throws Exception {
        int rowNum = 0;
        if(ValidateUtil.isEmpty(startTime)){
            startTime = DateTimeUtil.getDateString()+" 00:00:00";
        }
        if(ValidateUtil.isEmpty(endtime)){
            endtime = DateTimeUtil.getDateString()+" 23:59:59";
        }

        if(startTime.length() == 16){
            startTime += ":00";
        }

        if(endtime.length() == 16){
            endtime += ":59";
        }

        if(start + count > 10000){
            throw new Exception("查询最大数量为10000");
        }
        if(!ValidateUtil.isDateTime(startTime)){
            throw new Exception("查询开始时间格式必须许为 yyyy-MM-dd HH:mm:ss");
        }
        if(!ValidateUtil.isDateTime(endtime)){
            throw new Exception("查询开始时间格式必须许为 yyyy-MM-dd HH:mm:ss");
        }

        if(DateTimeUtil.compareSecond(endtime,startTime)>(85400*30)){
            throw new Exception("查询时间跨度不能超过30天");
        }

        BufferedReader reader = null;
        String beginDay = startTime.substring(0, 10);
        String endDay = endtime.substring(0, 10);
        try
        {
            String filename = String.format("%s/socks-ticket",applicationConfig.getTicketDir());
            String currendtime = DateTimeUtil.getDateString();
            int index = 0, end = start + count;
            ArrayList<SocksSession> list = new ArrayList<>();

            boolean loop = true;
            while (beginDay.compareTo(endDay) <= 0 && loop)
            {
                String curFileName = String.format("%s.%s.txt" , filename,beginDay);

                File file = new File(curFileName);
                if (!file.exists())
                {
                    beginDay = DateTimeUtil.getNextDateString(beginDay);
                    continue;
                }

                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    SocksSession logdata = SocksSession.fromString(line);
                    if(logdata==null){
                        continue;
                    }

                    if (ValidateUtil.isNotEmpty(username) && !logdata.getUsername().contains(username))
                        continue;

                    if (ValidateUtil.isNotEmpty(srcAddr) && !srcAddr.equals(logdata.getSrcAddr()))
                        continue;

                    if (srcPort!=null&& !srcPort.equals(logdata.getSrcPort()))
                        continue;

                    if (ValidateUtil.isNotEmpty(dstAddr) && !dstAddr.equals(logdata.getDstAddr()))
                        continue;

                    if (dstPort!=null&& !dstPort.equals(logdata.getDstPort()))
                        continue;

                    if(DateTimeUtil.compareSecond(logdata.getStartTime(),startTime)<0){
                        continue;
                    }
                    if(DateTimeUtil.compareSecond(logdata.getStartTime(),endtime)>0){
                        continue;
                    }

                    index++;
                    if (index >= start && index <= end){
                        if(list.size()<count){
                            list.add(logdata);
                        }
                    }
                    rowNum ++;
                    if(rowNum>=10000){
                        loop = false;
                        break;
                    }
                }

                beginDay = DateTimeUtil.getNextDateString(beginDay);
                reader.close();
            }
            return new PageResult<SocksSession>(start, rowNum, list);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception("查询上网记录失败",e);
        }
        finally
        {
            if (reader != null)
                try{reader.close();}catch(Exception ignore){}

            reader = null;
        }

    }



}

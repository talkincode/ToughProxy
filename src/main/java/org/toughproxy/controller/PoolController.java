package org.toughproxy.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.toughproxy.common.*;
import org.toughproxy.common.shell.ExecuteResult;
import org.toughproxy.common.shell.LocalCommandExecutor;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.entity.TsPppItem;
import org.toughproxy.mapper.TsPppItemMapper;

import java.util.List;

@Controller
public class PoolController {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    private TsPppItemMapper tsPppItemMapper;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private LocalCommandExecutor localCommandExecutor;

    @Autowired
    private ThreadPoolTaskExecutor systaskExecutor;

    @GetMapping(value = {"/api/ippool/query","/admin/ippool/query"})
    @ResponseBody
    public PageResult<TsPppItem> queryUser(@RequestParam(defaultValue = "0") int start,
                                           @RequestParam(defaultValue = "40") int count,
                                           String lastDia, Integer timeType, String poolname, String ipaddr){
        if(ValidateUtil.isNotEmpty(lastDia)&&lastDia.length() == 16){
            lastDia += ":00";
        }

        int page = start / count;
        Page<Object> objects = PageHelper.startPage(page + 1, count);
        PageResult<TsPppItem> result = new PageResult<>(0,0,null);
        try{
            TsPppItem query = new TsPppItem();

            if(ValidateUtil.isNotEmpty(poolname))
                query.setPoolname(poolname);
            if(timeType>0)
                query.setTimeType(timeType);
            if(ValidateUtil.isNotEmpty(ipaddr))
                query.setIpaddr(ipaddr);
            if(ValidateUtil.isNotEmpty(lastDia))
                query.setLastDia(DateTimeUtil.toTimestamp(lastDia));

            List<TsPppItem> data = tsPppItemMapper.findByAll(query);
            return new PageResult<>(start,(int) objects.getTotal(), data);

        }catch(Exception e){
            logger.error("query ippool error",e, Memarylogger.SYSTEM);
        }
        return result;
    }


    @GetMapping(value = {"/api/ippool/delete","/admin/ippool/delete"})
    @ResponseBody
    public RestResult delete(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                TsPppItemMapper mapper = sqlSession.getMapper(TsPppItemMapper.class);
                for (String id : ids.split(",") ) {
                    mapper.deleteByPrimaryKey(Long.valueOf(id));
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量删除ip失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("删除ip失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"删除ip失败");
        }
    }

    @GetMapping(value = {"/api/ippool/setlong","/admin/ippool/setlong"})
    @ResponseBody
    public RestResult setLong(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                TsPppItemMapper mapper = sqlSession.getMapper(TsPppItemMapper.class);
                for (String id : ids.split(",") ) {
                    TsPppItem item = new TsPppItem();
                    item.setId(Long.valueOf(id));
                    item.setTimeType(2);
                    mapper.updateByPrimaryKey(item);
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量 setLong 失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("setLong 失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"setLong 失败");
        }
    }

    @GetMapping(value = {"/api/ippool/setshort","/admin/ippool/setshort"})
    @ResponseBody
    public RestResult setShort(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                TsPppItemMapper mapper = sqlSession.getMapper(TsPppItemMapper.class);
                for (String id : ids.split(",") ) {
                    TsPppItem item = new TsPppItem();
                    item.setId(Long.valueOf(id));
                    item.setTimeType(1);
                    mapper.updateByPrimaryKey(item);
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量 setShort 失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("setShort 失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"setShort 失败");
        }
    }

    @GetMapping(value = {"/api/ippool/redial","/admin/ippool/redial"})
    @ResponseBody
    public DeferredResult<RestResult> redia(String names){
        DeferredResult<RestResult> deferredResult = new DeferredResult<>(120*1000L);
        try{
            systaskExecutor.execute(()->{
                ExecuteResult ert = localCommandExecutor.executeCommand("/usr/local/bin/rediappp --names "+names,120*1000);
                RestResult result = new RestResult(ert.getExitCode(), ert.getExecuteOut());
                deferredResult.setResult(result);
            });
            return deferredResult;
        }catch(Exception e){
            logger.error("重拨失败",e, Memarylogger.SYSTEM);
            deferredResult.setResult(new RestResult(1,"重拨失败"));
            return deferredResult;
        }
    }


    @GetMapping(value = {"/api/ippool/redialall","/admin/ippool/redialall"})
    @ResponseBody
    public DeferredResult<RestResult> redialAll(){
        DeferredResult<RestResult> deferredResult = new DeferredResult<>(300*1000L);
        try{
            systaskExecutor.execute(()->{
                ExecuteResult ert = localCommandExecutor.executeCommand("/usr/local/bin/rediappp --all",300*1000);
                RestResult result = new RestResult(ert.getExitCode(), ert.getExecuteOut());
                deferredResult.setResult(result);
            });
            return deferredResult;
        }catch(Exception e){
            logger.error("重拨失败",e, Memarylogger.SYSTEM);
            deferredResult.setResult(new RestResult(1,"重拨失败"));
            return deferredResult;
        }
    }


}


package org.toughproxy.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.toughproxy.common.CoderUtil;
import org.toughproxy.common.PageResult;
import org.toughproxy.common.RestResult;
import org.toughproxy.component.Memarylogger;
import org.toughproxy.mapper.AclMapper;
import org.toughproxy.component.AclCache;
import org.toughproxy.entity.Acl;


import java.util.List;

@Controller
public class AclController {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    protected AclMapper aclMapper;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    AclCache aclCache;

    @GetMapping(value = {"/api/v6/acl/query","/admin/acl/query"})
    @ResponseBody
    public PageResult<Acl> queryAcl(@RequestParam(defaultValue = "0") int start, @RequestParam(defaultValue = "40") int count, Acl aclquery){
        int page = start / count;
        Page<Object> objects = PageHelper.startPage(page + 1, count);
        PageResult<Acl> result = new PageResult<>(0,0,null);
        try{
            if("all".equals(aclquery.getPolicy())){
                aclquery.setPolicy(null);
            }
            List<Acl> data = aclMapper.queryForList(aclquery);
            return new PageResult<>(start,(int) objects.getTotal(), data);
        }catch(Exception e){
            logger.error("query acl error",e, Memarylogger.SYSTEM);
        }
        return result;
    }


    @PostMapping(value = {"/api/v6/acl/create","/admin/acl/create"})
    @ResponseBody
    public RestResult addAcl(Acl form){
        try{
            form.setId(CoderUtil.randomLongId18());
            form.setStatus(1);
            aclMapper.insertAcl(form);
            aclCache.addAcl(form);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("创建ACL失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"创建ACL失败");
        }
    }



    @PostMapping(value = {"/api/v6/acl/update","/admin/acl/update"})
    @ResponseBody
    public RestResult updatAcl(Acl form){
        try{
            Acl acl = aclMapper.findById(form.getId());
            if(acl==null){
                return new RestResult(1,"ACL不存在");
            }
            aclMapper.updateAcl(form);
            aclCache.updateAcl(form);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新ACL失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新ACL失败");
        }
    }


    @GetMapping(value = {"/api/v6/acl/delete","/admin/acl/delete"})
    @ResponseBody
    public RestResult delete(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                AclMapper baclMapper = sqlSession.getMapper(AclMapper.class);
                for (String id : ids.split(",") ) {
                    baclMapper.deleteById(Long.valueOf(id));
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量删除ACL失败", ee,Memarylogger.SYSTEM);
            }
            for (String id : ids.split(",") ) {
                aclCache.removeAcl(Long.valueOf(id));
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("删除ACL失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"删除ACL失败");
        }
    }


}

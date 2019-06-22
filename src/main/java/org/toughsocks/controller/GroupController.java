package org.toughsocks.controller;

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
import org.toughsocks.common.*;
import org.toughsocks.component.Memarylogger;
import org.toughsocks.entity.Group;
import org.toughsocks.mapper.GroupMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class GroupController {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    protected GroupMapper groupMapper;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @GetMapping(value = {"/admin/group/options"})
    @ResponseBody
    public List<Map> optionGroup(){
        try{
            List<Group> data = groupMapper.queryForList(new Group());
            List<Map> list = data.stream().map(x -> {
                Map<String,String> m = new HashMap<>();
                m.put("id", String.valueOf(x.getId()));
                m.put("value", x.getName());
                return m;
            }).collect(Collectors.toList());
            return list;
        }catch(Exception e){
            logger.error("query group error",e, Memarylogger.SYSTEM);
            return new ArrayList<>();
        }
    }

    @GetMapping(value = {"/api/v6/group/query","/admin/group/query"})
    @ResponseBody
    public PageResult<Group> queryGroup(@RequestParam(defaultValue = "0") int start,@RequestParam(defaultValue = "40") int count,String name){
        int page = start / count;
        Page<Object> objects = PageHelper.startPage(page + 1, count);
        PageResult<Group> result = new PageResult<>(0,0,null);
        try{
            Group query = new Group();
            query.setName(name);
            List<Group> data = groupMapper.queryForList(query);
            return new PageResult<>(start,(int) objects.getTotal(), data);
        }catch(Exception e){
            logger.error("query group error",e, Memarylogger.SYSTEM);
        }
        return result;
    }


    @PostMapping(value = {"/api/v6/group/create","/admin/group/create"})
    @ResponseBody
    public RestResult addGroup(Group form){
        try{
            form.setId(CoderUtil.randomLongId18());
            form.setStatus(1);
            groupMapper.insertGroup(form);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("创建用户组失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"创建用户组失败");
        }
    }


    @PostMapping(value = {"/api/v6/group/update","/admin/group/update"})
    @ResponseBody
    public RestResult updateGroup(Group form){
        try{
            Group grp = groupMapper.findById(form.getId());
            if(grp==null){
                return new RestResult(1,"用户组不存在");
            }
            groupMapper.updateGroup(form);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新用户组失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新用户组失败");
        }
    }

    @GetMapping(value = {"/api/v6/group/delete","/admin/group/delete"})
    @ResponseBody
    public RestResult delete(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                GroupMapper bgMapper = sqlSession.getMapper(GroupMapper.class);
                for (String id : ids.split(",") ) {
                    bgMapper.deleteById(Long.valueOf(id));
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量删除用户组失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("删除用户组失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"删除用户组失败");
        }
    }


}

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
import org.toughsocks.entity.User;
import org.toughsocks.form.UserForm;
import org.toughsocks.form.UserQuery;
import org.toughsocks.mapper.GroupMapper;
import org.toughsocks.mapper.UserMapper;


import java.util.List;

@Controller
public class UserController {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    protected UserMapper userMapper;

    @Autowired
    protected GroupMapper groupMapper;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @GetMapping(value = {"/api/v6/user/query","/admin/user/query"})
    @ResponseBody
    public PageResult<User> queryUser(@RequestParam(defaultValue = "0") int start,
                                           @RequestParam(defaultValue = "40") int count,
                                           String createTime, String expireTime, Integer status, String username, String keyword){
        if(ValidateUtil.isNotEmpty(expireTime)&&expireTime.length() == 16){
            expireTime += ":00";
        }
        if(ValidateUtil.isNotEmpty(createTime)&&createTime.length() == 16){
            createTime += ":59";
        }
        int page = start / count;
        Page<Object> objects = PageHelper.startPage(page + 1, count);
        PageResult<User> result = new PageResult<>(0,0,null);
        try{
            UserQuery query = new UserQuery();
            if(ValidateUtil.isNotEmpty(expireTime))
                query.setExpireTime(DateTimeUtil.toTimestamp(expireTime));
            if(ValidateUtil.isNotEmpty(createTime))
                query.setCreateTime(DateTimeUtil.toTimestamp(createTime));
            if(status!=null)
                query.setStatus(status);
            query.setKeyword(keyword);
            query.setUsername(username);
            List<User> data = userMapper.queryForList(query);
            return new PageResult<>(start,(int) objects.getTotal(), data);

        }catch(Exception e){
            logger.error("query user error",e, Memarylogger.SYSTEM);
        }
        return result;
    }

    @GetMapping(value = {"/api/v6/user/detail","/admin/user/detail"})
    @ResponseBody
    public RestResult<User> querySubscribeDetail(Long id){
        try{
            return new RestResult<>(0,"ok",userMapper.findById(id));
        }catch(Exception e){
            logger.error("查询用户详情失败",e, Memarylogger.SYSTEM);
            return new RestResult<>(1,"查询用户详情失败");
        }
    }

    @PostMapping(value = {"/api/v6/user/create","/admin/user/create"})
    @ResponseBody
    public RestResult addSubscribe(UserForm form){
        try{
            if(userMapper.findUser(form.getUsername())!=null){
                return new RestResult(1,"用户已经存在");
            }
            Group group = groupMapper.findById(form.getGroupId());
            User user = form.getUserData();
            user.setId(CoderUtil.randomLongId());
            user.setCreateTime(DateTimeUtil.nowTimestamp());
            user.setUpdateTime(DateTimeUtil.nowTimestamp());
            user.setStatus(1);
            user.setGroupPolicy(1);
            user.setMaxSession(group.getMaxSession());
            user.setUpLimit(group.getUpLimit());
            user.setDownLimit(group.getDownLimit());
            userMapper.insertUser(user);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("创建用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"创建用户失败");
        }
    }


    @PostMapping(value = {"/api/v6/user/batchcreate","/admin/user/batchcreate"})
    @ResponseBody
    public RestResult batchAddUser(UserForm form){
        try{
            Group group = groupMapper.findById(form.getGroupId());
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                UserMapper buserMapper = sqlSession.getMapper(UserMapper.class);
                int width = String.valueOf(form.getOpenNum()).length();
                for(int i = 0;i<form.getOpenNum();i++){
                    User user = form.getUserData();
                    user.setUsername(form.getUserPrefix()+ String.format("%0"+width+"d",i+1));
                    user.setRealname(user.getUsername());
                    if(form.getRandPasswd()==1||ValidateUtil.isEmpty(form.getPassword())){
                        user.setPassword(StringUtil.getRandomDigits(6));
                    }
                    user.setCreateTime(DateTimeUtil.nowTimestamp());
                    user.setUpdateTime(DateTimeUtil.nowTimestamp());
                    user.setStatus(1);
                    user.setGroupPolicy(1);
                    user.setMaxSession(group.getMaxSession());
                    user.setUpLimit(group.getUpLimit());
                    user.setDownLimit(group.getDownLimit());
                    buserMapper.insertUser(user);
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量创建用户失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("批量创建用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"批量创建用户失败");
        }
    }

    @PostMapping(value = {"/api/v6/user/uppwd","/admin/user/uppwd"})
    @ResponseBody
    public RestResult updatePassword(UserForm form){
        try{
            if(userMapper.findById(form.getId())==null){
                return new RestResult(1,"用户不存在");
            }
            if(!form.getPassword().equals(form.getCpassword())){
                return new RestResult(1,"确认密码不符");
            }
            userMapper.updatePassword(form.getId(),form.getPassword());
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新用户密码失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新用户密码失败");
        }
    }


    @PostMapping(value = {"/api/v6/user/update","/admin/user/update"})
    @ResponseBody
    public RestResult updateUser(UserForm form){
        try{
            if(userMapper.findById(form.getId())==null){
                return new RestResult(1,"用户不存在");
            }
            User subscribe = form.getUserData();
            subscribe.setUpdateTime(DateTimeUtil.nowTimestamp());
            userMapper.updateUser(subscribe);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新用户失败");
        }
    }

    @GetMapping(value = {"/api/v6/user/delete","/admin/user/delete"})
    @ResponseBody
    public RestResult delete(String ids){
        try{
            try (SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                UserMapper buserMapper = sqlSession.getMapper(UserMapper.class);
                for (String id : ids.split(",") ) {
                    buserMapper.deleteById(Long.valueOf(id));
                }
                sqlSession.commit();
            } catch (Exception ee) {
                logger.error("批量删除用户失败", ee,Memarylogger.SYSTEM);
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("删除用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"删除用户失败");
        }
    }


}

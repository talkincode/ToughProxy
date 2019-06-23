package org.toughproxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.toughproxy.entity.User;
import org.toughproxy.form.UserQuery;

import java.util.List;

@Repository
@Mapper
public interface UserMapper {

    List<User>  queryForList(UserQuery userQuery);

    User  findById(Long id);

    User  findUser(String username);

    User  findGUser(String username);

    Integer  insertUser(User user);

    void updatePassword(Long id, String password);

    void updateUser(User subscribe);

    void deleteById(Long valueOf);

    List<User> findLastUpdateUser(String previousDateTimeBySecondString);

}

package org.toughproxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.toughproxy.entity.Group;

import java.util.List;

@Repository
@Mapper
public interface GroupMapper {

    List<Group> queryForList(Group group);

    void insertGroup(Group group);

    void updateGroup(Group group);

    void deleteById(Long id);

    Group findById(Long id);

}

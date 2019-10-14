package org.toughproxy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.toughproxy.entity.TsPppItem;

@Mapper
public interface TsPppItemMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TsPppItem record);

    int insertOrUpdate(TsPppItem record);

    int insertOrUpdateSelective(TsPppItem record);

    int insertSelective(TsPppItem record);

    TsPppItem selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TsPppItem record);

    int updateByPrimaryKey(TsPppItem record);

    int updateBatch(List<TsPppItem> list);

    int batchInsert(@Param("list") List<TsPppItem> list);

    List<TsPppItem> findByAll(TsPppItem tsPppItem);

    Long countByPoolnameAndName(@Param("poolname") String poolname, @Param("name") String name);

    TsPppItem getByPoolnameAndName(@Param("poolname") String poolname, @Param("name") String name);
}
package org.toughproxy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.toughproxy.entity.Acl;

import java.util.List;

@Repository
@Mapper
public interface AclMapper {

    Acl findById(Long id);

    Acl findByIpaddr(String ipaddr);

    List<Acl> findByPolicy(String policy);

    void insertAcl(Acl acl);

    void updateAcl(Acl acl);

    void updateAclHits(Long id);

    void deleteById(Long id);

    List<Acl> queryForList(Acl aclquery);

}

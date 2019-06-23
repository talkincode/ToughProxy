package org.toughproxy.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.toughproxy.common.ValidateUtil;
import org.toughproxy.config.Constant;
import org.toughproxy.entity.Acl;
import org.toughproxy.mapper.AclMapper;
import org.toughproxy.common.IpUtil;

import java.util.*;

@Service
public class AclCache implements Constant {

    public final static int ACCEPT = 1;
    public final static int REJECT = 0;
    private final static List<AclRule> cacheData = new ArrayList<>();
    private final static Comparator<AclRule> asccomp = Comparator.comparingInt(AclRule::getPriority);

    @Autowired
    private AclMapper aclMapper;

    @Autowired
    private Memarylogger logger;


    public int size()
    {
        return cacheData.size();
    }

    /**
     *  匹配 ACL
     * @return
     */
    public int match(String srcip, String target, String domain){
        for (Iterator<AclRule> it = cacheData.iterator(); it.hasNext();) {
            AclRule rule = it.next();
            if(rule.getStatus()!=1){
                continue;
            }
            //全部拒绝
            if(ACL_REJECT_DESC.equals(rule.policy)&& ValidateUtil.isEmpty(rule.getDomain())&&rule.srclist.length==0&&rule.targetlist.length==0){
                aclMapper.updateAclHits(rule.getId());
                return REJECT;
            }
            //全部允许
            if(ACL_ACCEPT_DESC.equals(rule.policy)&&ValidateUtil.isEmpty(rule.getDomain())&&rule.srclist.length==0&&rule.targetlist.length==0){
                aclMapper.updateAclHits(rule.getId());
                return ACCEPT;
            }

            int srcindex = Arrays.binarySearch(rule.srclist,srcip);
            boolean useDomain = ValidateUtil.isNotEmpty(rule.getDomain());
            //优先判断域名
            if(!useDomain){
                //全部拒绝
                if(ACL_REJECT_DESC.equals(rule.policy)&&rule.srclist.length==0&&rule.targetlist.length==0){
                    aclMapper.updateAclHits(rule.getId());
                    return REJECT;
                }
                //全部允许
                if(ACL_ACCEPT_DESC.equals(rule.policy)&&rule.srclist.length==0&&rule.targetlist.length==0){
                    aclMapper.updateAclHits(rule.getId());
                    return ACCEPT;
                }

                int targetindex = Arrays.binarySearch(rule.targetlist,target);
                if(targetindex<0){
                    continue;
                }

                if(ACL_REJECT_DESC.equals(rule.policy)){
                    if(rule.srclist.length==0 || srcindex>0){
                        aclMapper.updateAclHits(rule.getId());
                        return REJECT;
                    }
                }
                else if(ACL_ACCEPT_DESC.equals(rule.policy)){
                    if( rule.srclist.length==0 && srcindex > 0){
                        aclMapper.updateAclHits(rule.getId());
                        return ACCEPT;
                    }
                }
            }else{
                if(ValidateUtil.isEmpty(domain) || !domain.endsWith(rule.getDomain())){
                    continue;
                }
                if(ACL_REJECT_DESC.equals(rule.policy)){
                    if(rule.srclist.length==0 || srcindex>0){
                        aclMapper.updateAclHits(rule.getId());
                        return REJECT;
                    }
                }
                else if(ACL_ACCEPT_DESC.equals(rule.policy)){
                    if( rule.srclist.length==0||srcindex>0){
                        aclMapper.updateAclHits(rule.getId());
                        return ACCEPT;
                    }
                }
            }
        }
        return ACCEPT;
    }



    public void removeAcl(Long id){
        cacheData.removeIf(x->x.getId().equals(id));
    }


    public void addAcl(Acl acl){
        if(acl!=null){
            cacheData.add(new AclRule(acl));
            cacheData.sort(asccomp);
        }
    }

    public void updateAcl(Acl acl){
        if(acl!=null){
            cacheData.removeIf(x->x.getId().equals(acl.getId()));
            cacheData.add(new AclRule(acl));
            cacheData.sort(asccomp);
        }
    }

    public void  updateAclCache(){
        long start = System.currentTimeMillis();
        List<Acl> acls = aclMapper.queryForList(new Acl());
        int count = 0;
        synchronized (cacheData){
            cacheData.clear();
            for(Acl acl : acls){
                count ++;
                cacheData.add(new AclRule(acl));
            }
        }
        cacheData.sort(asccomp);
        logger.print(String.format("update acl total = %s, cast %s ms ", count, System.currentTimeMillis()-start));
    }


    class AclRule {
        private Long id;
        private Integer status;
        private String src;
        private String target;
        private String domain;
        private Integer priority;
        private String policy;
        private String srclist[] = new String[]{};
        private String targetlist[] = new String[]{};

        public AclRule(Acl acl) {
            this.id = acl.getId();
            this.status = acl.getStatus();
            this.src = acl.getSrc();
            this.target = acl.getTarget();
            this.domain = acl.getDomain();
            this.priority = acl.getPriority();
            this.policy = acl.getPolicy();
            loadSrcList();
            loadTagetList();
        }

        private void loadSrcList(){
            if(ValidateUtil.isEmpty(src)){
                return;
            }
            if(ValidateUtil.isIP(src)){
                this.srclist = new String[]{src};
            }else if(src.contains("/")){
                try{
                    String attrs[] = src.split("/");
                    this.srclist = IpUtil.parseIpMaskRange(attrs[0],attrs[1]).toArray(new String[]{});
                    Arrays.sort(this.srclist);
                }catch (Exception ee){
                    logger.error("parse ipaddr "+src+" error",ee,Memarylogger.ACL);
                    this.srclist = new String[]{};
                }
            }else if(src.contains("-")){
                try{
                    String attrs[] = src.split("-");
                    this.srclist = IpUtil.parseIpRange(attrs[0],attrs[1]).toArray(new String[]{});
                    Arrays.sort(this.srclist);
                }catch (Exception ee){
                    logger.error("parse ipaddr "+src+" error",ee,Memarylogger.ACL);
                    this.srclist = new String[]{};
                }
            }
        }

        private void loadTagetList(){
            if(ValidateUtil.isEmpty(target)){
                return;
            }
            if(ValidateUtil.isIP(target)){
                this.targetlist = new String[]{target};
            }else if(target.contains("/")){
                try{
                    String attrs[] = target.split("/");
                    this.targetlist = IpUtil.parseIpMaskRange(attrs[0],attrs[1]).toArray(new String[]{});
                    Arrays.sort(this.targetlist);
                }catch (Exception ee){
                    logger.error("parse ipaddr "+target+" error",ee,Memarylogger.ACL);
                    this.targetlist = new String[]{};
                }
            }else if(target.contains("-")){
                try{
                    String attrs[] = target.split("-");
                    this.targetlist = IpUtil.parseIpRange(attrs[0],attrs[1]).toArray(new String[]{});
                    Arrays.sort(this.targetlist);
                }catch (Exception ee){
                    logger.error("parse ipaddr "+target+" error",ee,Memarylogger.ACL);
                    this.targetlist = new String[]{};
                }
            }
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }


}

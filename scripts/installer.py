#!/usr/bin/env python
#coding:utf-8
from __future__ import unicode_literals
import datetime
import shutil
import os
import sys

sysctlstr = '''
net.ipv4.ip_forward=1
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_tw_recycle = 1
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_keepalive_time = 1200
net.ipv4.ip_local_port_range = 10000 65000
net.ipv4.tcp_max_syn_backlog = 8192
net.ipv4.tcp_max_tw_buckets = 5000
net.core.netdev_max_backlog = 32768
net.core.somaxconn = 32768
net.core.wmem_default = 33554432
net.core.rmem_default = 33554432
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.ipv4.tcp_synack_retries = 2
net.ipv4.tcp_syn_retries = 2
net.ipv4.tcp_wmem = 8192 436600 873200
net.ipv4.tcp_rmem  = 32768 436600 873200
net.ipv4.tcp_mem = 94500000 91500000 92700000
net.ipv4.tcp_max_orphans = 3276800
vm.overcommit_memory = 1
'''

limitstr = '''
# start upcore_limit_config

*  soft nproc 40000
*  hard nproc 40000
*  soft nofile 40000
*  hard nofile 40000

mysql  soft nproc 40000
mysql  hard nproc 40000
mysql  soft nofile 40000
mysql  hard nofile 40000

# end upcore_limit_config
'''

loginstr = '''
# start upcore_limit_config

session required /lib64/security/pam_limits.so
session required pam_limits.so

# end upcore_limit_config
'''

profilestr = 'ulimit -n 65535'


def upcore():
    # backup sysconfig
    backup_time = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
    shutil.copy('/etc/sysctl.conf','/etc/sysctl.conf.bak.%s'%backup_time)
    shutil.copy('/etc/security/limits.conf','/etc/security/limits.conf.bak.%s'%backup_time)
    shutil.copy('/etc/pam.d/login','/etc/pam.d/login.bak.%s'%backup_time)


    with open('/etc/sysctl.conf','wb') as sysfs:
        sysfs.write(sysctlstr)

    os.system("sysctl -p")

    is_limit_up = False
    with open("/etc/security/limits.conf",'rb') as limitfs:
        for line in limitfs:
            if 'start upcore_limit_config' in line:
                is_limit_up = True
                break

    with open( "/etc/security/limits.conf", 'wab' ) as limitfsa:
        limitfsa.write(limitstr)

    os.system("cat /etc/security/limits.conf")

    is_login_up = False
    with open("/etc/pam.d/login",'rb') as loginfs:
        for line in loginfs:
            if 'start upcore_limit_config' in line:
                is_login_up = True
                break

    with open( "/etc/pam.d/login", 'wab' ) as loginfsa:
        loginfsa.write(loginstr)

    os.system( "cat /etc/pam.d/login" )


def up_mariadb():
    if not os.path.exists("/etc/systemd/system/mariadb.service.d"):
        os.makedirs("/etc/systemd/system/mariadb.service.d")

    with open('/etc/systemd/system/mariadb.service.d/limits.conf','wb') as mfs:
        mfs.write("[Service]\nLimitNOFILE=65535\n")

    os.system("systemctl daemon-reload")
    os.system("cat /etc/systemd/system/mariadb.service.d/limits.conf")


def install_proxy():
    cfgparam = dict(
        server_port = raw_input('请输入web服务端口 (默认 1823)'.encode("utf-8")) or 1823,
        socks_port = raw_input('请输入socks代理端口 (默认 1808)'.encode("utf-8")) or 1808,
        http_port = raw_input('请输入http代理端口 (默认 1880)'.encode("utf-8")) or 1880,
        dbname = raw_input('请输入数据库名 (默认 toughproxy)'.encode("utf-8")) or "toughproxy",
        dbuser = raw_input('请输入数据库用户名 (默认 proxyuser)'.encode("utf-8")) or "proxyuser",
        dbpwd = raw_input('请输入数据库密码 (默认 proxypwd)'.encode("utf-8")) or "proxypwd",
        server_mode = raw_input('请输入分布式服务运行模式 (分布式模式下输入 server 或 client, 默认单机无 none)'.encode("utf-8")) or "none",
        sync_port = raw_input('请输入分布式模式同步端口（本机运行为服务端时有效） (默认 1824)'.encode("utf-8")) or 1824,
        sync_server = raw_input('请输入分布式模式同步服务主机（本机运行为客户端时有效） IP:端口  (默认无 0.0.0.0:0)'.encode("utf-8")) or "0.0.0.0:0",
    )
    config_fstr='''server.port = {server_port}
logging.config=classpath:logback-prod.xml

org.toughproxy.socks.tcpPort = {socks_port}
org.toughproxy.http.tcpPort = {http_port}

# database config
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/{dbname}?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true
spring.datasource.username={dbuser}
spring.datasource.password={dbpwd}
spring.datasource.max-active=120
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

application.ticketDir = /var/toughproxy/data/ticket
application.rmiMaster = rmi://{sync_server}/sessioncache
application.rmiPort = {sync_port}
application.rmiRole = {server_mode}'''.format(**cfgparam)

    with open('/opt/toughproxy/application-prod.properties','wb') as mfs:
        mfs.write(config_fstr)

    usememary = raw_input('请输入服务进程使用的最大内存 ( 默认 1024M )'.encode("utf-8")) or "1024M"
    service_fstr = '''[Unit]
Description=toughproxy
After=syslog.target

[Service]
WorkingDirectory=/opt/toughproxy
User=root
LimitNOFILE=65535
LimitNPROC=65535
Type=simple
ExecStart=/usr/bin/java -jar -Xms{usememary} -Xmx{usememary} /opt/toughproxy/toughproxy-latest.jar  --spring.profiles.active=prod
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target'''.format(usememary=usememary)
    with open('/usr/lib/systemd/system/toughproxy.service','wb') as mfs:
        mfs.write(service_fstr)
    os.system("systemctl enable toughproxy")
    os.system("curl -L http://115.159.56.13:8008/toughproxy/toughproxy-latest.jar -o /opt/toughproxy/toughproxy-latest.jar")

    isrun = raw_input('安装完成，是否立即启动 (y/n 默认 y)?'.encode("utf-8")) or 'y'
    if isrun == 'y':
        os.system("systemctl start toughproxy && systemctl status toughproxy")


if __name__ == "__main__":
    usage= """====================================================================================
    # 即将开始安装 toughproxy，请仔细阅读以下内容，
    
    # java 和 mariadb 需要自行安装，centos7 下 请参考如下指令：
    
    # 安装 java
    yum install -y java
    
    # 测试 java 安装是否有效
    java -version
 
    # 安装 mariadb|mysql
    yum install -y mariadb-server
    systemctl enable mariadb
    systemctl start mariadb
    
    # 初始化数据库请执行如下指令， 如需修改数据库用户名密码，
    curl -L http://115.159.56.13:8008/toughproxy/database.sql -O  /tmp/database.sql
    mysql -uroot -p -e "create database toughproxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" -v
    mysql -uroot -p -e "GRANT ALL ON toughproxy.* TO proxyuser@'127.0.0.1' IDENTIFIED BY 'proxypwd' WITH GRANT OPTION;FLUSH PRIVILEGES;" -v
    mysql -uroot -p  < /tmp/database.sql
    
    # 注意还要开启防火墙端口，实际端口根据配置确定
    firewall-cmd --zone=public --add-port=1823/tcp --permanent
    firewall-cmd --zone=public --add-port=1824/tcp --permanent
    
    # 安装完成后，如需修改程序配置， 请修改 /opt/toughproxy/application-prod.properties
===================================================================================="""
    print(usage)

    if not raw_input('是否继续安装? (y/n)?'.encode("utf-8")) == 'y':
        sys.exit(0)

    if not os.path.exists("/usr/bin/java"):
        print("请安装 java 8")
        sys.exit(0)

    if not os.path.exists("/usr/bin/mysql"):
        print("请安装数据库 mariadb 或 mysql")
        sys.exit(0)

    if raw_input('是否需要优化系统内核?(y/n 默认 n)?'.encode("utf-8")) == 'y':
        upcore()

    if raw_input('是否需要优化mysql连接数限制?(y/n 默认 n)?'.encode("utf-8")) == 'y':
        up_mariadb()

    if raw_input('是否需要增加 ulimit -n 65535 到 /etc/profile(y/n 默认 n)?'.encode("utf-8")) == 'y':
        os.system("echo 'ulimit -n 65535' >> /etc/profile")


    install_proxy()



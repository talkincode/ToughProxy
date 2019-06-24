![](http://static.toughcloud.net/toughsms/20190624230829.png)

# ToughProxy

ToughProxy 是一个代理服务器， 目标是提供一个综合性的代理服务软件，支持 socks5,socks4, http, https代理协议， 针对 Socks5 提供完善的认证机制，以及提供流量控制策略，访问控制策略。

## 基础功能清单

- Socks5 代理（支持UDP穿透）
- Socks4 代理
- Http代理
- Https代理
- Socks5 Radius 认证， 支持下发限速扩展
- Socks5 数据库认证
- 认证用户管理，批量创建用户支持，用户组支持
- 全局限速和单个连接限速
- 连接数控制
- 源地址，目标地址，域名访问控制
- 实时连接会话查询，实时流量统计
- 连接日志存档查询，可自定义保存天数
- Http APi 提供


## 快速开始

> 注意， linux 下提供了快捷安装脚本，请参考安装包内的 linux-installer.md

### 系统环境依赖

- 操作系统：支持跨平台部署 （Linux，Windows，MacOS等）
- java 版本: 1.8或更高
- 数据库服务器：MySQL/MariaDB

### 数据库初始化

> 数据库的安装配置请自行完成,首先确保你的数据库服务器已经运行

运行创建数据库脚本以及创建专用用户

    create database toughproxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    GRANT ALL ON toughproxy.* TO proxyuser@'127.0.0.1' IDENTIFIED BY 'proxypwd' WITH GRANT OPTION;FLUSH PRIVILEGES;

创建数据库表

    USE `toughproxy`;
    
    -- 表 toughproxy.ts_acl 结构
    CREATE TABLE IF NOT EXISTS `ts_acl` (
      `id` bigint(20) unsigned NOT NULL,
      `priority` int(10) unsigned NOT NULL DEFAULT '0',
      `status` int(10) unsigned NOT NULL DEFAULT '1',
      `hits` int(10) unsigned NOT NULL DEFAULT '0',
      `policy` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
      `src` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `target` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `domain` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    
    
    -- 表 toughproxy.ts_config 结构
    CREATE TABLE IF NOT EXISTS `ts_config` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
      `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
      `value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    
    -- 表 toughproxy.ts_group 结构
    CREATE TABLE IF NOT EXISTS `ts_group` (
      `id` bigint(20) unsigned DEFAULT NULL,
      `name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `status` int(10) unsigned NOT NULL DEFAULT '1',
      `up_limit` int(10) unsigned DEFAULT NULL,
      `down_limit` int(10) unsigned DEFAULT NULL,
      `max_session` int(10) unsigned DEFAULT NULL,
      `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    
    -- 表 toughproxy.ts_user 结构
    CREATE TABLE IF NOT EXISTS `ts_user` (
      `id` bigint(20) unsigned DEFAULT NULL,
      `group_id` bigint(20) unsigned DEFAULT NULL,
      `group_policy` int(10) unsigned DEFAULT NULL,
      `realname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `username` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `password` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `mobile` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `status` int(10) unsigned DEFAULT NULL,
      `up_limit` int(10) unsigned DEFAULT NULL,
      `down_limit` int(10) unsigned DEFAULT NULL,
      `max_session` int(10) unsigned DEFAULT NULL,
      `create_time` timestamp NULL DEFAULT NULL,
      `update_time` timestamp NULL DEFAULT NULL,
      `expire_time` timestamp NULL DEFAULT NULL,
      `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
    
    
            
### 运行主程序

    java -jar -Xms256M -Xmx1024M /opt/toughproxy-latest.jar  --spring.profiles.active=prod
    
> 注意 jar 文件（toughproxy-latest.jar）的路径

### Linux  systemd 服务配置

/opt/application-prod.properties

    # web访问端口
    server.port = 1823

    # 日志配置，可选 logback-prod.xml 或 logback-dev.xml， 日志目录为 /var/toughproxy/logs
    logging.config=classpath:logback-prod.xml
    
    # 数据库配置
    spring.datasource.url=jdbc:mysql://127.0.0.1:3306/toughproxy?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8
    &allowMultiQueries
    =true
    spring.datasource.username=proxyuser
    spring.datasource.password=proxypwd
    spring.datasource.max-active=120
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

/usr/lib/systemd/system/toughproxy.service

    [Unit]
    Description=toughproxy
    After=syslog.target
    
    [Service]
    WorkingDirectory=/opt
    User=root
    LimitNOFILE=65535
    LimitNPROC=65535
    Type=simple
    ExecStart=/usr/bin/java -server -jar -Xms256M -Xmx1024M /opt/toughproxy-latest.jar  --spring.profiles.active=prod
    SuccessExitStatus=143
    
    [Install]
    WantedBy=multi-user.target

> 如果了解 spring systemd和配置原理，可以根据自己的实际需要进行修改

通过以下指令启动服务

    systemctl enable toughproxy
    systemctl start toughproxy
    
## 意见反馈

如您有好的建议想法， 请加入 QQ 群 247860313 交流
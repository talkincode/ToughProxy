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
  `max_client` int(10) unsigned DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `expire_time` timestamp NULL DEFAULT NULL,
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ts_ipaddr_stat(
	id BIGINT (15) NOT NULL AUTO_INCREMENT COMMENT 'id',
	ipaddr VARCHAR (50) NOT NULL DEFAULT '' COMMENT '本地拨号获取IP',
	pick_times INT (11) NOT NULL DEFAULT 0 COMMENT 'ip提取次数',
	begin_time TIMESTAMP UNIQUE NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '开始时间',
	end_time TIMESTAMP UNIQUE NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '结束时间',
	PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ts_ppp_item(
  id BIGINT (15) NOT NULL AUTO_INCREMENT COMMENT '拨号ID',
  poolname VARCHAR (50) NOT NULL DEFAULT '' COMMENT 'ip池名称',
  `name` VARCHAR (50) NOT NULL DEFAULT '' COMMENT 'ppp 名称',
  ipaddr VARCHAR (50) DEFAULT '' COMMENT '本地拨号获取IP',
  peer VARCHAR (50) DEFAULT '' COMMENT '对端地址',
  time_type INT (11) NOT NULL DEFAULT 0 COMMENT 'IP时间类型 1 短效IP， 2 长效IP',
  last_dia TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后拨号时间',
  dia_times INT (11) NOT NULL DEFAULT 0 COMMENT '拨号次数',
  UNIQUE `ux_server_name`(server,name),
  PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT COLLATE=utf8mb4_unicode_ci;


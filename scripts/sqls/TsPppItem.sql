-- auto Generated on 2019-09-28
-- DROP TABLE IF EXISTS ts_ppp_item;
CREATE TABLE ts_ppp_item(
                          id BIGINT (15) NOT NULL AUTO_INCREMENT COMMENT '拨号ID',
                          server VARCHAR (50) NOT NULL DEFAULT '' COMMENT '服务器名称',
                          `name` VARCHAR (50) NOT NULL DEFAULT '' COMMENT 'ppp 名称',
                          ipaddr VARCHAR (50) DEFAULT '' COMMENT '本地拨号获取IP',
                          peer VARCHAR (50) DEFAULT '' COMMENT '对端地址',
                          time_type INT (11) NOT NULL DEFAULT 0 COMMENT 'IP时间类型 1 短效IP， 2 长效IP',
                          last_dia TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后拨号时间',
                          dia_times INT (11) NOT NULL DEFAULT 0 COMMENT '拨号次数',
                          UNIQUE `ux_server_name`(server,name),
                          PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'ts_ppp_item';

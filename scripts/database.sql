create table if not exists ts_config
(
	id bigint auto_increment primary key,
	type varchar(32) not null,
	name varchar(128) not null,
	value varchar(255) null,
	remark varchar(255) null
);


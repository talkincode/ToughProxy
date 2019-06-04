create database toughsocks DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL ON toughsocks.* TO socksuser@'127.0.0.1' IDENTIFIED BY 'sockspwd' WITH GRANT OPTION;FLUSH PRIVILEGES;
# GRANT ALL ON toughsocks.* TO socksuser@'%' IDENTIFIED BY 'sockspwd' WITH GRANT OPTION;FLUSH PRIVILEGES;
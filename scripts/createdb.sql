create database toughproxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL ON toughproxy.* TO proxyuser@'127.0.0.1' IDENTIFIED BY 'proxypwd' WITH GRANT OPTION;FLUSH PRIVILEGES;
# GRANT ALL ON toughproxy.* TO socksuser@'%' IDENTIFIED BY 'sockspwd' WITH GRANT OPTION;FLUSH PRIVILEGES;
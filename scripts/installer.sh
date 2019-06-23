#!/bin/sh

install_package()
{
    \cp application-prod.properties /opt/application-prod.properties
    \cp toughproxy-latest.jar /opt/toughproxy-latest.jar
    \cp toughproxy.service /usr/lib/systemd/system/toughproxy.service
    \cp -r portal /opt/
    systemctl enable toughproxy
    echo "install done, please exec systenctl start toughproxy after initdb"
}

setup_mysql()
{
    echo "create database toughproxy"
    mysql -uroot -p -e "create database toughproxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "GRANT db user"
    mysql -uroot -p -e "GRANT ALL ON toughproxy.* TO socksuser@'127.0.0.1' IDENTIFIED BY 'sockspwd' WITH GRANT OPTION;FLUSH PRIVILEGES;" -v
    echo "create tables"
    mysql -uroot -p  < database.sql
}

case "$1" in

  initdb)
    setup_mysql
  ;;

  install)
    install_package
  ;;

esac
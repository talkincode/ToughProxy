#!/bin/sh
currdir=$PWD
version="v1.0.0.1"
releasedir=${currdir}/release/toughproxy-${version}
releasefile=toughproxy-${version}.zip


build_version()
{
    echo "release version ${version}"
    test -d ${releasedir} || mkdir ${releasedir}
    rm -fr ${releasedir}/*
    test -f ${releasefile} && rm -f ${releasefile}

    cp ${currdir}/scripts/application-prod.properties ${releasedir}/application-prod.properties
    cp ${currdir}/scripts/createdb.sql ${releasedir}/createdb.sql
    cp ${currdir}/scripts/database.sql ${releasedir}/database.sql
    cp ${currdir}/scripts/installer.sh ${releasedir}/installer.sh
    cp ${currdir}/scripts/toughproxy.service ${releasedir}/toughproxy.service
    cp ${currdir}/scripts/linux-installer.md ${releasedir}/linux-installer.md
    cp ${currdir}/README.md ${releasedir}/README.md
    dos2unix ${releasedir}/*.properties
    dos2unix ${releasedir}/*.sql
    dos2unix ${releasedir}/*.sh
    dos2unix ${releasedir}/*.service
    cp ${currdir}/scripts/startup.bat ${releasedir}/startup.bat
    cp ${currdir}/target/toughproxy-latest.jar ${releasedir}/toughproxy-latest.jar
    cd ${currdir}/release && zip -r ${releasefile} toughproxy-${version}
    echo "release file ${releasefile}"
}


case "$1" in

  build)
    build_version
  ;;

esac
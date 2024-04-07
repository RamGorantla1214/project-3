#!/bin/bash

echo "";
echo "88                                                           88                       88  88";
echo "88                                                           88                       88  88         ,d";
echo "88                                                           88                       88  88         88";
echo "88,dPPYba,    ,adPPYba,  ,adPPYYba,  8b,dPPYba,   ,adPPYba,  88,dPPYba,    ,adPPYba,  88  88       MM88MMM   ,adPPYba,   8b,dPPYba,";
echo '88P     "8a  a8P_____88  ""      Y8  88P     "8a  I8[    ""  88P    "8a   a8P_____88  88  88         88     a8"     "8a  88P    "8a';
echo '88       d8  8PP"""""""  ,adPPPPP88  88       88   `"Y8ba,   88       88  8PP"""""""  88  88         88     8b       d8  88       d8';
echo '88b,   ,a8   "8b,   ,aa  88,    ,88  88       88  aa    ]8I  88       88  "8b,   ,aa  88  88  888    88,    "8a,   ,a8"  88b,   ,a8"';
echo '8Y"Ybbd8"     "Ybbd8"     "8bbdP"Y8  88       88   "YbbdP"   88       88   "Ybbd8""   88  88  888    "Y888    "YbbdP"    88`YbbdP"';
echo '                                                                                                                         88';
echo '                                                                                                                         88';
echo '                                                [Write less,time more.]';
echo "";
echo "                                            Copyright@2021 www.beanshell.top";
echo "";

OPERATE=$1
APP_NAME=$2

SYSTEM=$(uname)
RUNNING="false"
SHELL_PATH=$(cd $(dirname "$0") && pwd)
BASE_PATH=$(dirname ${SHELL_PATH})
LOG_PATH="${BASE_PATH}/logs"
CONF_PATH="${BASE_PATH}/conf"
PROGRAM_NAME="*.jar"
PROGRAM_PATH="${BASE_PATH}/${PROGRAM_NAME}"
CONF_FILE_PATH="${CONF_PATH}/application-*.properties"
HEAP_DUMP_PATH="${BASE_PATH}/heapdump"
START_TIME=$(date +%Y%m%d%H%M%S)

JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
MEM_OPTS=" -server -Xms1G -Xmx1G -XX:MaxNewSize=512m -Xmn256m -Xss256k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${HEAP_DUMP_PATH}/heapdump-${START_TIME}.hprof"

if [ -z "${APP_NAME}" ]; then
  APP_NAME=$(ls ${PROGRAM_PATH})
  echo "APP_NAME=${APP_NAME}"
fi
# 检查日志路径是否存在
if [ ! -d "${LOG_PATH}" ]; then
  mkdir "${LOG_PATH}"
fi

# 检查heapdump路径是否存在
if [ ! -d "${HEAP_DUMP_PATH}" ]; then
  mkdir "${HEAP_DUMP_PATH}"
fi

SERVER_NAME=`sed '/^ram.application.name=/!d;s/.*=//' $CONF_FILE_PATH`
SERVER_PORT=`sed '/^ram.application.http.port=/!d;s/.*=//' $CONF_FILE_PATH`

echo "App name is ${SERVER_NAME}"
echo "App http port is ${SERVER_PORT}"


# 检查应用是否在运行
function check() {
  echo "check running status..."
  if [ "${SYSTEM}" == 'Linux' ]; then
    tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $2}')
  else
    tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $3}')
  fi
  if [ "${tpid}" ]; then
    echo "App is running. pid=${tpid}"
    RUNNING="true"
  else
    echo "App is down."
    RUNNING="false"
  fi
}

# 启动程序
function start() {
  # 检查是否已经存在
  check
  if [ ${RUNNING} == "true" ]; then
    echo "App already running!"
  else
    echo -e "Start...\c"
    nohup java -Dloader.path=file://${CONF_PATH} ${JAVA_OPTS} ${MEM_OPTS} -jar ${PROGRAM_PATH} >${LOG_PATH}/console.log 2>&1 &
    count=0
    wait_time=0;
    while [ ${count} -lt 1 ]; do
      echo -e ".\c"
      sleep 1
      count=$(netstat -an | grep "${SERVER_PORT}" | wc -l)
      echo -e "${count}\c"
      if [ ${count} -gt 0 ]; then
        break
      fi
      ((wait_time++));
      if [ ${wait_time} -eq 60 ]; then
          echo "App start over 60 seconds, it may fail to bootstrap. Please check the logs for more information."
          break
      fi
    done
    if [ ${count} -ge 1 ]; then
      APP_PID=$(ps -ef | grep "${BASE_PATH}" | grep -v "grep" | awk '{print $2}')
      echo "."
      START_END_TIME=$(date +%Y%m%d%H%M%S)
      START_TOTAL_TIME=$(($START_END_TIME-$START_TIME))
      echo "Pid is ${APP_PID}"
      echo "The program started successfully, taking ${START_TOTAL_TIME} seconds."
    fi
  fi
}

# 停止程序
function stop() {
  STOP_BEGIN_TIME=$(date +%Y%m%d%H%M%S)
  if [ "${SYSTEM}" == 'Linux' ]; then
    tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $2}')
  else
    tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $3}')
  fi
  if [ "${tpid}" ]; then
    echo -e 'Stop Process...\c'
    kill -15 "${tpid}"
    # 检查程序是否停止成功, 等待120s
    for ((i = 0; i < 120; ++i)); do
      sleep 1
      if [ "${SYSTEM}" == 'Linux' ]; then
        tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $2}')
      else
        tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $3}')
      fi
      if [ "${tpid}" ]; then
        echo -e ".\c"
      else
        echo "."
        STOP_END_TIME=$(date +%Y%m%d%H%M%S)
        STOP_TOTAL_TIME=$(($STOP_END_TIME-STOP_BEGIN_TIME))
        echo "Stop Successfully! Taking ${STOP_TOTAL_TIME} seconds."
        break
      fi
    done
    # 超过120s 无法正常关闭程序，则强制杀死进程
    if [ "${SYSTEM}" == 'Linux' ]; then
      tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $2}')
    else
      tpid=$(ps -ef | grep "${APP_NAME}" | grep -v grep | grep -v kill | awk '{print $3}')
    fi
    if [ "${tpid}" ]; then
      STOP_END_TIME=$(date +%Y%m%d%H%M%S)
      STOP_TOTAL_TIME=$(($STOP_END_TIME-STOP_BEGIN_TIME))
      echo "Normal kill failed. Waiting ${STOP_TOTAL_TIME} seconds. Now execute forcing kill..."
      kill -9 "${tpid}"
    fi
  else
    echo 'App already stop!'
  fi
}

# 重启
function restart() {
  stop
  check
  if [ ${RUNNING} == "false" ]; then
    start
  else
    echo "start failed..."
  fi
}

function displayHelp() {
  echo "This script just support the command under the table:"
  echo "---------------------------------------------------"
  echo "| start   |  Start the app using default config.  |"
  echo "---------------------------------------------------"
  echo "| stop    |  Stop the app.                        |"
  echo "---------------------------------------------------"
  echo "| check   |  Check the app if running or not.     |"
  echo "---------------------------------------------------"
  echo "| restart |  Just reboot the app.                 |"
  echo "---------------------------------------------------"
  echo "| help    |  Display app operation manual.        |"
  echo "---------------------------------------------------"
  echo "Usage: app.sh start/stop/check/restart/help"
}

# 参数检查
if [ -z "${OPERATE}" ] || [ -z "${APP_NAME}" ]; then
  if [ -z "${OPERATE}" ]; then
    displayHelp
  else
    echo "APP_NAME can not be null."
  fi
else
  # 启动程序
  if [ "${OPERATE}" == "start" ]; then
    start
  # 停止程序
  elif [ "${OPERATE}" == "stop" ]; then
    stop
  # 检查查询运行状态
  elif [ "${OPERATE}" == "check" ]; then
    check
  # 重启程序
  elif [ "${OPERATE}" == "restart" ]; then
    restart
  elif [ "${OPERATE}" == "help" ]; then
    displayHelp
  else
    echo "Unsupported operation. Just type 'help' to display what operation is supported."
  fi
fi

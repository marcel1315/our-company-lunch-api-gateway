#!/bin/bash

WORK_DIR=$(pwd)
CURRENT_DATE=$(date +%Y%m%d%H%M%S)

# AWS config
export AWS_REGION="ap-northeast-2"

# Run jar
DEPLOY_PATH="${WORK_DIR}/deploy_${CURRENT_DATE}"
DEPLOY_LOG_PATH="${DEPLOY_PATH}/deploy.log"
APPLICATION_LOG_PATH="${DEPLOY_PATH}/application.log"

JAR_FILE="$WORK_DIR/build/libs/*.jar" # Expects one jar file exists.
JAR_PATH=$(ls $JAR_FILE)
JAR_NAME=$(basename $JAR_PATH)

JAVA_OPTS=""

mkdir -p $DEPLOY_PATH # Create the directory
touch $DEPLOY_LOG_PATH

echo "> Build filename: $JAR_NAME" > $DEPLOY_LOG_PATH

echo "> Copy build file" >> $DEPLOY_LOG_PATH

cp $JAR_PATH $DEPLOY_PATH

echo "> Get application PID if there is one running" >> $DEPLOY_LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> No application running" >> $DEPLOY_LOG_PATH
else
    echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH
    kill -9 $CURRENT_PID
    sleep 5
fi

JAR_PATH=$DEPLOY_PATH/$JAR_NAME
echo "> JAR_PATH $JAR_PATH" >> $DEPLOY_LOG_PATH
echo "> JAVA_OPTS $JAVA_OPTS" >> $DEPLOY_LOG_PATH

# To set java
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

nohup java $JAVA_OPTS -jar $JAR_PATH 1> $APPLICATION_LOG_PATH 2> $DEPLOY_LOG_PATH &

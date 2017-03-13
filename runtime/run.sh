#!/bin/bash

cd /home/pi/settop

#export JAVA_HOME=/home/pi/lib/java
#export PATH=$JAVA_HOME/bin:$PATH:

export CLASSPATH=.:./bin:$CLASSPATH:
export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar:

export CLASSPATH=$CLASSPATH:./lib/aopalliance-1.0.jar:
export CLASSPATH=$CLASSPATH:./lib/commons-codec-1.9.jar:
export CLASSPATH=$CLASSPATH:./lib/commons-httpclient-3.1.jar:
export CLASSPATH=$CLASSPATH:./lib/commons-lang-2.6.jar:
export CLASSPATH=$CLASSPATH:./lib/commons-logging-1.2.jar:
export CLASSPATH=$CLASSPATH:./lib/fluent-hc-4.5.2.jar:
export CLASSPATH=$CLASSPATH:./lib/httpclient-4.5.2.jar:
export CLASSPATH=$CLASSPATH:./lib/httpclient-cache-4.5.2.jar:
export CLASSPATH=$CLASSPATH:./lib/httpclient-win-4.5.2.jar:
export CLASSPATH=$CLASSPATH:./lib/httpasyncclient-4.1.jar:
export CLASSPATH=$CLASSPATH:./lib/httpcore-nio-4.4.1.jar:
export CLASSPATH=$CLASSPATH:./lib/httpcore-4.4.4.jar:
export CLASSPATH=$CLASSPATH:./lib/httpmime-4.5.2.jar:
export CLASSPATH=$CLASSPATH:./lib/jl1.0.1.jar:
export CLASSPATH=$CLASSPATH:./lib/jna-4.1.0.jar:
export CLASSPATH=$CLASSPATH:./lib/jna-platform-4.1.0.jar:
export CLASSPATH=$CLASSPATH:./lib/json-simple-1.1.jar:
export CLASSPATH=$CLASSPATH:./lib/basicplayer-3.0.0.0.jar:
export CLASSPATH=$CLASSPATH:./lib/mp3plugin.jar:
export CLASSPATH=$CLASSPATH:./lib/vorbisspi1.0.2.jar:
export CLASSPATH=$CLASSPATH:./lib/tritonus-share-0.3.7.4.jar:
export CLASSPATH=$CLASSPATH:./lib/gson-2.8.0.jar:
export CLASSPATH=$CLASSPATH:./lib/mp3spi-1.9.5.4.jar:
export CLASSPATH=$CLASSPATH:./lib/kj_dsp1.1.jar:
export CLASSPATH=$CLASSPATH:./lib/jspeex0.9.7.jar:
export CLASSPATH=$CLASSPATH:./lib/jorbis-0.0.15.jar:
export CLASSPATH=$CLASSPATH:./lib/jogg-0.0.7.jar:
export CLASSPATH=$CLASSPATH:./lib/jmactritonusspi1.74.jar:
export CLASSPATH=$CLASSPATH:./lib/jflac-1.2.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-web-2.5.6.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-test-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-expression-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-core-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-context-support-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-context-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-beans-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-aspects-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/spring-aop-4.2.8.RELEASE.jar:
export CLASSPATH=$CLASSPATH:./lib/logback-core-1.1.9.jar:
export CLASSPATH=$CLASSPATH:./lib/logback-classic-1.1.9.jar:
export CLASSPATH=$CLASSPATH:./lib/logback-access-1.1.9.jar:
export CLASSPATH=$CLASSPATH:./lib/slf4j-api-1.7.7.jar:
export CLASSPATH=$CLASSPATH:./lib/log4j-1.2.15.jar:
export CLASSPATH=$CLASSPATH:./lib/junit-4.7.jar:
export CLASSPATH=$CLASSPATH:./lib/jsoup-1.7.3.jar:
export CLASSPATH=$CLASSPATH:./lib/json-simple-1.1.jar:
export CLASSPATH=$CLASSPATH:./lib/jlayer-1.0.1.4.jar:

export CLASSPATH=$CLASSPATH:./lib/shopngenie-linux-1.0.0.jar:

#javac -d ./src *.java

#java -jar cvf ./src

java -Duser.timezone=GMT+09:00 com.genie.shop.PlayerMain b2btest qwer1010!



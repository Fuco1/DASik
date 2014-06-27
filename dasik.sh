#!/bin/bash

cp="lib/log4j-1.2.16.jar"
cp="${cp}:lib/dom4j-2.0.0-ALPHA-2.jar"
cp="${cp}:lib/jaxen-1.1.3.jar"
cp="${cp}:lib/jsoup-1.6.0.jar"
cp="${cp}:lib/pircbot.jar"
cp="${cp}:lib/mysql-connector-java-5.1.6-bin.jar"
cp="${cp}:lib/org.springframework.transaction-3.1.0.M1.jar"
cp="${cp}:lib/org.springframework.jdbc-3.1.0.M1.jar"
cp="${cp}:lib/org.springframework.core-3.1.0.M1.jar"
cp="${cp}:lib/org.springframework.beans-3.1.0.M1.jar"
cp="${cp}:lib/commons-logging-1.1.1.jar"
cp="${cp}:lib/groovy-all-1.8.0.jar"
cp="${cp}:build/classes"

java -cp $cp -Djava.net.preferIPv4Stack=true -Djava.security.manager -Djava.security.policy=dasik.policy cz.dasnet.dasik.Dasik
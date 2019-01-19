#!/bin/sh

# Kill the current running version of Wyatt
curl http://www.mtheory7.com:17071/seppuku?pass={SECRET_HASH256}

# Remove old Wyatt logs
rm nohup.out
rm logs/*.log*

# Update the binance-java-api package
cd binance-java-api
git pull
mvn clean install
# mvn clean install -DskipTests
cd ..

# Update Wyatt's code and rebuild
cd wyatt
git pull
mvn clean install
cd ..

# Print out updated line count for Wyatt's code
cloc wyatt

# Start new Wyatt instance so even Control+C doesn't kill the running instance
nohup java -jar wyatt/target/wyatt*.jar <arg1> <..> <arg6> &

# USEFULL COMMANDS
# =======================================
# ps aux | grep -i wyatt   # This command finds a running instance of Wyatt
# sudo kill -9 {PID}       # Kills running Wyatt instance

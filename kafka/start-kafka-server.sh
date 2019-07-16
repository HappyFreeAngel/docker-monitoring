#!/usr/bin/env bash
bin/kafka-server-stop.sh
nohup bin/zookeeper-server-start.sh config/zookeeper.properties  &

sleep 5

nohup bin/kafka-server-start.sh config/server.properties &

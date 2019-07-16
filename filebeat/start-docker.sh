#!/usr/bin/env bash
if [ ! -d "/tmp/docker_container" ];then
    mkdir -p /tmp/docker_container
    chmod -R 777 /tmp/docker_container
fi
docker stop filebeat
docker rm  filebeat

docker run \
-d \
--name filebeat \
-v `pwd`/filebeat.yml:/usr/share/filebeat/filebeat.yml:ro \
-v /tmp/docker_container:/usr/share/filebeat/data:rw \
nexus.linkaixin.com:2443/docker.elastic.co/beats/filebeat:7.2.0
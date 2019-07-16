# docker容器实时监控

#使用方法 
# 前提条件: 1. 有一台以上的机器上安装了docker 环境，运行着docker,  机器系统为Linux 系统，默认是Centos7
#          2. 可以ssh 免密码登录远程的docker 机器,支持bash shell
#
#
#          3.有一台机器，支持ssh, （可以是和1同一台机器)


#使用方法:  
第一步: 启动监控ETL远程采集工具
cd docker-monitor-shell
./1.sh
执行里面的脚本, 参数自己根据环境修改
#!/usr/bin/env bash
#先删除之前的文件夹，如果有的话
`rm -rf /tmp/docker_container`
if [ ! -d "/tmp/docker_container" ];then
    mkdir -p /tmp/docker_container
    chmod -R 777 /tmp/docker_container
fi
touch /tmp/docker_container/docker_container1.log
ssh root@主机IP " docker stats  --format ',#ID#: #{{ .ID }}#,#name#: #{{.Name}}#,#cpu#: #{{ .CPUPerc }}#,#memory#: {#raw#: #{{ .MemUsage }}#,#percent#: #{{ .MemPerc }}#},#netIO#: #{{.NetIO}}#,#blockIO#: #{{.BlockIO}}#,#PIDs#: #{{.PIDs}}# }' | stdbuf -oL -eL  sed -e  's/\#/\\\#/g' | sed 's/\x1b//g' |   xargs -L 1 echo `date +'{#time#:#%Y-%m-%d %H:%M:%S#'` $1  | sed -e  's/#/\"/g'" > /tmp/docker_container/docker_container1.log


第二步:
运行kafka
cd kafka
./start-kafka-server.sh

第三步:
启动flink 集群或单例
cd $FLINK_HOME
bin/start-cluster.sh


第四步:运行filebeat    把/tmp/docker_container/下的日志文件 采集送到kafka

cd filebeat
./start-docker.sh

第五步:
运行 flink-app



# docker-monitoring

---
layout: post
title: "Nginx+tomcat集群+Redis集群搭建"
description: "Nginx+tomcat集群+Redis集群搭建"
category: articles
tags: [web]
comments: true
---
Nginx+tomcat集群+Redis集群搭建
=======
#### Reference
<http://blog.csdn.net/l1028386804/article/details/52216000>
<https://github.com/jcoleman/tomcat-redis-session-manager>
#### 集成
tomcat要支持Redis，需要，Copy the following files into the **/usr/share/tomcat/lib/** directory:</br>
注意哈，这里目录**/usr/share/tomcat/lib/** 是查询配置文件/etc/tomcat/tomcat.conf，得到$CATALINA_HOME/lib/

+ tomcat-redis-session-manager-VERSION.jar
+ jedis-2.5.2.jar
+ commons-pool2-2.2.jar

1. 从maven的repository中获取jedis jar包，http://central.maven.org/maven2/redis/clients/jedis/2.5.2/jedis-2.5.2.jar
2. 从maven的repository中获取 http://central.maven.org/maven2/com/bluejeans/tomcat-redis-session-manager/2.0.0/tomcat-redis-session-manager-2.0.0.jar
3. 从这里获取 http://central.maven.org/maven2/org/apache/commons/commons-pool2/2.2/commons-pool2-2.2.jar
同时还需要下面jar包
```
[root@nn1 tomcats]#  wget http://central.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
[root@nn1 tomcats]#  wget http://central.maven.org/maven2/org/apache/tomcat/tomcat-juli/7.0.69/tomcat-juli-7.0.69.jar
```

</br>
修改/etc/tomcat/context.xml， 如下：
```
<Valve className="com.orangefunction.tomcat.redissessions.RedisSessionHandlerValve"/>
<Manager className="com.orangefunction.tomcat.redissessions.RedisSessionManager" host="localhost" port="6379" database="0" maxInactiveInterval="60"/>
```
因为我搭建的是redis的Cluster，而目前Jedis还不支持。参考了这篇文章：<http://blog.csdn.net/dlf123321/article/details/53900378> 还是没有成功，出现错误2.所以暂时没有继续，想先学习学习redis的使用再说。
#### 问题集锦
1. Tomcat报如下的错：
```
Jul 28, 2017 9:37:49 AM org.apache.catalina.core.StandardHostValve invoke
SEVERE: Exception Processing /sample/session.jsp
redis.clients.jedis.exceptions.JedisMovedDataException: MOVED 3402 10.255.6.255:7000
        at redis.clients.jedis.Protocol.processError(Protocol.java:102)
        at redis.clients.jedis.Protocol.process(Protocol.java:131)
        at redis.clients.jedis.Protocol.read(Protocol.java:200)
        at redis.clients.jedis.Connection.readProtocolWithCheckingBroken(Connection.java:285)
        at redis.clients.jedis.Connection.getBinaryBulkReply(Connection.java:204)
        at redis.clients.jedis.BinaryJedis.get(BinaryJedis.java:120)
        at com.bluejeans.tomcat.redissessions.RedisSessionManager.loadSessionDataFromRedis(RedisSessionManager.java:503)
        at com.bluejeans.tomcat.redissessions.RedisSessionManager.findSession(RedisSessionManager.java:432)
        at org.apache.catalina.connector.Request.doGetSession(Request.java:3022)
        at org.apache.catalina.connector.Request.getSessionInternal(Request.java:2636)
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:430)
        at com.bluejeans.tomcat.redissessions.RedisSessionHandlerValve.invoke(RedisSessionHandlerValve.java:21)
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:169)
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:103)
        at org.apache.catalina.valves.AccessLogValve.invoke(AccessLogValve.java:956)
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:436)
        at org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1078)
        at org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)
        at org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
        at java.lang.Thread.run(Thread.java:748)
```
2. Tomcat的//usr/share/tomcat/logs/catalina.2017-07-28.log报如下错误：
```
SEVERE: The session manager failed to start
org.apache.catalina.LifecycleException: Failed to start component [com.orangefunction.tomcat.redissessions.RedisSessionManager[/sample]]
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:153)
        at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5593)
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:147)
        at org.apache.catalina.core.ContainerBase.addChildInternal(ContainerBase.java:899)
        at org.apache.catalina.core.ContainerBase.addChild(ContainerBase.java:875)
        at org.apache.catalina.core.StandardHost.addChild(StandardHost.java:652)
        at org.apache.catalina.startup.HostConfig.deployWAR(HostConfig.java:1092)
        at org.apache.catalina.startup.HostConfig$DeployWar.run(HostConfig.java:1984)
        at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.NumberFormatException: For input string: "7000@17000"
        at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
        at java.lang.Integer.parseInt(Integer.java:580)
        at java.lang.Integer.valueOf(Integer.java:766)
        at redis.clients.util.ClusterNodeInformationParser.getHostAndPortFromNodeLine(ClusterNodeInformationParser.java:40)
        at redis.clients.util.ClusterNodeInformationParser.parse(ClusterNodeInformationParser.java:14)
        at redis.clients.jedis.JedisClusterConnectionHandler.discoverClusterNodesAndSlots(JedisClusterConnectionHandler.java:72)
        at redis.clients.jedis.JedisClusterConnectionHandler.initializeSlotsCache(JedisClusterConnectionHandler.java:48)
        at redis.clients.jedis.JedisClusterConnectionHandler.<init>(JedisClusterConnectionHandler.java:30)
        at redis.clients.jedis.JedisSlotBasedConnectionHandler.<init>(JedisSlotBasedConnectionHandler.java:14)
        at redis.clients.jedis.JedisCluster.<init>(JedisCluster.java:30)
        at redis.clients.jedis.JedisCluster.<init>(JedisCluster.java:21)
        at redis.clients.jedis.JedisCluster.<init>(JedisCluster.java:25)
        at com.orangefunction.tomcat.redissessions.RedisSessionManager.initializeDatabaseConnection(RedisSessionManager.java:614)
        at com.orangefunction.tomcat.redissessions.RedisSessionManager.startInternal(RedisSessionManager.java:287)
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:147)
        ... 12 more
```

案例1
=====
**reference** : <https://blog.csdn.net/renfufei/article/details/77585294>
在ＧＲＰＣ的测试中实际遇到的问题如下：
```
Sep 20 04:21:11 telmetrycollector collector-1.0.0-SNAPSHOT.jar: Exception in thread "grpc-default-executor-25772" java.lang.OutOfMemoryError: GC overhead limit exceeded
```
```
Sep 20 02:32:54 telmetrycollector collector-1.0.0-SNAPSHOT.jar: Exception in thread "grpc-default-executor-24886" java.lang.OutOfMemoryError: Java heap space
```
######分析

JVM抛出 java.lang.OutOfMemoryError: GC overhead limit exceeded 错误就是发出了这样的信号: 执行垃圾收集的时间比例太大, 有效的运算量太小. 默认情况下, 如果GC花费的时间超过 98%, 并且GC回收的内存少于 2%, JVM就会抛出这个错误。

注意, java.lang.OutOfMemoryError: GC overhead limit exceeded 错误只在连续多次 GC 都只回收了不到2%的极端情况下才会抛出。假如不抛出 GC overhead limit 错误会发生什么情况呢? 那就是GC清理的这么点内存很快会再次填满, 迫使GC再次执行. 这样就形成恶性循环, CPU使用率一直是100%, 而GC却没有任何成果. 系统用户就会看到系统卡死 - 以前只需要几毫秒的操作, 现在需要好几分钟才能完成。

#######总结
这个问题发生在ｅｘｅｃｕｔｏｒ，也就是，是ａｐｐｌｉｃａｔｉｏｎ那边出的问题．其实从上面的线程数，就可以看出来创建了太多线程了,因为grpc-default-executor是用的CachedThreadPool，只要有需要，就会创建新的ｔｈｒｅａｄ，但是也同时证明load很大啊．

其实就是这个就是因为一下子来了太多个任务，测试是每秒钟发了２０００－３０００个请求，所以overload了．耗尽了ＪＶＭ的ＨＥＡＰ．

######解决方案
我个人觉得其实就是load太大．

案例2
=====
在ＧＲＰＣ的测试中实际遇到的问题如下：
```
2018-09-25 02:14:05 [grpc-default-worker-ELG-1-2] WARN  io.grpc.netty.NettyServerStream - Exception processing me
ssage
io.grpc.StatusRuntimeException: RESOURCE_EXHAUSTED: gRPC message exceeds maximum size 4194304: 4216807
        at io.grpc.Status.asRuntimeException(Status.java:517)
        at io.grpc.internal.MessageDeframer.processHeader(MessageDeframer.java:387)
        at io.grpc.internal.MessageDeframer.deliver(MessageDeframer.java:267)
        at io.grpc.internal.MessageDeframer.request(MessageDeframer.java:161)
        at io.grpc.internal.AbstractStream$TransportState.requestMessagesFromDeframer(AbstractStream.java:205)
        at io.grpc.netty.NettyServerStream$Sink$1.run(NettyServerStream.java:100)
        at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
        at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:404)
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:463)
        at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:886)
        at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.lang.Thread.run(Thread.java:748)
```
######分析
首先，这个问题发生在grpc-default-worker，来看看worker的解释
```
Once the Netty channel has been created it gets passes to the Worker Event Loop Group.  This is the threadpool dedicating to doing socket read() and write() calls.  This is often called the "network thread".   In addition to doing the direct reads and writes, it can also do small amounts of processing, such as turn a collection of bytes into an HTTP/2 request, or do SSL / TLS encryption.  The main thing to know about this group is that it must never block.  That is, it cannot call wait(), or sleep(), or await results from a Future, or things like that.  The reason is that doing this prevents the worker thread (the "event loop") from servicing other requests.
```
以上错误是发生在读的时候．而且，抛出来的异常是RuntimeException，所以处理当前request的线程退出

######解决方案
增加grpc的max message size.


案例3：
======
####Description: 
在GRPC Server收到客户端的数据后，出错，log如下：
```
2018-10-16 00:59:54 [grpc-default-executor-5] WARN  io.grpc.internal.ServerCallImpl - Cancelling the stream with status Status{code=INTERNAL, description=Too many responses, cause=null}
2018-10-16 00:59:54 [grpc-default-executor-6] ERROR c.n.c.o.t.c.g.CollectorServiceImpl - Telemetry stream request error.
io.grpc.StatusRuntimeException: CANCELLED: cancelled before receiving half close
        at io.grpc.Status.asRuntimeException(Status.java:517)
        at io.grpc.stub.ServerCalls$StreamingServerCallHandler$StreamingServerCallListener.onCancel(ServerCalls.java:272)
        at io.grpc.internal.ServerCallImpl$ServerStreamListenerImpl.closed(ServerCallImpl.java:293)
        at io.grpc.internal.ServerImpl$JumpToApplicationThreadServerStreamListener$1Closed.runInContext(ServerImpl.java:737)
        at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37)
        at io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:123)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)
2018-10-16 01:00:03 [grpc-default-worker-ELG-1-8] WARN  io.grpc.netty.NettyServerHandler - Stream Error
io.netty.handler.codec.http2.Http2Exception$StreamException: Received DATA frame for an unknown stream 1
        at io.netty.handler.codec.http2.Http2Exception.streamError(Http2Exception.java:129)
        at io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder$FrameReadListener.shouldIgnoreHeadersOrDataFrame(DefaultHttp2ConnectionDecoder.java:535)
        at io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder$FrameReadListener.onDataRead(DefaultHttp2ConnectionDecoder.java:187)
        at io.netty.handler.codec.http2.Http2InboundFrameLogger$1.onDataRead(Http2InboundFrameLogger.java:48)
        at io.netty.handler.codec.http2.DefaultHttp2FrameReader.readDataFrame(DefaultHttp2FrameReader.java:421)
```
抓包可以看到是有server端主动发起了一个RST_STREAM去cancle的这个stream, 如下：
```
73  223.014115  172.24.168.103  135.251.96.66   HTTP2   79  DATA[1]
74  223.017813  135.251.96.66   172.24.168.103  HTTP2   69  RST_STREAM[1]
```
其中， 172.24.168.103是client,135.251.96.66是server．

####Analyse
因为报的错都是底层的错，百思不得其解，为啥server要去cancel.远程debug以后发现，是在io.grpc.internal. ServerCallImp方法internalClose被调用，从而发起的cancle,如下：
```java
 public void sendMessage(RespT message) {
        Preconditions.checkState(this.sendHeadersCalled, "sendHeaders has not been called");
        Preconditions.checkState(!this.closeCalled, "call is closed");
        if (this.method.getType().serverSendsOneMessage() && this.messageSent) {
            this.internalClose(Status.INTERNAL.withDescription("Too many responses"));
        } else {
        this.messageSent = true;

            try {
                InputStream resp = this.method.streamResponse(message);
                ...
```
而从以上代码可以看到，只有当 if (this.method.getType().serverSendsOneMessage() && this.messageSent)两个条件都满足时才会进入一下分支．</p>

this.method.getType().serverSendsOneMessage()的意思是，response只能发一个message,也就是unary和client streaming都满足．我当前是client streaming，自然是满足的．</p>

那对于当前情况，只有this.messageSent为true,就促发internalClose.但是代码的else分支，可以看到马上this.messageSent = true了呀．</p>

这个时候，我又回顾了下，sendMessage是被io.grpc.stub$ServerCallStreamObserverImpl的onNext()方法调用的
```java
  public void onNext(RespT response) {
            if (this.cancelled) {
                throw Status.CANCELLED.withDescription("call already cancelled").asRuntimeException();
            } else {
                if (!this.sentHeaders) {
                    this.call.sendHeaders(new Metadata());
                    this.sentHeaders = true;
                }

                this.call.sendMessage(response);
            }
        }
```
通过远程调用，我发觉我这个oneNext被调用了好多次．然后赶紧去拜读下grpc官方文档，发现定义如下：
refer to <https://grpc.io/grpc-java/javadoc/io/grpc/stub/StreamObserver.html#onNext-V->
```
void onNext(V value)

Receives a value from the stream.

Can be called many times but is never called after onError(Throwable) or onCompleted() are called.

Unary calls must invoke onNext at most once. Clients may invoke onNext at most once for server streaming calls, but may receive many onNext callbacks. Servers may invoke onNext at most once for client streaming calls, but may receive many onNext callbacks.

If an exception is thrown by an implementation the caller is expected to terminate the stream by calling onError(Throwable) with the caught exception prior to propagating it.

Parameters:
    value - the value passed to the stream 
```
啥子意思呢，对于我这案例，重点就是client-streaming下，server端就只能调用一次onNext()呀．而我在server端是扎个写的呢，错误的代码如下：
```java
  @Override
            public void onNext(TelemetryOuterClass.TelemetryStreamPublish telemetryStreamPublish) {
                telemetryNotification.publish(telemetryStreamPublish);
                 responseObserver.onNext(TelemetryOuterClass.TelemetryStreamResponse
                        .newBuilder().build());
            }
```
看到没，我在server短的service实现里面的onNext，调用了 responseObserver.onNext.那就是说客户端每调用一次onNext，responseObserver.onNext就要被调用一次．</p>

所以会走入internalClose分支呀．人家文档说得明明白白的，对于client-streaming，server端就只能调用一次onNext()．

####Solution:
把responseObserver.onNext调用放到onCompleted调用．
```java 
 @Override
            public void onNext(TelemetryOuterClass.TelemetryStreamPublish telemetryStreamPublish) {
                telemetryNotification.publish(telemetryStreamPublish);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Telemetry stream request error.",throwable);
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(TelemetryOuterClass.TelemetryStreamResponse
                        .newBuilder().build());
                log.info("Telemetry stream request completed.");
                responseObserver.onCompleted();
            }
        };
```
案例4：
======
####Description: 
抓包看到：
Client端给GRPC SERVER发送PING，GRPC Server给client端回了一个GOAWAY，
Stream: GOAWAY, Stream ID: 0, Length 22
Error: ENHANCE_YOUR_CALM (11)
too_many_pings
然后server端发送了RST。
具体见包grpc-ping.cap ![](grpc-ping.cap)
####Analyse
查看类io.grpc.netty.NettyServerBuilder
发现如下代码：
```
 this.permitKeepAliveTimeInNanos = TimeUnit.MINUTES.toNanos(5L);
```
 根据<https://groups.google.com/forum/#!topic/grpc-io/_ZKK58mP4h0> 里面的如下描述：
 ```
 Keepalives in Netty and OkHttp now allow sending pings without outstanding RPCs. The minimum keepalive time was also reduced from 1 minute to 10 seconds. Clients must get permission from the services they use before enabling keepalive."

However, that puts servers in danger, so we also added server-side detection of over-zealous clients. In the release notes:

"Netty server: now detects overly aggressive keepalives from clients, with configurable limits. Defaults to permitting keepalives every 5 minutes only while there are outstanding RPCs, but clients must not depend on this value."

too_many_pings is the server saying the client is pinging too frequently. Either reduce the keepalive rate on the client-side or increase the limit on the server-side.
 ```
 可以得知，是因为client发送ping太过频繁。
####Solution:
案例5
======
####Description: 
java.lang.OutOfMemoryError: unable to create new native thread</br>
在collector运行了一段时间后，发现了如下错误：
```
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: #033[30m2019-09-29 17:26:31,960#033[0;39m #033[1;31mERROR#033[0;39m [#033[34mgrpc-default-boss-ELG-3-1#033[0;39m] #033[33m
io.netty.util.internal.logging.Slf4JLogger#033[0;39m: Failed to submit a listener notification task. Event loop shut down?
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: java.lang.OutOfMemoryError: unable to create new native thread
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at java.lang.Thread.start0(Native Method)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at java.lang.Thread.start(Thread.java:717)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.ThreadPerTaskExecutor.execute(ThreadPerTaskExecutor.java:33)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.doStartThread(SingleThreadEventExecutor.java:875)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.startThread(SingleThreadEventExecutor.java:864)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.execute(SingleThreadEventExecutor.java:768)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.safeExecute(DefaultPromise.java:764)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:432)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:103)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:84)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannel$CloseFuture.setClosed(AbstractChannel.java:1148)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannel$AbstractUnsafe.register(AbstractChannel.java:490)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.SingleThreadEventLoop.register(SingleThreadEventLoop.java:80)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.SingleThreadEventLoop.register(SingleThreadEventLoop.java:74)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.MultithreadEventLoopGroup.register(MultithreadEventLoopGroup.java:86)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelRead(ServerBootstrap.java:255)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1414)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:945)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.nio.AbstractNioMessageChannel$NioMessageUnsafe.read(AbstractNioMessageChannel.java:93)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:645)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:580)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:497)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:459)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:886)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at java.lang.Thread.run(Thread.java:748)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: #033[30m2019-09-29 17:26:31,961#033[0;39m #033[1;31mERROR#033[0;39m [#033[34mgrpc-default-boss-ELG-3-1#033[0;39m] #033[33mio.netty.util.internal.logging.Slf4JLogger#033[0;39m: Failed to submit a listener notification task. Event loop shut down?
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: java.lang.OutOfMemoryError: unable to create new native thread
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at java.lang.Thread.start0(Native Method)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at java.lang.Thread.start(Thread.java:717)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.ThreadPerTaskExecutor.execute(ThreadPerTaskExecutor.java:33)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.doStartThread(SingleThreadEventExecutor.java:875)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.startThread(SingleThreadEventExecutor.java:864)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.SingleThreadEventExecutor.execute(SingleThreadEventExecutor.java:768)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.safeExecute(DefaultPromise.java:764)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:432)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.util.concurrent.DefaultPromise.tryFailure(DefaultPromise.java:121)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannel$AbstractUnsafe.safeSetFailure(AbstractChannel.java:987)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannel$AbstractUnsafe.register(AbstractChannel.java:491)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.SingleThreadEventLoop.register(SingleThreadEventLoop.java:80)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.SingleThreadEventLoop.register(SingleThreadEventLoop.java:74)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.MultithreadEventLoopGroup.register(MultithreadEventLoopGroup.java:86)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor.channelRead(ServerBootstrap.java:255)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
Sep 29 17:26:31 adapter_10_242_111_37 collector-1.0.0-SNAPSHOT.jar: at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
```
####Analyse
1. Check the thread count by :
```
 # ps -T <pid>|wc -l
```
And also can get a tough count by :
```
systemctl status collector
...
    Tasks: 5676
```
结论： 确实太多thread了。
2. Do threaddump by:
```
# jstack -l 20794  > /root/tian/collectorThreadDump.txt
# grep "grpc-default-executor" /root/tian/collectorThreadDump.txt|wc -l
4829
# grep "java.lang.Thread.State: WAITING (parking)" /root/tian/collectorThreadDump.txt |wc -l
4832
# grep " java.lang.Thread.State: BLOCKED" /root/tian/collectorThreadDump.txt|wc -l
14
```
问题应该就是出在那些WAITING的thread上。</br>
**grpc-default-executor is also called the "app thread".  This is where the gRPC stubs do their main work.** </br>
3. 分析这个threaddump，发现了WATTTING的都是这样的：
```
grep "ch.qos.logback.classic.Logger.info" /root/tian/collectorThreadDump.txt|wc -l
2393
```
```
grpc-default-executor-19372" #19469 daemon prio=5 os_prio=0 tid=0x00007f6dc855a800 nid=0x443a waiting on condition [0x00007f6d4c7ee000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000005cbcbf298> (a java.util.concurrent.locks.ReentrantLock$FairSync)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
        at java.util.concurrent.locks.ReentrantLock$FairSync.lock(ReentrantLock.java:224)
        at java.util.concurrent.locks.ReentrantLock.lock(ReentrantLock.java:285)
        at ch.qos.logback.core.OutputStreamAppender.subAppend(OutputStreamAppender.java:210)
        at ch.qos.logback.core.rolling.RollingFileAppender.subAppend(RollingFileAppender.java:235)
        at ch.qos.logback.core.OutputStreamAppender.append(OutputStreamAppender.java:100)
        at ch.qos.logback.core.UnsynchronizedAppenderBase.doAppend(UnsynchronizedAppenderBase.java:84)
        at ch.qos.logback.core.spi.AppenderAttachableImpl.appendLoopOnAppenders(AppenderAttachableImpl.java:48)
        at ch.qos.logback.classic.Logger.appendLoopOnAppenders(Logger.java:270)
        at ch.qos.logback.classic.Logger.callAppenders(Logger.java:257)
        at ch.qos.logback.classic.Logger.buildLoggingEventAndAppend(Logger.java:421)
        at ch.qos.logback.classic.Logger.filterAndLog_1(Logger.java:398)
        at ch.qos.logback.classic.Logger.info(Logger.java:583)
        at com.nokia.cd.otc.telemetry.collector.grpc.CollectorServiceImpl$1.onNext(CollectorServiceImpl.java:44)
        at com.nokia.cd.otc.telemetry.collector.grpc.CollectorServiceImpl$1.onNext(CollectorServiceImpl.java:38)
        at io.grpc.stub.ServerCalls$StreamingServerCallHandler$StreamingServerCallListener.onMessage(ServerCalls.java:248)
        at io.grpc.ForwardingServerCallListener.onMessage(ForwardingServerCallListener.java:33)
        at com.nokia.cd.otc.telemetry.collector.grpc.GrpcServerInterceptor$1.onMessage(GrpcServerInterceptor.java:26)
        at io.grpc.internal.ServerCallImpl$ServerStreamListenerImpl.messagesAvailable(ServerCallImpl.java:263)
        at io.grpc.internal.ServerImpl$JumpToApplicationThreadServerStreamListener$1MessagesAvailable.runInContext(ServerImpl.java:682)
        at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37)
        at io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:123)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - <0x000000073088a3c8> (a java.util.concurrent.ThreadPoolExecutor$Worker)
```
那就是都等着打log，不过当前load其实也不大，就是大概43/秒。不至于撒？


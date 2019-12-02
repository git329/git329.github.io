Reference
=========
这篇源代码啊分析很不错：　<https://www.jianshu.com/p/3246ebfdfd54>
这篇讲究了一些原理：　<http://shift-alt-ctrl.iteye.com/blog/2292862>

ＮＥＴＴＹ学习
==================
这篇是ＮＥＴＴＹ的学习：　<https://segmentfault.com/a/1190000013741848>

Scalable IO in Java
======
<http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf>

Listen,Connect,Accept
=====================
服务器端在调用listen之后，内核会建立两个队列，SYN队列和ACCEPT队列，其中ACCPET队列的长度由backlog指定。
服务器端在调用accpet之后，将阻塞，等待ACCPT队列有元素。
客户端在调用connect之后，将开始发起SYN请求，请求与服务器建立连接，此时称为第一次握手。
服务器端在接受到SYN请求之后，把请求方放入SYN队列中，并给客户端回复一个确认帧ACK，此帧还会携带一个请求与客户端建立连接的请求标志，也就是SYN，这称为第二次握手
客户端收到SYN+ACK帧后，connect返回，并发送确认建立连接帧ACK给服务器端。这称为第三次握手
服务器端收到ACK帧后，会把请求方从SYN队列中移出，放至ACCEPT队列中，而accept函数也等到了自己的资源，从阻塞中唤醒，从ACCEPT队列中取出请求方，重新建立一个新的sockfd，并返回。
这就是listen,accept,connect这三个函数的工作流程及原理。从这个过程可以看到，在connect函数中发生了两次握手。


BIO,NIO学习
===========
有关ＮＩＯ，ＢＩＯ学习: <https://segmentfault.com/a/1190000012976683>
＜https://www.jianshu.com/p/8ad464ed516e＞
JAVA NIO学习:<https://www.cnblogs.com/puyangsky/p/5840873.html>

ＴＣＰ／ＩＰ基础
===============
从底层ＴＣＰ开始比较详细的ＮＩＯ，这个是一个系列文章，非常好: <https://my.oschina.net/u/1859679/blog/1835423>

阻塞，同步
========
一次IO操作，以read方法举例，会经历两个阶段：
（1）等待数据准备(Waitingfor the data to be ready)
是否阻塞指的就是这一个阶段。

（2）将数据从内核拷贝到进程中(Copying the data from the kernel to the process)
是否同步指的就是这一个阶段。

BIO
===
blocking IO，阻塞式IO.**同步并阻塞**
服务器的实现模式是一个连接一个线程.
BIO 全称Block-IO 是一种阻塞同步的通信模式。我们常说的Stock IO 一般指的是BIO。是一个比较传统的通信方式，模式简单，使用方便。但并发处理能力低，通信耗时，依赖网速。

NIO
=====
即non-blocking IO. **同步非阻塞的**
NIO本身是基于事件驱动思想来完成的

NIO的三个重点，重中之重的是：
1. channel（通道）
连接data数据与buffer缓存区的桥梁。
* 既可以从通道中读取数据，又可以写数据到通道。但流的读写通常是单向的。
* 通道可以异步地读写。
* 通道中的数据总是要先读到一个Buffer，或者总是要从一个Buffer中写入。
2. Buffer（缓冲区）
用于和NIO通道进行交互。如图所示，数据是从通道读入缓冲区，从缓冲区写入到通道中的
3. Selector（选择器）
是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件，如此一个单独的线程可以管理多个channel，从而管理多个连接。


BIO VS NIO
==========
BIO与NIO一个比较重要的不同，是我们使用BIO的时候往往会引入多线程，**每个连接一个单独的线程**而NIO则是使用单线程或者只使用少量的多线程，每个连接共用一个线程。

ＧＲＰＣ源码阅读
=========
＜https://skyao.io/learning-grpc/grpc/source_navigating.html＞

Thread Pool in GRPC
====================
There are three main threadpools that gRPC Java uses in Server mode:

* Boss Event Loop Group  (a.k.a. bossEventLoopGroup() )
* Worker Event Loop Group ( a.k.a. workerEventLoopGroup() )：grpc-default-worker
* Application Executor (a.k.a. executor() )

The Boss group can be the same as the worker group.  It's purpose is to accept calls from the network, and create Netty channels (not gRPC Channels) to handle the socket.   Effectively, this is the thread pool that calls listen() and accept().   

Once the Netty channel has been created it gets passes to the Worker Event Loop Group.  This is the threadpool dedicating to doing socket read() and write() calls.  This is often called the "network thread".   In addition to doing the direct reads and writes, it can also do small amounts of processing, such as turn a collection of bytes into an HTTP/2 request, or do SSL / TLS encryption.  The main thing to know about this group is that it must never block.  That is, it cannot call wait(), or sleep(), or await results from a Future, or things like that.  The reason is that doing this prevents the worker thread (the "event loop") from servicing other requests.


The last thread group is the application executor, also called the "app thread".  This is where the gRPC stubs do their main work.  It is for handling the callbacks that bubble up from the network thread.  For example, when a packet comes in, the worker thread decrypts it, decodes the HTTP/2 code, and then notifies the app thread that data has arrived.  It immediately goes back to doing other network work.  The App thread notices that there is data available, and converts the data into (usually) a Protobuf, and then invokes your stub.   

Note that the Application executor could have no threads at all.  This would make the network thread immediately do the work of handling the request.   On the builder this is the directExecutor() function.   The reason for using this is that it saves a thread hop from worker event loop to the app thread, or about 15 microseconds.  In very latency sensitive environments this is a good idea.  However, it makes it very easy to accidentally do blocking work, which is deadlock prone.   It is rarely correct to do.


As for usages:   Most people should use either reuse the same boss event loop group as the worker group.   Barring this, the boss eventloop group should be a single thread, since it does very little work.  For the app thread, users should provide a fixed size thread pool.  By default, we use an unbounded cached threadpool, but this is just for safety.  It is not the most efficient, and can be dangerous in some circumstances.  It just happens to be the safest default, given no other information.       

grpc-default-executor
=========
The last thread group is the application executor, also called the "app thread".  This is where the gRPC stubs do their main work.  It is for handling the callbacks that bubble up from the network thread.  For example, when a packet comes in, the worker thread decrypts it, decodes the HTTP/2 code, and then notifies the app thread that data has arrived.  It immediately goes back to doing other network work.  The App thread notices that there is data available, and converts the data into (usually) a Protobuf, and then invokes your stub.  

在io/grpc/internal/GrpcUtil$1.class这个里面创建出来的．是newCachedThreadPool．所以，它会按需要创建新的线程。
所以有遇到过如下错误：
```
java.lang.OutOfMemoryError: unable to create new native thread
```
简单的说，处理ｇｒｐｃ的ｓｔｕｂ业务就是它．

所以之前测试中遇到如下错误：
```
Sep 20 04:21:11 telmetrycollector collector-1.0.0-SNAPSHOT.jar: Exception in thread "grpc-default-executor-25772" java.lang.OutOfMemoryError: GC overhead limit exceeded
```
就是因为同一时刻client发送太多条了。



grpc-default-boss-ELG
===================
对应Reactor模式中的**主Reactor**,用于响应连接请求．
Effectively, this is the thread pool that calls listen() and accept().   


grpc-default-worker
==================
对应Reactor模式中的**从Reactor**,用于处理IO操作请求．
This is the threadpool dedicating to doing socket read() and write() calls. 

属于 Worker Event Loop Group．

这个就是处理处理事件的线程了。在/io/grpc/grpc-netty/1.12.0/grpc-netty-1.12.0.jar!/io/grpc/netty/Utils.class有如下定义：
```java
static {
       ...
        DEFAULT_BOSS_EVENT_LOOP_GROUP = new Utils.DefaultEventLoopGroupResource(1, "grpc-default-boss-ELG");
        DEFAULT_WORKER_EVENT_LOOP_GROUP = new Utils.DefaultEventLoopGroupResource(0, "grpc-default-worker-ELG");
      ...
    }
```
在测试中出现的如下错误,就是在处理事情的线程中出现的：
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

grpc一次请求处理的过程
=====================
refer to <https://www.jianshu.com/p/3246ebfdfd54>
* 简单的说，其实GRPC底部启动的是NettyServer．
* 处理client请求的是NettyServerHandler

从BIO,NIO到NETTY
===============
我觉得这一篇说得最简单清楚：<https://www.jianshu.com/p/2461535c38f3>
我们从Netty服务器代码来看，与Reactor模型进行对应！

* EventLoopGroup就相当于是Reactor，bossGroup对应主Reactor,workerGroup对应从Reactor
"grpc-default-boss-ELG"-----bossGroup----主Reactor
"grpc-default-worker-ELG"-----workerGroup------从Reactor

* TimeServerHandler就是Handler
* child开头的方法配置的是客户端channel，非child开头的方法配置的是服务端channel


GRPC异常处理流程
=================
<https://skyao.gitbooks.io/learning-grpc/content/server/status/exception_process.html>
---
layout: post
title: Spark shuffle 学习
description: "Spark编程"
category: articles
tags: [Spark编程]
comments: true
---
Spark shuffle学习
=======
####  Reference
<http://sharkdtu.com/posts/spark-shuffle.html>
<https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html>
<https://www.ibm.com/developerworks/cn/java/j-lo-just-in-time/index.html>
#### 什么是shuffle
这个按照一定的规则对数据重新分区的过程就是Shuffle（洗牌）。</br>
以Shuffle为边界，Spark将一个Job划分为不同的Stage，这些Stage构成了一个大粒度的DAG。Spark的Shuffle分为Write和Read两个阶段，分属于两个不同的Stage，前者是Parent Stage的最后一步，后者是Child Stage的第一步
#### Shuffle演进
从spark-1.6.0开始，把Sort Shuffle和Unsafe Shuffle全部统一到Sort Shuffle中，如果检测到满足Unsafe Shuffle条件会自动采用Unsafe Shuffle，否则采用Sort Shuffle。从spark-2.0.0开始，spark把Hash Shuffle移除，可以说目前spark-2.0中只有一种Shuffle，即为Sort Shuffle。
#### 内存管理和二进制处理
##### JVM的问题
基于JVM的application主要基于JVM的Garbage collector来管理内存。但是有两个问题。</br>
1. Java Object与生俱来的特性就是占字节数多。比如一个简单的4个字节string在JVM object model中需要48 bytes。具体可参照美文<https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html> </br>
2. JVM的垃圾回收机制通常是把object分成两类，年轻代（生命周期比较短的对象）和老年代。GC能够高效的工作，依赖于GC能够很好的估计Object的生命周期。</br>
但是，Spark不是一个通常意义的application。Spark清楚了解数据流怎样通过计算的各个Stage， 以及Job和Task的scope。因此，Sprak比GC更了解内存块（Memory Block）的生命周期，所以啊，Spark有这个能力比GC更加高效的管理内存。
##### Spark解决方案
为了解决object的开销以及GC的低效。Spark引入了显示的memory manager，直接对二进制数据而不是java 对象操作。</br>
This builds on <font color="HotPink">sun.misc.Unsafe</font>, an advanced functionality provided by the JVM that exposes C-style memory access (e.g. explicit allocation, deallocation, pointer arithmetics). Furthermore, Unsafe methods are intrinsic, meaning each method call is compiled by <font color="HotPink">JIT </font>into a single machine instruction.</br>
这里JIT就是Just In Timej的缩写, 也就是即时编译编译器。使用即时编译器技术，能够加速 Java 程序的执行速度</br>
关于JIT参见另一篇美文 <https://www.ibm.com/developerworks/cn/java/j-lo-just-in-time/index.html》
#### shuffle调优
Shuffle是一个涉及到CPU（序列化与反序列化），网络IO（跨节点数据传输），磁盘IO（shuffle的中间结果落地）的操作。用户在编写程序时要尽可能的考虑Shuffle的相关优化，提升Spark应用程序的性能。
1. 尽量减少shuffle的次数
2. 必要时主动shuffle，通常用于改变并行度，提高后续分布式运行速度
3. 使用treeReduce & treeAggregate替换reduce & aggregate。数据量较大时，reduce & aggregate一次性聚合，shuffle量太大，而treeReduce & treeAggregate是分批聚合，更为保险。

test snappy
========
```
# /data/toop/telemetry/hbase-1.2.0/bin/hbase org.apache.hadoop.hbase.util.CompressionTest file:///var/tmp/env.config snappy
Picked up JAVA_TOOL_OPTIONS: -Dfile.encodeing=UTF-8
2019-11-07 10:30:34,687 WARN  [main] util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
2019-11-07 10:30:34,990 INFO  [main] hfile.CacheConfig: CacheConfig:disabled
Exception in thread "main" java.lang.UnsatisfiedLinkError: org.apache.hadoop.util.NativeCodeLoader.buildSupportsSnappy()Z
    at org.apache.hadoop.util.NativeCodeLoader.buildSupportsSnappy(Native Method)
    at org.apache.hadoop.io.compress.SnappyCodec.checkNativeCodeLoaded(SnappyCodec.java:63)
    at org.apache.hadoop.io.compress.SnappyCodec.getCompressorType(SnappyCodec.java:132)
    at org.apache.hadoop.io.compress.CodecPool.getCompressor(CodecPool.java:148)
    at org.apache.hadoop.io.compress.CodecPool.getCompressor(CodecPool.java:163)
    at org.apache.hadoop.hbase.io.compress.Compression$Algorithm.getCompressor(Compression.java:303)
    at org.apache.hadoop.hbase.io.encoding.HFileBlockDefaultEncodingContext.<init>(HFileBlockDefaultEncodingContext.java:90)
    at org.apache.hadoop.hbase.io.hfile.HFileBlock$Writer.<init>(HFileBlock.java:851)
    at org.apache.hadoop.hbase.io.hfile.HFileWriterV2.finishInit(HFileWriterV2.java:124)
    at org.apache.hadoop.hbase.io.hfile.HFileWriterV2.<init>(HFileWriterV2.java:116)
    at org.apache.hadoop.hbase.io.hfile.HFileWriterV3.<init>(HFileWriterV3.java:67)
    at org.apache.hadoop.hbase.io.hfile.HFileWriterV3$WriterFactoryV3.createWriter(HFileWriterV3.java:59)
    at org.apache.hadoop.hbase.io.hfile.HFile$WriterFactory.create(HFile.java:309)
    at org.apache.hadoop.hbase.util.CompressionTest.doSmokeTest(CompressionTest.java:124)
    at org.apache.hadoop.hbase.util.CompressionTest.main(CompressionTest.java:160)

```

Install Snappy on standalone hbase without hadoop
============
refer to <http://lxw1234.com/archives/2017/01/830.htm>

Download the CDH-HADOOP from : <http://archive.cloudera.com/cdh5/cdh/5/hadoop-2.6.0-cdh5.9.3.tar.gz>
1. Install the rpm as below:
```
# rpm -ivh hadoop-2.6.0+cdh5.8.3+1718-1.cdh5.8.3.p0.7.el7.x86_64.rpm --nodeps
```
2. Copy relatee snappy package to hbase.
```
# ll /usr/lib/hadoop/lib/native/
total 2136
-rw-r--r--. 1 root root  221146 Oct 12  2016 libhadoop.a
-rw-r--r--. 1 root root  197264 Oct 12  2016 libhadooppipes.a
lrwxrwxrwx. 1 root root      18 Nov  7 08:13 libhadoop.so -> libhadoop.so.1.0.0
-rwxr-xr-x. 1 root root  139608 Oct 12  2016 libhadoop.so.1.0.0
-rw-r--r--. 1 root root   56714 Oct 12  2016 libhadooputils.a
-rw-r--r--. 1 root root   99694 Oct 12  2016 libhdfs.a
-rw-r--r--. 1 root root 1013616 Oct 12  2016 libnativetask.a
lrwxrwxrwx. 1 root root      22 Nov  7 08:13 libnativetask.so -> libnativetask.so.1.0.0
-rwxr-xr-x. 1 root root  420336 Oct 12  2016 libnativetask.so.1.0.0
lrwxrwxrwx. 1 root root      18 Nov  7 08:13 libsnappy.so -> libsnappy.so.1.1.4
lrwxrwxrwx. 1 root root      18 Nov  7 08:13 libsnappy.so.1 -> libsnappy.so.1.1.4
-rwxr-xr-x. 1 root root   23800 Oct 12  2016 libsnappy.so.1.1.4

# cp -r /usr/lib/hadoop/lib/native  /data/toop/hbase-1.2.0/lib/
```

3. Add one line as below in /data/toop/hbase-1.2.0/conf/hbase-env.sh:
```
export HBASE_LIBRARY_PATH=/data/toop/hbase-1.2.0/lib/native
```
4. Start hbase.

Test Snappy
====
1. test snappy
```
# ./hbase org.apache.hadoop.hbase.util.CompressionTest file:///home/test.txt  snappy 
2019-11-07 08:22:52,758 INFO  [main] hfile.CacheConfig: CacheConfig:disabled
2019-11-07 08:22:52,837 INFO  [main] compress.CodecPool: Got brand-new compressor [.snappy]
2019-11-07 08:22:52,841 INFO  [main] compress.CodecPool: Got brand-new compressor [.snappy]
2019-11-07 08:22:52,915 INFO  [main] hfile.CacheConfig: CacheConfig:disabled
2019-11-07 08:22:52,936 INFO  [main] compress.CodecPool: Got brand-new decompressor [.snappy]
SUCCESS
```

2. Create snappy table as beow:
```
hbase(main):001:0> create 'tsnappy', { NAME => 'f', COMPRESSION => 'snappy'}
0 row(s) in 10.6590 seconds
//describe表
hbase(main):002:0> describe 'tsnappy'
```

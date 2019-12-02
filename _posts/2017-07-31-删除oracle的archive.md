---
layout: post
title: "删除oracle的archive， 用rman"
description: "删除oracle的archive"
category: articles
tags: [oracle]
comments: true
---
删除oracle的archive
=======
先检查db的状态
```
]$ sqlplus / as sysdba

SQL> SELECT INSTANCE_NAME, STATUS, DATABASE_STATUS FROM V$INSTANCE;

INSTANCE_NAME    STATUS       DATABASE_STATUS
---------------- ------------ -----------------
oss              OPEN         ACTIVE
```
然后用RMAN来删除：
```
[oracle@drlab2vm4 sitea ~]$ rman target /

Recovery Manager: Release 12.1.0.2.0 - Production on Mon Jul 31 09:08:29 2017

Copyright (c) 1982, 2014, Oracle and/or its affiliates.  All rights reserved.

connected to target database (not started)

RMAN> STARTUP MOUNT

Oracle instance started
database mounted

Total System Global Area   17649631232 bytes

Fixed Size                     3719400 bytes
Variable Size               3154120472 bytes
Database Buffers           14428405760 bytes
Redo Buffers                  63385600 bytes

RMAN>crosscheck archivelog all;
RMAN>delete NOPROMPT archivelog all;

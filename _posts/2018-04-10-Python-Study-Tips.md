---
layout: post
title: "Java Generic 学习小记3"
description: "Java Generic"
category: articles
tags: [java]
comments: true
---
Python学习趣事
#### reduce对单个item的list操作
```
l=map(int,'123.4'[4:3:-1])
>>> l
[4]
>>> m=reduce(lambda x,y: 0.1*x+y,l)
>>> m
4
```
可以看到，在lambda中的x实际是一个初始值，而y才是取出list中的各个值。
---
layout: post
title: "AngularJS 学习小记1"
category: articles
tags: [java]
comments: true
---

Install on Ubuntu
==============
Refer to : <>
####Install NodeJs

Download from : https://nodejs.org/en/download/

```
tar xJfv node-v8.11.1-linux-x64.tar.xz
$ sudo ln -s /home/tiayin/Downloads/node-v8.11.1-linux-x64/bin/npm /usr/local/bin/npm
$ sudo ln -s /home/tiayin/Downloads/node-v8.11.1-linux-x64/bin/npx /usr/local/bin/npx
$ sudo ln -s /home/tiayin/Downloads/node-v8.11.1-linux-x64/bin/node /usr/local/bin/node
```
检查安装
```
$ node -v
v8.11.1
```

####Config proxy for NPM

```
$ npm config set proxy http://87.254.212.121:8080
$ npm config set https-proxy http://87.254.212.121:8080

```

###Config NG to the path if need
```
$ sudo ln -s /home/tiayin/Downloads/node-v8.11.1-linux-x64/lib/node_modules/@angular/cli/bin/ng /usr/local/bin/ng
```
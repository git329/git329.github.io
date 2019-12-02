---
layout: post
title: "Spring Boot+Angular2 搭建"
description: "Spring Boot+Angular2 搭建"
category: articles
tags: [Java]
comments: true
---
Spring Boot+Angular2 搭建
==============
####  Reference
<https://blog.jdriven.com/2016/12/angular2-spring-boot-getting-started/>
2018.09.03 refer to  <http://g00glen00b.be/prototyping-spring-boot-angularjs/>>
#### 安装angular-cli
在我的pc机上，windows哈
```
\Jedis\springBootRedis\frontend\src\main>ng new --skip-git --directory frontend ng2boot
```
这里的--skip-git是为了不让它创建 git repository，因为目前我们没有在project的根目录。这里创建的Application叫做ng2boot。
这里选择目录为src\main\frontend ，是为了防止maven把source files，包括 node_modules。 放到jar包。
#### 配置Maven来build Angular2 application。
**这部分都是参考<https://blog.jdriven.com/2016/12/angular2-spring-boot-getting-started/>**
这里使用 frontend-maven-plugin来build Angular2的Application。 pom文件长这样：
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.tian.study</groupId>
        <artifactId>study-springBootRedis</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <groupId>com.tian.study</groupId>
    <artifactId>frontend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <build>
        <plugins>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.3</version>

                <configuration>
                    <nodeVersion>v6.10.1</nodeVersion>
                    <npmVersion>4.4.1</npmVersion>
                    <workingDirectory>src/main/frontend</workingDirectory>
                </configuration>

                <executions>
                    <execution>
                        <id>install node and npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>

                    </execution>

                    <execution>
                        <id>npm install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                    </execution>

                    <execution>
                        <id>npm run build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>

                        <configuration>
                            <arguments>run build</arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```
#### 修改.angular-cli.json
为了符合Maven的规范，打开.angular-cli.json文件，修改如下配置：
```   
"outDir": "../../../target/frontend"
```
#### 把Angular2 application打进Jar包
在pom里面配置如下项：
```
<build>
       <resources>
           <resource>
               <directory>target/frontend</directory>
               <targetPath>static</targetPath>
           </resource>
       </resources>
...
```
因为backend是在另一个Module上面，使用需要在backend的POM上面需要加入依赖：

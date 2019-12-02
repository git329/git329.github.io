---
layout: post
title: "SpringBoot+Jedis测试Redis单节点"
description: "Redis访问"
category: articles
tags: [web]
comments: true
---
SpringBoot+Jedis测试Redis单节点
=======
### Reference
<http://blog.didispace.com/springbootredis/>
<https://docs.spring.io/spring-session/docs/current/reference/html5/guides/boot.html>
<http://blog.csdn.net/lifetragedy/article/details/50628820>
### 创建项目
#### 创建新的SpringBoot项目
1. 访问<http://start.spring.io/>创建maven项目,得到一个zip包，把这个zip包解压.注意我这里面选的版本是1.4.3，比较稳定
2. 创建maven项目时就把dependency里面选择redis。
3. 打开IDE(InteliJ)，File–>New–>Project from Existing Sources...
4. POM长这样：
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.tian.study</groupId>
	<artifactId>springBootRedis</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>springBootRedis</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.4.3.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-redis</artifactId>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>


</project>
```
#### 参数配置
按照惯例在application.properties中加入Redis服务端的相关配置
```
#  REDIS (RedisProperties)
#  Redis数据库索引（默认为0）
spring.redis.database=0
#  Redis服务器地址
spring.redis.host=dn1
#  Redis服务器连接端口
spring.redis.port=7001
#  Redis服务器连接密码（默认为空）
spring.redis.password=
#  连接池最大连接数（使用负值表示没有限制）
spring.redis.pool.max-active=8
#  连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.pool.max-wait=-1
#  连接池中的最大空闲连接
spring.redis.pool.max-idle=8
#  连接池中的最小空闲连接
spring.redis.pool.min-idle=0
#  连接超时时间（毫秒）
spring.redis.timeout=0
```
#### 测试访问
```
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootRedisApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
        // 保存字符串
        stringRedisTemplate.opsForValue().set("aaa", "111");
        assertThat("111", equalTo(stringRedisTemplate.opsForValue().get("aaa")));
    }

}
```
### 测试使用redis支持session
#### 引入依赖
```
<dependency>
                <groupId>org.springframework.session</groupId>
                <artifactId>spring-session</artifactId>
                <version>1.3.1.RELEASE</version>
</dependency>
```
#### 创建Spring configuration
创建如下的Spring Configuration，这个配置是创建一个Servlet Filter用了替代HttpSession。 使其使用spring session，并且有redis
```
@EnableRedisHttpSession
public class Config {
    @Bean
    public LettuceConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory();
    }
}
```
+ 这里@EnableRedisHttpSession创建了一个Spring Bean：**springSessionRepositoryFilter**。 这个Bean实现了Filter，这个filter负责取代HttpSession为Spring Session。这里是Redis。功能其实就是和在web.xml里面定义了一个filter差不多。
+ 这里创建了一个RedisConnectionFactory(看代码就会发现LettuceConnectionFactory implement了RedisConnectionFactory)，把HttpSession连接到Redis Server。
#### 初始化Java Servlet Container
创建如下的类：
```
public class Initializer extends AbstractHttpSessionApplicationInitializer {
    public Initializer(){
        super(Config.class);
    }
}
```
+这里创建的 **Intializer** 继承自 **AbstractHttpSessionApplicationInitializer** 从而保证了 **springSessionRepositoryFilter** 在Servlet Container里面被注册了。而且让Spring去Load了，我们前面自己定义的Config。

---
layout: post
title: CCSP 学习小记1"
description: "Security"
category: articles
tags: [Security]
comments: true
---
第一部分：Cloud System Architecture – Concepts and Design
======
这些概念都是根据ISO/IEC 17788中得来的。</br>
ISO/IEC 17788:2014 provides an overview of cloud computing along with a set of terms and definitions. It is a terminology foundation for cloud computing standards.</br>
#####Cloud Computing Participants
1. Cloud Service Provider(CSP)
比如亚马逊的AWS，阿里云啊之类的
2. Cloud Service Customer
3. Cloud Service User(和customer的区别就是，customer是组织，这里就指个人，指使用者)
4. Cloud Service partenter
比如，提供网络链接给cloud provider和客户网络，这样就可以绕过Internet了。
5. Cloud Auditor
Provider必须要有第三方的Audit</br>
可以了解下AWS的autitor，refer to <https://aws.amazon.com/compliance/auditor-learning-path/>
6. Cloud Broker
就是掮客撒，在customer和provider中间协调。</br>

####Key Cloud Computing Characteristics
1. Broad Network Access
2. On-demand self-service
3. Multi-tenant capabilities
比如多个客户可以都共用同一个mail service，但是有各自的instance,并且有各自的定制化配置，彼此是isolate隔离开的。</br>
Client data isolation is a major security concern for multitenant application services.
4. Recourse Pooling
就是CSP有一堆硬件,这些硬件有可能放置在不同的地方，有些CSP还会提供replicate在不同的地方，以达到HA。 就有这种可能性，不同的用户，可能都是使用的同一个physical resource.
5. Rapid Elasticity and Scalability
也就是客户可以根据自己的需要，增加或者减少资源（可以自动的）。</br>
为啥是rapid呢？比起传统的模式，你不需要从买硬件，部署硬件，装操作系统等等一切来做。这些太花时间了，你只需要在cloud里面，简单的provision/deprovision资源就可以了。
6. Measured Service
这样客户就可以知道自己用了好多resource，就可以根据此来付费。

####Cloud Computing Infrstructure
##Compute
#Physical Hosts
#Virtualized Infrstructure-Virtual Machines
##Network
#Physical Transport(比如路由器，交换机这些设备）
#Virtualized Infrstructure-Virtual LANs(就可以让客户的网络彼此分开，更安全)
##Storage
#Physical Volumns
#Shared Storage Pool

####Cloud Computing Activities
##CCRA（Cloud Computing Reference Architecture)
1. Cloud Computing Roles
2. Cloud Computing Activities
3. Cloud Computing Functional Componnents
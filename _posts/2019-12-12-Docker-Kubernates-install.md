---
layout: post
title: Kubernates+Docker 学习小记"
description: "Cloud"
category: articles
tags: [Cloud]
comments: true
---
我发觉东西特别杂，概念特别多，看起来有的懵。但是我觉得这篇文章讲得挺好的： 
<https://medium.com/javarevisited/kubernetes-step-by-step-with-spring-boot-docker-gke-35e9481f6d5f>
OS
===

Install Docker
=====
Prepare:
```
source ~/.bashrc
setenforce 0
sed -i --follow-symlinks 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/sysconfig/selinux
modprobe br_netfilter
sudo echo '1' > /proc/sys/net/bridge/bridge-nf-call-iptables
sudo swapoff -a
update-alternatives --set iptables /usr/sbin/iptables-legacy
```
Install
```
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
```
To clear previous docker related setups (be cautious) 
```
 yum remove -y docker docker-client docker-client-latest docker-common docker-latest docker-latest-logrotate docker-logrotate docker-engine docker-ce docker-ce-cli
sudo yum remove -y docker-selinux
rm -rf /var/lib/docker
rm -rf /var/lib/docker-engine
rm -rf /etc/docker
```
Install docker → download required container-selinux  version w.r.t docker requirement , here it's 2.107-3
```
wget http://mirror.centos.org/centos/7/extras/x86_64/Packages/container-selinux-2.107-3.el7.noarch.rpm
chmod +x container-selinux-2.107-3.el7.noarch.rpm
yum install -y container-selinux-2.107-3.el7.noarch.rpm
yum install -y docker-ce docker-ce-cli containerd.io
mkdir -p /etc/systemd/system/docker.service.d

systemctl enable docker && systemctl start docker
docker version
systemctl status docker.service
```
Install Kubernates
======
refer to <https://callistaenterprise.se/blogg/teknik/2017/12/20/kubernetes-on-docker-in-docker/>
Install jq by:
```
wget http://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
rpm -ivh epel-release-latest-7.noarch.rpm
yum repolist  ##这里报错也没关系，主要看repo加进去没。
yum install jq
```
Insall md5sha1sum by:
```
yum install coreutils
```
Install 
```
git clone https://github.com/Mirantis/kubeadm-dind-cluster.git
cd kubeadm-dind-cluster/fixed
cd /root/kubeadm-dind-cluster/fixed/
chmod +x *.sh
NUM_NODES=3 ./dind-cluster-v1.15.sh up
```

Use kinde to install
============
Refert to <https://itnext.io/starting-local-kubernetes-using-kind-and-docker-c6089acfc1c0>
1. Install go in redhat
```
 # wget https://dl.google.com/go/go1.13.5.linux-amd64.tar.gz
 # tar -C /usr/local/ -xzf go1.13.5.linux-amd64.tar.gz
 # echo 'export PATH=$PATH:/usr/local/go/bin' >>/etc/profile
 # source /etc/profile
[root@kub tmp]# go version
go version go1.13.5 linux/amd64
```
2. Installing kind
Set proxy as below:
```
export http_proxy=http://87.254.212.120:8080/
export HTTPS_PROXY=http://87.254.212.120:8080/
export https_proxy=http://87.254.212.120:8080/
export HTTP_PROXY=http://87.254.212.120:8080/

```
Install kind
```
# go get -u sigs.k8s.io/kind
```
Create Cluster
```
# kind create cluster
```
Check Cluster
```
# kind get clusters
kind
```
3. Install kubectl
```
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.40/bin/darwin/amd64/kubectl
# mv ./kubectl /usr/local/bin/kubectl
# kubectl version
# ll ~/.kube/config 
-rw------- 1 root root 5350 Dec 16 10:16 /root/.kube/config
[root@kub tmp]# kubectl cluster-info
Kubernetes master is running at https://127.0.0.1:46743
KubeDNS is running at https://127.0.0.1:46743/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.

```
4. Install Helm
Helm helps you manage Kubernetes applications — Helm Charts help you define, install, and upgrade even the most complex Kubernetes application</br>

###Helm的基本概念
Helm是Kubernetes的一个包管理工具，用来简化Kubernetes应用的部署和管理。可以把Helm比作CentOS的yum工具。 Helm有如下几个基本概念：

    Chart: 是Helm管理的安装包，里面包含需要部署的安装包资源。可以把Chart比作CentOS yum使用的rpm文件。每个Chart包含下面两部分：
    包的基本描述文件Chart.yaml
    放在templates目录中的一个或多个Kubernetes manifest文件模板
    Release：是chart的部署实例，一个chart在一个Kubernetes集群上可以有多个release，即这个chart可以被安装多次
    Repository：chart的仓库，用于发布和存储chart

使用Helm可以完成以下事情：

    管理Kubernetes manifest files
    管理Helm安装包charts
    基于chart的Kubernetes应用分发

Install Helm Client
```
[root@kub tmp]# # wget https://get.helm.sh/helm-v3.0.1-linux-amd64.tar.gz
[root@kub tmp]# tar -xvzf /root/helm-v3.0.1-linux-amd64.tar.gz -C /var/tmp
[root@kub tmp]# ll /var/tmp/linux-amd64/
total 36948
-rwxr-xr-x 1 3434 3434 37818368 Dec  6 04:18 helm
-rw-r--r-- 1 3434 3434    11373 Dec  6 04:18 LICENSE
-rw-r--r-- 1 3434 3434     3248 Dec  6 04:18 README.md
[root@kub tmp]# cp /var/tmp/linux-amd64/helm /usr/local/bin/
[root@kub tmp]# helm version
version.BuildInfo{Version:"v3.0.1", GitCommit:"7c22ef9ce89e0ebeb7125ba2ebf7d421f3e82ffa", GitTreeState:"clean", GoVersion:"go1.13.4"}

```
Install Helm Server
```
```
Install nginx in kubernetes
=====
```
[root@kub ~]# kubectl create deployment nginx --image=nginx
deployment.apps/nginx created
[root@kub ~]# kubectl get deployments
NAME            READY   UP-TO-DATE   AVAILABLE   AGE
nginx           0/1     1            0           6s
root@kub ~]# kubectl create service nodeport nginx --tcp=80:80
service/nginx created
```
Verify
```
[root@kub ~]# kubectl get svc
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP        22h
nginx        NodePort    10.96.187.110   <none>        80:30338/TCP   8s
[root@kub ~]# kubectl get pods
NAME                     READY   STATUS    RESTARTS   AGE
nginx-86c57db685-9rxqx   1/1     Running   0          16h
[root@kub ~]# curl http://localhost:8001/api/v1/namespaces/default/pods/nginx-86c57db685-9rxqx/proxy/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 35em;
        margin: 0 auto;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>

```

Kubernates Proxy
=====
开启反向代理，默认是8001端口
```
[root@kub ~]# kubectl proxy
Starting to serve on 127.0.0.1:8001
```
就可以通过8001端口获取一些信息
```
[root@kub ~]# curl http://localhost:8001/api/
{
  "kind": "APIVersions",
  "versions": [
    "v1"
  ],
  "serverAddressByClientCIDRs": [
    {
      "clientCIDR": "0.0.0.0/0",
      "serverAddress": "172.17.0.2:6443"
    }
  ]
}
```

Build collector as docker
=========
1. 把collector-1.0.0-SNAPSHOT-bin.tar.gz上传到/var/tmp/,解压
2. cd collector-1.0.0-SNAPSHOT/
3. 写Dockerfile, 如下(最后要留一行空行)：
```
FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8091 50051
RUN mkdir -p /data/toop/telemetry
RUN mkdir -p /data/toop/telemetry/config
WORKDIR /data/toop/telemetry 
ADD collector-1.0.0-SNAPSHOT.jar /data/toop/telemetry/collector-1.0.0-SNAPSHOT.jar
ADD config/application.yml //data/toop/telemetry/conifg/application.yml
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=product", "-jar", "/data/toop/telemetry/collector-1.0.0-SNAPSHOT/collector-1.0.0-SNAPSHOT.jar"]


```
4. Check
```
[root@kub collector-1.0.0-SNAPSHOT]# docker image list -a
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
collector           latest              df3173d83769        4 minutes ago       169MB
<none>              <none>              a4edae9470a0        4 minutes ago       169MB
<none>              <none>              eb74058dab5b        4 minutes ago       169MB
<none>              <none>              54551c7c14e1        4 minutes ago       105MB
<none>              <none>              fdfcd01a9791        18 minutes ago      105MB
<none>              <none>              b0af31ecaf96        18 minutes ago      105MB
<none>              <none>              1f7e53c27b70        18 minutes ago      105MB
<none>              <none>              5b8d5b8613b5        18 minutes ago      105MB
kindest/node        v1.17.0             1f301b25b2d3        5 days ago          1.33GB
openjdk             8-jdk-alpine        a3562aa0b991        7 months ago        105MB

```

Install kafka in kubernetes
=============
```
[root@kub tmp]# kubectl create namespace kafka && \
> kubectl apply -k github.com/Yolean/kubernetes-kafka/variants/dev-small/?ref=v6.0.3
namespace/kafka created
role.rbac.authorization.k8s.io/pod-labler created
clusterrole.rbac.authorization.k8s.io/node-reader created
rolebinding.rbac.authorization.k8s.io/kafka-pod-labler created
clusterrolebinding.rbac.authorization.k8s.io/kafka-node-reader created
configmap/broker-config created
configmap/zookeeper-config created
service/bootstrap created
service/broker created
service/pzoo created
service/zookeeper created
service/zoo created
statefulset.apps/kafka created
statefulset.apps/pzoo created
statefulset.apps/zoo created
[root@kub kubernetes-kafka]# kubectl -n kafka get pods
NAME      READY   STATUS    RESTARTS   AGE
kafka-0   1/1     Running   4          84m
pzoo-0    1/1     Running   0          84m
[root@kub kubernetes-kafka]# kubectl -n kafka describe statefulsets.apps kafka
# kubectl -n kafka logs kafka-0 -c init-config
# kubectl -n kafka logs kafka-0 -c broker
```
=================================================================
Install kvm
====
Step 1: Install kvm

Type the following yum command:
# yum install qemu-kvm libvirt libvirt-python libguestfs-tools virt-install
Start the libvirtd service:
# systemctl enable libvirtd
# systemctl start libvirtd

Install kubernetes
===========
#yum install -y kubelet kubeadm kubectl
Install minikube
=========
refer to <https://kubernetes.io/docs/tasks/tools/install-minikube/>
```
[root@kub ~]# curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube
[root@kub ~]# sudo mkdir -p /usr/local/bin/
[root@kub ~]# sudo install minikube /usr/local/bin/
```
```
[root@kub ~]# minikube start --vm-driver=none --kubernetes-version v1.17.0 --alsologtostderr

```
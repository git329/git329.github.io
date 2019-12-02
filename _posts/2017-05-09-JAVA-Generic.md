---
layout: post
title: "Java Generic 学习小记1"
description: "Java Generic"
category: articles
tags: [java]
comments: true
---
什么是泛型(Generics)？
---
>Java泛型（generics）是JDK 5中引入的一个新特性，允许在定义类和接口的时候使用类型参数（type parameter）</br>
>泛型最精准的定义：参数化类型。具体点说就是处理的数据类型不是固定的，而是可以作为参数传入。定义泛型类、泛型接口、泛型方法，这样，同一套代码，可以用于多种数据类型.</br>
>有许多原因促成了泛型的出现，而最引人注意的一个原因，就是为了创建容器类。

原文链接:<http://liuzxc.github.io/blog/java-advance-06/> <https://segmentfault.com/a/1190000002646193>

[TOC]

为什么要引入泛型
==============
#### 创建容器类

容器类应该算得上最具重用性的类库之一。先来看一个没有泛型的情况下的容器类如何定义：
```java
public class Container {
    private String key;
    private String value;

    public Container(String k, String v) {
        key = k;
        value = v;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
```
<font color="HotPink">Container</font>类保存了一对<font color="HotPink">key-value</font>键值对，但是类型是定死的，也就说如果我想要创建一个键值对是<font color="HotPink">String-Integer</font>类型的，当前这个<font color="HotPink">Container</font>是做不到的，必须再自定义。那么这明显重用性就非常低。</br>
当然，我可以用<font color="HotPink">Object</font来代替<font color="HotPink">String</font>，并且在Java SE5之前，我们也只能这么做，由于Object是所有类型的基类，所以可以直接转型。但是这样灵活性还是不够，因为还是指定类型了，只不过这次指定的类型层级更高而已，有没有可能不指定类型？有没有可能在运行时才知道具体的类型是什么？</br>
所以，就出现了泛型。
```java
public class Container<K, V> {
    private K key;
    private V value;

    public Container(K k, V v) {
        key = k;
        value = v;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
```
在编译期，是无法知道<font color="HotPink">K</font>和<font color="HotPink">V</font>具体是什么类型，只有在运行时才会真正根据类型来构造和分配内存。可以看一下现在Container类对于不同类型的支持情况：
```java
public class Main {

    public static void main(String[] args) {
        Container<String, String> c1 = new Container<String, String>("name", "findingsea");
        Container<String, Integer> c2 = new Container<String, Integer>("age", 24);
        Container<Double, Double> c3 = new Container<Double, Double>(1.1, 2.2);
        System.out.println(c1.getKey() + " : " + c1.getValue());
        System.out.println(c2.getKey() + " : " + c2.getValue());
        System.out.println(c3.getKey() + " : " + c3.getValue());
    }
}
```
输出：
···
name : findingsea
age : 24
1.1 : 2.2
···
2. #### 泛型接口
在泛型接口中，生成器是一个很好的理解，看如下的生成器接口定义：
```java
public interface Generator<T> {
    public T next();
}
```
然后定义一个生成器类来实现这个接口：
```java
public class CandyGenerator implements Generator<String> {
    private String[] candis={"Chocolate","Hard Candy","Cherry Jelly"};
    public String next() {
        Random random = new Random();
        return candis[random.nextInt(3)];
    }
}
```
调用：
```java
public static void main(String[] args){
        CandyGenerator candyGenerator = new CandyGenerator();
        System.out.println(candyGenerator.next());
        System.out.println(candyGenerator.next());
        System.out.println(candyGenerator.next());
        System.out.println(candyGenerator.next());
}
```
输出：
```
Chocolate
Cherry Jelly
Chocolate
Cherry Jelly
```
3. #### 泛型方法
一个基本的原则是：无论何时，只要你能做到，你就应该尽量使用泛型方法。也就是说，如果使用泛型方法可以取代将整个类泛化，那么应该有限采用泛型方法。下面来看一个简单的泛型方法的定义：
```java
public class Main {

    public static <T> void out(T t) {
        System.out.println(t);
    }

    public static void main(String[] args) {
        out("findingsea");
        out(123);
        out(11.11);
        out(true);
    }
}
```
再看一个泛型方法和可变参数的例子：
```java
public class Main {

    public static <T> void out(T... args) {
        for (T t : args) {
            System.out.println(t);
        }
    }

    public static void main(String[] args) {
        out("findingsea", 123, 11.11, true);
    }
}
```
输出和前一段代码相同，可以看到泛型可以和可变参数非常完美的结合。

以上，泛型的第一部分的结束。

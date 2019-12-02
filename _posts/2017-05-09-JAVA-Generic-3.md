---
layout: post
title: "Java Generic 学习小记3"
description: "Java Generic"
category: articles
tags: [java]
comments: true
---
原文链接:<http://www.jianshu.com/p/7a1b09e62867>
[TOC]

类型参数
=======

#### 类型参数的限定

泛型的类型擦除会把所有类型参数当做Object，但是我们也可以对参数类型进行上界限定。这样类型擦除就会转换为限定类型。

##### 上界为某个具体类或接口
```java
public class Som<T extends Number> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
```
这样使用Som类，类型参数只接受Number及其子类。

当上界是泛型类或者接口的时候，上界也需要类型参数。如下：
```java
public class Som<T extends Comparable<T>> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
```
##### 上界为其他类型参数
```java
public class Som<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public <E extends T> void test(E e) {
        System.out.println("Som test: e");
    }
}
```
T是泛型类Som的参数类型，E的上界是T，也就是其它类型参数。

#### 泛型的通配符

泛型的通配符增强了方法的灵活性但也容易让人困惑。Java中有无限定通配符<?>,上界限定通配符<? extends E>,下界限定通配符<? super E>这三种通配符。

##### 无限定通配符<?>
需求：打印List中的元素。List是一个泛型类，有List<String>,List<Number>,List<Object>等可能。使用List<?>通配符，可以匹配任意List泛型。
代码如下：
```java
public static void printList(List<?> list) {
    for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
    }
}
```
看起来很简单，但是此时的list是无法进行add操作的，因为List的类型是未知的。这就是<?>的只读性，稍后会有介绍。
##### 有限通配符<? extends E>
同样是一个打印List元素的例子，但是只接受类型参数是Number及其子类。
```java
public static void printList(List<? extends Number> list) {
    for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
    }
}
```
和<?>一样，<? extends E>也具有只读性。
##### <?>和<? extends E>的只读性
通配符<?>和<? extends E>具有只读性，即可以对其进行读取操作但是无法进行写入。
```java
public static void printList(List<?> list) {
    for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
    }
    //以下操作不可以
    list.add(1);
    list.add("123");
}
```
原因在于：?就是表示类型完全无知，? extends E表示是E的某个子类型，但不知道具体子类型，如果允许写入，Java就无法确保类型安全性。假设我们允许写入，如果我们传入的参数是List<Integer>，此时进行add操作，可以添加任何类型元素，就无法保证List<Integer>的类型安全了。

##### 超类型<? super E>
超类型通配符允许写入，例子如下：
```java
public static void printList(List<? super String> list) {
    for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
    }
    list.add("123");
    list.add("456");
}
```
这个很好理解，list的参数类型是String的上界，必然可以添加String类型的元素。

##### 泛型与数组
Java不能创建泛型数组，以Som泛型类为例，以下代码编译报错：
```java
Som<String> [] soms = new Som<String>[8];
```
原因是像Integer[]和Number[]之间有继承关系，而List<Integer>和List<Number>没有，如果允许泛型数组，那么编译时无法发现，运行时也不是立即就能发现的问题会出现。参看以下代码：
```java
Som<Integer>[] soms = new Som<Integer>[3];
Object[] objs = soms;
objs[0] = new Som<String>();
```
那我们怎么存放泛型对象呢？可以使用原生数组或者泛型容器。

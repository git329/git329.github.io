---
layout: post
title: "Java Generic 学习小记2"
description: "Java Generic"
category: articles
tags: [java]
comments: true
---
原文链接:<http://liuzxc.github.io/blog/java-advance-06/> <https://segmentfault.com/a/1190000002646193>
[TOC]

Java泛型：类型擦除,类型检查，类型转换
================

正确理解泛型概念的首要前提是理解类型擦除（type erasure）。 Java中的泛型基本上都是在编译器这个层次来实现的。在生成的Java字节代码中是不包含泛型中的类型信息的。**使用泛型的时候加上的类型参数，会被编译器在编译的时候去掉。这个过程就称为类型擦除。** 如在代码中定义的List<Object>和List<String>等类型，在编译之后都会变成List。JVM看到的只是List，而由泛型附加的类型信息对JVM来说是不可见的。Java编译器会在编译时尽可能的发现可能出错的地方，但是仍然无法避免在运行时刻出现类型转换异常的情况。类型擦除也是Java的泛型实现方式与C++模板机制实现方式之间的重要区别。
#### 类型擦除

```java
Class c1 = new ArrayList<Integer>().getClass();
Class c2 = new ArrayList<String>().getClass();
System.out.println(c1 == c2);
```
Output如下：
```
true
```
显然在平时使用中，<font color="HotPink">new ArrayList<Integer>()</font>和<font color="HotPink">new ArrayList<String>()</font>是完全不同的类型，但是在这里，程序却的的确确会输出true。</br>
这就是Java泛型的类型擦除造成的，因为不管是<font color="HotPink">new ArrayList<Integer>()</font>还是<font color="HotPink">new ArrayList<String>()</font>，都在编译器被编译器擦除成了<font color="HotPink">ArrayList</font>。那编译器为什么要做这件事？</br>
原因也和大多数的Java让人不爽的点一样——兼容性。由于泛型并不是从Java诞生就存在的一个特性，而是等到SE5才被加入的，所以为了兼容之前并未使用泛型的类库和代码，不得不让编译器擦除掉代码中有关于泛型类型信息的部分，这样最后生成出来的代码其实是『泛型无关』的，我们使用别人的代码或者类库时也就不需要关心对方代码是否已经『泛化』，反之亦然。</br>
在编译器层面做的这件事（擦除具体的类型信息），使得Java的泛型先天都存在一个让人非常难受的缺点：
>在泛型代码内部，无法获得任何有关泛型参数类型的信息。

如下：
```java
List<Integer> list = new ArrayList<Integer>();
Map<Integer, String> map = new HashMap<Integer, String>();
System.out.println(Arrays.toString(list.getClass().getTypeParameters()));
System.out.println(Arrays.toString(map.getClass().getTypeParameters()));
```
output:
```
[E]
[K, V]
```
我们期待的是得到泛型参数的类型，但是实际上我们只得到了一堆占位符。
```java
public class Main<T> {

    public T[] makeArray() {
        // error: Type parameter 'T' cannot be instantiated directly
        return new T[5];
    }
}
```
我们无法在泛型内部创建一个T类型的数组，原因也和之前一样，<font color="HotPink">T</font>仅仅是个占位符，并没有真实的类型信息，实际上，除了<font color="HotPink">new</font>表达式之外，<font color="HotPink">instanceof</font>操作和转型（会收到警告）在泛型内部都是无法使用的，而造成这个的原因就是之前讲过的编译器对类型信息进行了擦除。</br>
同时，面对泛型内部形如T var;的代码时，记得多念几遍：它只是个Object，它只是个Object……
#### 类型检查和类型转换
```java
public class Main<T> {

    private T t;

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public static void main(String[] args) {
        Main<String> m = new Main<String>();
        m.set("findingsea");
        String s = m.get();
        System.out.println(s);
    }
}
```
Output:
```Output
findingsea
```
虽然有类型擦除的存在，使得编译器在泛型内部其实完全无法知道有关T的任何信息，但是编译器可以保证重要的一点：内部一致性，也是我们放进去的是什么类型的对象，取出来还是相同类型的对象，这一点让Java的泛型起码还是有用武之地的。</br>
以上代码展现就是编译器确保了我们放在t上的类型的确是T（即便它并不知道有关T的任何类型信息）。这种确保其实做了两步工作：
* set()处的类型检验
* get()处的类型转换
这两步工作也成为边界动作。

类似的code如下：
```java
public class Main<T> {

    public List<T> fillList(T t, int size) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            list.add(t);
        }
        return list;
    }

    public static void main(String[] args) {
        Main<String> m = new Main<String>();
        List<String> list = m.fillList("findingsea", 5);
        System.out.println(list.toString());
    }
}
```
```output
[findingsea, findingsea, findingsea, findingsea, findingsea]
```
代码片段同样展示的是泛型的内部一致性。

使用泛型
```java
List<Apple> box = ...;
Apple apple = box.get(0);
```
没有泛型
```java
List box = ...;
Apple apple = (Apple) list.get(0);
```
可以看到，泛型最主要的优点就是让编译器追踪参数类型，**执行类型检查和类型转换**：编译器保证类型转换不会失败。

如果依赖程序员去追踪对象类型和执行转换，那么运行时产生的错误将很难去定位和调试，然而有了泛型，编译器 可以帮助我们执行大量的类型检查，并且可以检测出更多的编译时错误。
#### 擦除的补偿
如上看到的，但凡是涉及到确切类型信息的操作，在泛型内部都是无法共工作的。那是否有办法绕过这个问题来编程，答案就是显示地传递类型标签。
```java
public class Main<T> {

    public T create(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Main<String> m = new Main<String>();
        String s = m.create(String.class);
    }
}
```
以上代码片段展示了一种用类型标签生成新对象的方法，但是这个办法很脆弱，因为这种办法要求对应的类型必须有默认构造函数，遇到Integer类型的时候就失败了，而且这个错误还不能在编译器捕获。

进阶的方法可以用限制类型的显示工厂和模板方法设计模式来改进这个问题，具体可以参见《Java编程思想 （第4版）》P382。
```java
public class Main<T> {

    public T[] create(Class<T> type) {
        return (T[]) Array.newInstance(type, 10);
    }

    public static void main(String[] args) {
        Main<String> m = new Main<String>();
        String[] strings = m.create(String.class);
    }
}
```
代码片段展示了对泛型数组的擦除补偿，本质方法还是通过显示地传递类型标签，通过<font color="HotPink">Array.newInstance(type, size)</font>来生成数组，同时也是最为推荐的在泛型内部生成数组的方法。

以上，泛型的第二部分的结束。

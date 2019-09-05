# 02| Java 内存模型：看java如何解决可见性和有序性问题
核心方案： Java 内存模型
## 定义
解决由缓存导致的可见性问题和由编译优化导致的有序性问题，合理的方法是按需禁用缓存以及编译优化。
对于并发程序，何时禁用缓存和编译优化只有程序员知道，所谓的按需也即按照程序员的要求来禁用缓存和编译优化。
为了解决可见性和有序性问题，只需要提供给程序员按需警用缓存以及编译优化的方法即可。

java 内存模型可以从不同的角度进行解读，在程序员的角度来看，本质上可以理解为：java内存模型规范了Jvm如何
提供按需禁用缓存和编译优化的方法。具体的方法有：
- volatile synchronized 和 final 三个关键字
- 六项Happens-Before规则

## volatile的困惑
最原始的意义就是禁用cpu缓存，例如volatile int x = 0，表达的是：告诉编译器，对这个变量的读写，不能使用cpu缓存，
必须从内存中读取或者写入。

```java
public  class VolatileExample{
    int x = 0;
    volatile int v = false;
    public void writer(){
       x = 42;
       v = true;
    }
    
    public void reader(){
        if(v == true){
            // 若A线程中调用了write()函数将v=true写入内存，此时B线程调用reader() 
            //　这里x会是多少呢？　0还是42?
         System.out.println(x);
        }
    }   
}
```   

存在注释中的困惑的原因是： x可能被cpu缓存而导致可见性问题。  
是0还是42取决于jdk的版本，jdk1.5后一定是42。

### 解决方案
1.5版本后jdk对volatile语义进行了加强，增强的方法是Happens_Before规则。

## Happens-Before规则
### 内涵
前面的一个操作的结果对后续的操作是可见的。即约束了编译器的优化行为，虽允许编译优化，但是要求编译器
优化后，但是要求你编译器优化后一定要遵守Happpens-Before规则。


### 内容
#### 1. 程序的顺序性规则
在一个线程中，按照程序的顺序，前面的操作Happens-Before于后续的任意操作。
即前面对于一个变量的修改对后续的操作是可见的。
#### 2. volatile变量规则
对于一个volatile变量的写操作Happens-Before于后续对于这个volatile变量的读操作。  
关联规则三可以解决volatile的困惑。
#### 3. 传递性规则
若A Happens-Before 于　B, B Happens-Before 于 C。则A Happens-Before 于　C。

结合　1 2 3解决前面的困惑：
1. x = 42 Happens-Before 于v = true
2. v = true Happens-Before 于读变量：v == true。
3. x = 42 Happens-Before于x == true

以上就是JDK5对于volatile语义的增强。　java.util.concurrent 就是通过这个来搞定可见性的。  

#### 4. 管程中的锁规则
对一个锁的解锁Happens-Before于后续对这个锁的解锁。
即a线程对于变量的写操作对于后续线程可见。
##### 管程的定义
是一种通用的同步原语，在 Java 中指的就是 synchronized，synchronized 是 Java 里对管程的实现。  

管程中的锁在 Java 里是隐式实现的。
在进入同步块之前，会自动加锁，而在代码块执行完会自动释放锁，加锁以及释放锁都是编译器帮我们实现的。
　
```java
    synchronized(this) { // 此处自动加锁
        // x 是共享变量，　初始值为10
        if(this.x < 12){
            this.x = 10;
        }   
    }// 此处自动解锁
```
#### 5. 线程start()规则
它是指主线程 A 启动子线程 B 后，子线程 B 能够看到主线程在启动子线程 B 前的操作。

换句话说就是，如果线程 A 调用线程 B 的 start() 方法（即在线程 A 中启动线程 B），那么该 start() 操作 Happens-Before 于线程 B 中的任意操作。
结合规则1及传递性，可以得出线程A　start()之前的所有操作Happens-Before于B线程中的所有操作。
例子：
```kotlin
Thread B = new Thread(() ->{
    // 主线程B.start()之前所有对共享变量的修改，此处都可见
    // 此例中，　var share == 77
})
// 此处对share进行修改
var share = 77
// 此处启动线程
B.start()
```
#### 6. 线程的join()规则
这条是关于线程等待的。它是指主线程 A 等待子线程 B 完成（主线程 A 通过调用子线程 
B 的 join() 方法实现），当子线程 B 完成后（主线程 A 中 join() 方法返回），
主线程能够看到子线程的操作。当然所谓的“看到”，指的是对 共享变量 的操作。

换句话说就是，如果在线程 A 中，调用线程 B 的 join() 并成功返回，那么线程 B 中的
任意操作 Happens-Before 于该 join() 操作的返回。

例子：
```kotlin
Thread B = new Thread(() ->{
    // 此处对共享变量的修改
    shared = 66
})
B.start()
B.join()
// 子线程对于所有共享变量的修改在
// 主线程调用B.join()之后皆可见
// 此例中　shared == 66
```
## 被忽视的final关键字
final 修饰变量时，初衷是告诉编译器：这个变量生而不变，可以可劲儿优化。 
Java 编译器在 1.5 以前的版本的确优化得很努力，以至于都优化错了。

在 1.5 以后 Java 内存模型对 final 类型变量的重排进行了约束。现在只要我们提供正确构造函数没有“逸出”，就不会出问题了。

在下面例子中，在构造函数里面将 this 赋值给了全局变量 global.obj，这就是“逸出”，线程通过 global.obj 读取 x 是有可能读到 0 的。因此我们一定要避免“逸出”。
```java
final int x;
public AClass(){
    x = 3;
    globa.obj = this;
}
```

## 总结
Happens-Before 的语义是一种因果关系。
在现实世界里，如果 A 事件是导致 B 事件的起因，
那么 A 事件一定是先于（Happens-Before）B 事件发生的，这个就是 Happens-Before 语义的现实理解。

在 Java 语言里面，Happens-Before 的语义本质上是一种可见性，A Happens-Before B 意味着 A 事件对 B 事件来说是可见的，无论 A 事件和 B 事件是否发生在同一个线程里。

Java 内存模型主要分为两部分，一部分面向你我这种编写并发程序的应用开发人员，另一部分是面向 JVM 的实现人员的
## 课后思考
有一个共享变量 abc，在一个线程里设置了 abc 的值 abc=3 ，你思考一下，有哪些办法可以让其他线程能够看到 abc==3 ？

思路：　从六项Happens-Before出发，因为提到的是多线程，所以必须要有start(), 或者join(), 或者锁的概念。

1. 主线程里设置了abc = 3后start()了B线程，由主线程A在启动子线程B之后，子线程B能够看到主线程在启动子线程A之前的操作, 即若线程A
调用了子线程B的start()函数，则start()函数操作Happens-Before于子线程B中的所有操作规则，可得线程B可以看到abc == 3。
2. 根据规则6:　主线程A等待子线程B的完成，当子线程B完成后，主线程A能够看到子线程B中的操作，即主线程A调用子线程B的join()函数并成功返回的时候，
子线程B中的所有操作Happens-Before于join()操作的返回，由此可以在子线程B中修改abc = 3, 则在主线程A中，join()之后的操作可以看到abc == 3
3. 声明共享变量abc, 并使用volatile关键字修饰abc
4. 声明共享变量abc，在synchronized关键字对abc的赋值代码块加锁，由于Happen-before管程锁的规则，可以使得后续的线程可以看到abc的值。 
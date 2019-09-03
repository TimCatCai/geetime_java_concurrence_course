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
#### 2. volatile变量规则
#### 3. 传递性规则
#### 4. 管程中的锁规则
#### 5. 线程start()规则
#### 6. 线程的join()规则

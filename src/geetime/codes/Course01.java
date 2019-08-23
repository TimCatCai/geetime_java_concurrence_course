package geetime.codes;

public class Course01 {
    // 加上volatile 关键字可以禁用缓存，运行速度会成倍下降
    // 但是最终结果仍不会是最终的结果，因而解决该问题不是简单的缓存可见性问题，可能还有原子问题
    private  long count = 0;
    private void add1Billion(){
        // 当数据为1w, 10w时结果可能正确，因为不同的线程启动有一个时间差，
        // 而在这个时间差之内，一个线程的加法操作可能已经完成。
        // 而当数据逐渐增大时，发生缓存可见性问题的可能性变大
        for(int i = 0; i < 1000000000; i++){
            count ++;
        }
    }

    public static long calc(){
        final Course01 course01 = new Course01();
        // 启动两个线程对同一个变量进行加法操作
        Thread thread1 = new Thread(() -> course01.add1Billion());
        Thread thread2 = new Thread(() -> course01.add1Billion());
        thread1.start();
        thread2.start();
        try {
            //　等待两个线程的结束
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return course01.getCount();
    }

    public long getCount() {
        return count;
    }



}

class Singleton{
    private static Singleton instance;
    public static Singleton getInstance(){
        if(instance == null){
            synchronized (Singleton.class){
                if(instance == null){
                    // 可能由于编译器优化带来空指针异常
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

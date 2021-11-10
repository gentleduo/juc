package org.duo.cacheline;

import java.util.concurrent.CountDownLatch;

public class CacheLinePadding {
    public static long count = 10_0000_0000L;

    /**
     * 1) 不加volatile情况，使不使用缓存对齐，由于StoreBuffer和Invalidate Queues的存在对性能影响不大
     * 2) 加volatile
     * 使用缓存对齐:由于两个对象处于两个不同的缓存行内，所以使用缓存锁，并且每个线程锁定不同的缓存行不存在锁竞争所以性能高
     * 不使用缓存对齐:由于两个对象处于同一缓存行内，所以使用总线锁，并且存在锁竞争相当于串行所以性能低
     * <p>
     * 加不加volatile缓存一致性协议都存在。只不过加了volatile之后由于缓存行被锁定，所以另一个线程要等待锁的释放，并且通过缓存一致性协议读取最新的值
     */
    private static class T {
        private long p1, p2, p3, p4, p5, p6, p7;
        public volatile long x = 0L;
        //public long x = 0L;
        private long p9, p10, p11, p12, p13, p14, p15;
    }

    public static T[] arr = new T[2];

    static {
        arr[0] = new T();
        arr[1] = new T();
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = 0; i < count; i++) {
                    arr[0].x = i;
                }
                latch.countDown();
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = 0; i < count; i++) {
                    arr[1].x = i;
                }
                latch.countDown();
            }
        });
        final long start = System.nanoTime();
        t1.start();
        t2.start();
        latch.await();
        System.out.println((System.nanoTime() - start) / 100_0000);

    }
}

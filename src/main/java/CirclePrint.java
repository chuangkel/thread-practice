import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class CirclePrint {

    private static ReentrantLock lock = new ReentrantLock(true);
    private static Condition conditionA = lock.newCondition();
    private static Condition conditionB = lock.newCondition();
    private static Condition conditionC = lock.newCondition();
    private static String c;

    private static CountDownLatch latch1 = new CountDownLatch(1);
    private static CountDownLatch latch2 = new CountDownLatch(1);

    public static void main(String[] args) {
        PrintThread task1 = new PrintThread(lock, conditionA, conditionB, conditionC, "阿", latch1,latch2);
        PrintThread task2 = new PrintThread(lock, conditionB, conditionC, conditionA, "里", latch1,latch2);
        PrintThread task3 = new PrintThread(lock, conditionC, conditionA, conditionB, "巴巴", latch1,latch2);

        new Thread(task1).start();
        new Thread(task2).start();
        new Thread(task3).start();
    }

    public static class PrintThread implements Runnable {
        ReentrantLock lock;
        Condition conditionA;
        Condition conditionB;
        Condition conditionC;
        CountDownLatch latch1;
        CountDownLatch latch2;
        String c;

        public PrintThread(ReentrantLock lock, Condition conditionA, Condition conditionB, Condition conditionC, String c, CountDownLatch latch1,CountDownLatch latch2) {
            this.lock = lock;
            this.conditionA = conditionA;
            this.conditionB = conditionB;
            this.conditionC = conditionC;
            this.c = c;
            this.latch1 = latch1;
            this.latch2 = latch2;
        }

        public void run() {

            try {
                if ("阿".equals(c)) {
                    lock.lock();
                    latch1.countDown();
                } else {
                    latch1.await();
                    if("里".equals(c)){
                        lock.lock();
                        latch2.countDown();
                    }else{
                        latch2.await();
                        lock.lock();
                    }
                }

                for (int i = 0; i < 10; i++) {
                    System.out.print(c);
                    if ("阿".equals(c)) {
                        conditionB.signal();
                        conditionA.await();
                    }
                    if ("里".equals(c)) {
                        conditionC.signal();
                        conditionB.await();
                    }
                    if ("巴巴".equals(c)) {
                        conditionA.signal();
                        if (i < 9) {
                            conditionC.await();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();

            }
        }
    }
}
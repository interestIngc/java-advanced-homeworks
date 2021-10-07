package info.kgeorgiy.ja.Anikina.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> currThreads;
    private final Deque<Runnable> tasks;

    private static class Collector<R> {

        private final List<R> result;
        private int size;
        private final int argsSize;

        Collector(int argsSize) {
            this.argsSize = argsSize;
            result = new ArrayList<>(Collections.nCopies(argsSize, null));
            size = 0;
        }

        synchronized void addValue(int pos, R value) {
            result.set(pos, value);
            size++;
            notifyAll();
        }

        synchronized List<R> getResult() throws InterruptedException {
            while (size < argsSize) {
                wait();
            }
            return result;
        }
    }


    public ParallelMapperImpl(final int threads) {
        currThreads = new ArrayList<>();
        tasks = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            currThreads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                tasks.wait();
                            }
                            task = tasks.poll();
                            tasks.notifyAll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {

                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            currThreads.get(i).start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Collector<R> result = new Collector<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            synchronized (tasks) {
                int finalI = i;
                tasks.add(() -> result.addValue(finalI, f.apply(args.get(finalI))));
                tasks.notifyAll();
            }
        }
        return result.getResult();
    }

    @Override
    public void close() {
        currThreads.forEach(Thread::interrupt);
        for (Thread thread : currThreads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {

            }
        }
    }
}

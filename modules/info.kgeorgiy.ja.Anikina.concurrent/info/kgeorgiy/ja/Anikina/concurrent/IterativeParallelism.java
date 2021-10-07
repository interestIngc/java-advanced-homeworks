package info.kgeorgiy.ja.Anikina.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.min;

public class IterativeParallelism implements ListIP {

    ParallelMapper parallelMapper;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    <T> List<Stream<? extends T>> distribute(int threads, List<? extends T> values) {
        int currThreads = min(threads, values.size());
        int numberOfValues = values.size() / currThreads;
        int rest = values.size() - numberOfValues * currThreads;
        List<Stream<? extends T>> distribution = new ArrayList<>(currThreads);
        int ind = 0;
        for (int i = 0; i < currThreads; i++) {
            int currCnt = numberOfValues;
            if (rest > 0) {
                currCnt++;
                rest--;
            }
            distribution.add(values.subList(ind, ind + currCnt).stream());
            ind += currCnt;
        }
        return distribution;
    }

    <T, R> R work(int threads, List<? extends T> values,
                  Function<Stream<? extends T>, R> function, Function<Stream<? extends R>, ? extends R> reduce)
            throws InterruptedException {
        if (threads <= 0) {
            System.err.println("can't divide into 0 threads");
            return null;
        }
        List<Stream<? extends T>> distribution = distribute(threads, values);
        List<R> currResult;

        if (parallelMapper == null) {
            currResult = new ArrayList<>(Collections.nCopies(distribution.size(), null));
            List<Thread> currThreads = new ArrayList<>(distribution.size());
            for (int i = 0; i < distribution.size(); i++) {
                int finalI = i;
                currThreads.add(new Thread(() ->
                        currResult.set(finalI, function.apply(distribution.get(finalI)))
                ));
                currThreads.get(i).start();
            }
            for (Thread thread : currThreads) {
                thread.join();
            }
        } else {
            currResult = parallelMapper.map(function, distribution);
        }
        return reduce.apply(currResult.stream());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return work(threads, values, x -> x.max(comparator).orElse(null),
                x -> x.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return work(threads, values, x -> x.allMatch(predicate), x -> x.allMatch(y -> y.equals(Boolean.TRUE)));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return work(threads, values, x -> x.anyMatch(predicate), x -> x.anyMatch(y -> y.equals(Boolean.TRUE)));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return work(threads,
                values,
                x -> x.map(Object::toString).collect(Collectors.joining()),
                x -> x.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return work(threads, values,
                x -> x.filter(predicate).collect(Collectors.toList()),
                x -> x.flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return work(threads, values,
                x -> x.map(f).collect(Collectors.toList()),
                x -> x.flatMap(List::stream).collect(Collectors.toList()));
    }
}

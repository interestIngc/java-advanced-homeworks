package info.kgeorgiy.ja.Anikina.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final Set<String> links = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<String, IOException> errors;
    private final ConcurrentMap<String, DownloadersQueue> barrierConcurrentMap = new ConcurrentHashMap<>();

    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
        this.perHost = perHost;
        errors = new ConcurrentHashMap<>();
    }

    private static int getArgOrDefault(int pos, String[] args) {
        return pos < args.length ? Integer.parseInt(args[pos]) : 1;
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 5) {
            System.err.println("invalid number of args!");
            return;
        }
        try {
            String url = args[0];
            int depth = getArgOrDefault(1, args);
            int downloaders = getArgOrDefault(2, args);
            int extractors = getArgOrDefault(3, args);
            int perHost = getArgOrDefault(4, args);
            try (Crawler crawler = new WebCrawler(
                    new CachingDownloader(),
                    downloaders,
                    extractors,
                    perHost)) {
                crawler.download(url, depth);
            } catch (IOException e) {
                System.err.println("failed");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("invalid argument passed");
        }
    }

    @Override
    public Result download(String url, int depth) {
        links.add(url);
        Phaser phaser = new Phaser(1);
        dfs(url, depth, phaser);
        phaser.arriveAndAwaitAdvance();
        links.removeAll(errors.keySet());
        return new Result(new ArrayList<>(links), errors);
    }

    private void dfs(String url, int depth, Phaser phaser) {
        String host;
        try {
            host = URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return;
        }
        DownloadersQueue downloadersQueue = barrierConcurrentMap.computeIfAbsent(host, h -> new DownloadersQueue());
        phaser.register();
        downloadersQueue.add(() -> {
            try {
                Document doc = downloader.download(url);
                if (depth > 1) {
                    phaser.register();
                    extractors.submit(() -> {
                        try {
                            doc.extractLinks().stream().filter(links::add).
                                    forEach(link -> dfs(link, depth - 1, phaser));
                        } catch (IOException ignored) {
                        } finally {
                            phaser.arrive();
                        }
                    });
                }
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        });
    }

    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
        try {
            extractors.awaitTermination(5, TimeUnit.SECONDS);
            downloaders.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interuppted: %s" + e.getMessage());
        }
    }



    private class DownloadersQueue {
        private int currentNumber;
        private final Queue<Runnable> tasks;

        public DownloadersQueue() {
            tasks = new ArrayDeque<>();
            currentNumber = 0;
        }

        private synchronized void tryAdd() {
            if (currentNumber < perHost) {
                Runnable task = tasks.poll();
                if (task != null) {
                    currentNumber++;
                    downloaders.submit(() -> {
                        task.run();
                        currentNumber--;
                        tryAdd();
                    });
                }
            }
        }

        private synchronized void add(Runnable task) {
            tasks.add(task);
            tryAdd();
        }
    }
}

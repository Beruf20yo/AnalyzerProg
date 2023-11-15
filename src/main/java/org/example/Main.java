package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static final HashMap<Character, String> sizeToFreq = new HashMap<>(3);
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);
    private static final int maxLength = 10_000;

    public static void main(String[] args) throws InterruptedException {
        long startTs = System.currentTimeMillis();
        sizeToFreq.put('a', "");
        sizeToFreq.put('b', "");
        sizeToFreq.put('c', "");
        Thread fillQueue = new Thread(() ->
        {
            for (int i = 0; i < 10_000; i++) {
                String newText = generateText("abc", 100_000);
                try {
                    queueA.put(newText);
                    queueB.put(newText);
                    queueC.put(newText);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        fillQueue.start();
        List<Thread> threads = new ArrayList<>();
        threads.add(createThread(queueA, 'a'));
        threads.add(createThread(queueB, 'b'));
        threads.add(createThread(queueC, 'c'));
        for (var th : threads) {
            th.start();
        }
        for (var th : threads) {
            th.join();
        }
        long endTs = System.currentTimeMillis();
        System.out.println("Time: " + (endTs - startTs) + "ms");
    }

    private static int findLetterCount(String fullStr, char letter) {
        char[] chars = fullStr.toCharArray();
        int count = 0;
        for (char ch : chars) {
            if (ch == letter) {
                count++;
            }
        }
        return count;
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static Thread createThread(BlockingQueue<String> queue, char ch) {
        return new Thread(() ->
        {
            int maxCount = 0;
            for (int i = 0; i < maxLength; i++) {
                try {
                    String taken = queue.take();
                    int count = findLetterCount(taken, ch);
                    if (count > maxCount) {
                        maxCount = count;
                        sizeToFreq.replace(ch, taken);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
            System.out.println("Строка с максимальным чилом C = " + maxCount + "\n" + sizeToFreq.get(ch));
        });
    }
}
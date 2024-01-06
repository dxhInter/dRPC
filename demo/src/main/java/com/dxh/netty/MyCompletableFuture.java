package com.dxh.netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyCompletableFuture {
    public static void main(String[] args) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture();
        new Thread(() -> {
            int i = 0;
            completableFuture.complete(i);
        }).start();
        try {
            Integer integer = completableFuture.get();
            System.out.println("integer = " + integer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.nathanmcunha.minispring;

   public class MiniSpringApp {
    public static void main(String[] args) {
        System.out.println("Java Version: " + System.getProperty("java.version"));

        var vThread = Thread.ofVirtual().name("my-virtual-thread").start(() -> {
            System.out.println("Running Inside: " + Thread.currentThread());
        });

        try { vThread.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }

   }

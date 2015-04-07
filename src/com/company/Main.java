package com.company;

public class Main {

    public static void main(String[] args) {
        System.out.println("TempFolder: " + Properties.BASE_DOWNLOAD_URI);
        CommandThread commandThread = new CommandThread();
        commandThread.start();
    }
}

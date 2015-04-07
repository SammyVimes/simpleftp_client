package com.company;

import java.util.Queue;
import java.util.Scanner;

/**
 * Created by Семён on 07.04.2015.
 */
public class CommandThread extends Thread {

    private boolean authed = false;

    private EventThread eventThread = new EventThread();

    private Handler handler = new Handler(eventThread) {
        @Override
        public void handle(EventThread.Event event) {
            String type = event.getType();
            System.out.println("" + type);
            FTP.Message message = (FTP.Message) event.getData();
            if (message.getCode() == FTP.FTPCode.COMMAND_FAILED) {
                System.out.println("Failed to execute command: " + message);
                return;
            }
            System.out.println("RESPONSE: " + message);
            switch (type) {
                case "GREET":
                    break;
                case "USER":
                    ftp.authPassword(Properties.PASSWORD);
                    break;
                case "RETR":
                    String[] retrRsult = message.getData().split(" ");
                    int fileSize = Integer.parseInt(retrRsult[0]);
                    ftp.loadFile(fileSize, retrRsult[1]);
                    break;
                case "PASS":
                    authed = true;
                    break;
                case "PASV":
                    String address = message.getData();
                    String[] a = address.split(":");
                    ftp.initDataSocket(a[0], Integer.parseInt(a[1]));
                    break;
                case "LIST":
                    break;
                case "FILE_SUCCESS":
                    System.out.println("File downloaded");
                    break;
            }
        }
    };

    private FTP ftp = new FTP(handler, eventThread);

    @Override
    public void run() {
        eventThread.start();
        ftp.init();
        ftp.authUser(Properties.USER_NAME);
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while((line = scanner.nextLine()) != null) {
            if (authed) {
                ftp.sendCommand(line);
            }
        }
    }



}

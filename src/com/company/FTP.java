package com.company;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Семён on 07.04.2015.
 */
public class FTP extends Handler {

    private List<String> files = null;
    private String userName;
    private String password;
    private Handler handler;



    private Socket connectionSocket = null;
    private Socket dataConnectionSocket = null;
    private BufferedReader inputStream = null;
    private OutputStreamWriter outputStream = null;

    public FTP(Handler handler, EventThread eventThread) {
        super(eventThread);
        this.handler = handler;
        try {
            connectionSocket = new Socket(Properties.HOST, Properties.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(EventThread.Event event) {
        try {
            String type = event.getType();
            String[] arr = type.split(" ");
            outputStream.write(type);
            outputStream.flush();
            String inputLine;
            String fullLine = "";
            int lines = 0;
            try {
//            while ((inputLine = inputStream.readLine()) != null)
//                fullLine += inputLine;
                fullLine = inputStream.readLine();
                if ("LIST".equals(type)) {
                    lines = Integer.parseInt(fullLine.split(" ")[1]);
                }
                for (int i = 0; i < lines; i++) {
                    fullLine += "\r\n" + inputStream.readLine();
                    if (fullLine == null) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message message = parseResponse(fullLine);
            if (type.contains("RETR")) {
                message.data += " " + arr[1];
                handler.addEvent(arr[0], message);
            } else {
                handler.addEvent(arr[0], message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initDataSocket(final String host, final int port) {
        try {
            dataConnectionSocket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFile(final int size, final String fileName) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    DataInputStream dis = new DataInputStream(dataConnectionSocket.getInputStream());

                    fos = new FileOutputStream(Properties.BASE_DOWNLOAD_URI + fileName, false);

                    byte[] bytes = new byte[size];
                    dis.read(bytes, 0, size);
                    fos.write(bytes);
                    fos.flush();
                    handler.addEvent("FILE_SUCCESS", new Message(FTPCode.COMMAND_SUCCEED, ""));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        thread.start();
    }

    public void init() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(
                    connectionSocket.getInputStream()));
            outputStream = new OutputStreamWriter(
                    connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String inputLine;
        String fullLine = "";
        try {
//            while ((inputLine = inputStream.readLine()) != null)
//                fullLine += inputLine;
            fullLine = inputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message message = parseResponse(fullLine);
        handler.addEvent("GREET", message);
    }

    public static class Message {

        private FTPCode code;

        private String data;

        public Message(FTPCode code, String data) {
            this.code = code;
            this.data = data;
        }

        public FTPCode getCode() {
            return code;
        }

        public void setCode(FTPCode code) {
            this.code = code;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return code.toString() + " " + data;
        }
    }

    private Message parseResponse(final String input) {
        String[] array = input.split(" ");
        Integer c = Integer.valueOf(array[0]);
        String data = "";
        if (array.length > 1) {
            data = input.substring(array[0].length() + 1);
        }
        Message message = new Message(FTPCode.valueOf(c), data);
        return message;
    }

    private enum FTPCode {
        COMMAND_SUCCEED(240),
        COMMAND_FAILED(140),
        COMMAND_CONNECTION_CLOSED(520),
        GREET(220);

        private int code;

        FTPCode(final int code) {
            this.code = code;
        }

        public static FTPCode valueOf(final int code) {
            for (FTPCode _code : values()) {
                if (_code.code == code) {
                    return _code;
                }
            }
            return null;
        }

    }

    public void authUser(final String userName) {
        sendCommand("USER " + userName);
    }

    public void authPassword(final String password) {
        sendCommand("PASS " + password);
    }

    public void sendCommand(final String command) {
        addEvent(command, null);
    }

}

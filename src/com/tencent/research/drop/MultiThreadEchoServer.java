package com.tencent.research.drop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程的Server实现
 * 阻塞式的IO,客户端的慢速会拖累server的处理线程
 * Created by niuniuzhang on 17/2/23.
 */
public class MultiThreadEchoServer {
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    static class HandleMsg implements Runnable {
        Socket clientSocket;

        public HandleMsg(Socket clientSokcet) {
            this.clientSocket = clientSokcet;
        }

        @Override
        public void run() {
            BufferedReader is = null;
            PrintWriter os = null;
            try {
                is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                os = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine = null;
                long start = System.currentTimeMillis();
                while ((inputLine = is.readLine()) != null) {
                    System.out.println("[Server]Received, content=" + inputLine);
                    os.println(inputLine);
                }
                long used = System.currentTimeMillis() - start;
                System.out.println("[Server]Spend " + used + " ms for client " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String args[]) {
        ServerSocket echoServer = null;
        Socket clientSocket = null;
        try {
            echoServer = new ServerSocket(8000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                clientSocket = echoServer.accept();
                System.out.println("[Server]Accepted, client=" + clientSocket.getRemoteSocketAddress());
                threadPool.execute(new HandleMsg(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

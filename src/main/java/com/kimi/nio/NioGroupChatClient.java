package com.kimi.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author 郭富城
 */
public class NioGroupChatClient {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 5905;
    private SocketChannel socketChannel;
    private String username;

    public NioGroupChatClient() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(HOST,PORT));
            socketChannel.configureBlocking(false);
            username = socketChannel.getLocalAddress().toString().substring(1);
            System.out.println("客户端启动.....");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取消息
     */
    public void read(){
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            socketChannel.read(buf);
            String msg = new String(buf.array()).trim();
            if (!"".equals(msg)){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送消息
     * @param msg 消息
     */
    public void send(String msg){
        try {
            socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            System.out.println("服务器故障！");
        }
    }

    public static void main(String[] args) {
        NioGroupChatClient client = new NioGroupChatClient();

        Executors.newSingleThreadExecutor().submit(()->{
            while (true){
                client.read();
                // 睡三秒读取一次
                TimeUnit.SECONDS.sleep(3);
            }
        });

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String msg = scanner.next();
            client.send(msg);
        }
    }


}

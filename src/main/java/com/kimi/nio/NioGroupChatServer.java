package com.kimi.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


/**
 * @author 郭富城
 */
public class NioGroupChatServer {

    private Selector selector;
    private ServerSocketChannel listenChannel;

    public static final int PORT = 5905;

    public NioGroupChatServer() {

        try {
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress("127.0.0.1",PORT));
            listenChannel.configureBlocking(false);
            // 监听注册事件
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听
     */
    public void listen() {
        try {
            while (true) {
                int active = selector.select(3000);
                // 有事件发生
                if (active>0){
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()){
                        SelectionKey next = iterator.next();
                        // 删除当前key
                        iterator.remove();

                        // 监听到accept
                        if (next.isAcceptable()){
                            SocketChannel sc = listenChannel.accept();
                            // 接收到的客户端的channel也注册到selector上
                            sc.configureBlocking(false);
                            sc.register(selector,SelectionKey.OP_READ);

                            sc.write(ByteBuffer.wrap("连接建立".getBytes()));
                            System.out.println(sc.getRemoteAddress()+"上线");
                        }
                        // 监听到读事件,通道可读时
                        else if (next.isReadable()){
                            read(next);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取客户端消息
     */
    private void read(SelectionKey key){
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) key.channel();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            int read = channel.read(buf);
            if (read>0){
                String msg = new String(buf.array());
                System.out.println("接收到客户端 "+channel.getRemoteAddress()+" 消息："+msg);

                write("用户 "+ channel.getRemoteAddress()+" 说：" + msg);
            }

        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress()+" 离线");
                key.cancel();
                channel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 给客户端发送消息
     * @param msg 消息
     */
    private void write(String msg){
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel){
                SocketChannel target = (SocketChannel) key.channel();
                try {
                    target.write(ByteBuffer.wrap(msg.getBytes()));
                } catch (IOException e) {
                    try {
                        System.out.println("客户端 "+target.getRemoteAddress()+" 下线");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        NioGroupChatServer server = new NioGroupChatServer();
        server.listen();
    }

}























package com.kimi.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

/**
 * @author 郭富城
 */
public class NettyGroupChatClient {

    public NettyGroupChatClient() {
    }

    public void run(String host,int port){
        EventLoopGroup group = new NioEventLoopGroup(8);
        Bootstrap bootstrap = new Bootstrap();
        try{
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder",new StringDecoder())
                                    .addLast("encoder",new StringEncoder())
                                    .addLast(new ClientHandler());
                        }
                    });

            ChannelFuture sync = null;
            try {
                sync = bootstrap.connect(host, port).sync();
                Channel channel = sync.channel();
                System.out.println("当前用户："+channel.localAddress());

                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNext()){
                    String msg = scanner.next();
                    // 发送消息至服务器
                    channel.writeAndFlush(msg+"\r\n");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyGroupChatClient().run("127.0.0.1",5905);
    }
}

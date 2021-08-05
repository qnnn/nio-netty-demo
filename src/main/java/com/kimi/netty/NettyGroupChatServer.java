package com.kimi.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author 郭富城
 */
public class NettyGroupChatServer {
    public NettyGroupChatServer(){

    }
    public void run(int port){

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(8);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加解码器和编码器
                            pipeline.addLast("decoder",new StringDecoder())
                                    .addLast("encoder",new StringEncoder())
                                    // 业务处理
                                    .addLast(new ServerHandler());
                        }
                    });

            System.out.println("服务开启");
            try {
                // 同步绑定,bootstrap里的任务如监听器等都是异步的，同步等待绑定完成
                ChannelFuture sync = bootstrap.bind(port).sync();
                // 监听关闭
                sync.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new NettyGroupChatServer().run(5905);
    }

}




















package com.kimi.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 郭富城
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    /**
     * 全局channel管理
     */
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 活跃状态，提示上线
     * @param ctx 客户端
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" 上线");
    }

    /**
     * 不活跃状态
     * @param ctx 客户端
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" 离线 "+ time.format(new Date()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    /**
     * 连接建立
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        CHANNEL_GROUP.writeAndFlush(channel.remoteAddress()+" 进入 " +time.format(new Date()) +"\n");
        CHANNEL_GROUP.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        CHANNEL_GROUP.writeAndFlush(channel.remoteAddress()+" 离开了 " +time.format(new Date()) +"\n");
        System.out.println("现存连接为："+ CHANNEL_GROUP.size());
    }

    /**
     * 数据处理
     * @param ctx 客户端
     * @param msg 消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel ch = ctx.channel();
        CHANNEL_GROUP.writeAndFlush(ch.remoteAddress()+" 说："+msg+" " +time.format(new Date()) +"\n", channel -> channel!=ch);
        CHANNEL_GROUP.writeAndFlush("我说："+msg+" " +time.format(new Date()) +"\n",channel -> channel==ch);

    }
}

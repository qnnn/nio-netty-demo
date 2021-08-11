package com.kimi.netty;

import com.kimi.entity.Comment;
import com.kimi.entity.Pyq;
import com.kimi.utils.JedisUtils;
import com.kimi.utils.MapperUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 郭富城
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    /**
     * 全局channel管理
     */
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 在线用户
     */
    private static final ConcurrentHashMap<Channel,String> CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,Channel> USER_MAP = new ConcurrentHashMap<>();

    /**
     * 暂存离线消息
     */
    private static final ConcurrentHashMap<String,List<Pyq>> MSG_MAP = new ConcurrentHashMap<>();

    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Jedis jedis = JedisUtils.getJedis();


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
        CHANNEL_GROUP.add(channel);

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        CHANNEL_GROUP.writeAndFlush(channel.remoteAddress()+" 离开了 " +time.format(new Date()) +"\n");
        String userId = CHANNEL_MAP.get(channel);
        CHANNEL_MAP.remove(channel);
        USER_MAP.remove(userId);
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
        msg = msg.trim();

        // 用户id
        if (msg.startsWith(MsgStatus.USER)) {
            String userId = msg.substring(MsgStatus.USER.length());
            CHANNEL_MAP.put(ch,userId);
            USER_MAP.put(userId,ch);

            // 载入离线时产生的消息
            if (MSG_MAP.containsKey(userId)){
                List<Pyq> offMsg = MSG_MAP.get(userId);
                for (Pyq pyq : offMsg) {
                    String json = MapperUtils.obj2json(pyq);
                    CHANNEL_GROUP.writeAndFlush(json+"\n",channel -> channel==ch);
                }
            }
            MSG_MAP.remove(userId);
        }

        // 发布朋友圈
        else if (msg.startsWith(MsgStatus.PUBLISH)){
            String content = msg.substring(MsgStatus.PUBLISH.length());
            Pyq pyq = new Pyq()
                    .setContent(content)
                    .setUserId(CHANNEL_MAP.get(ch))
                    .setDate(new Date())
                    .setMessageId("msg1");
            // 获取朋友列表
            Set<String> friends = jedis.smembers(CHANNEL_MAP.get(ch));
            // 存放消息id
            jedis.set("publish:"+pyq.getMessageId(),pyq.getUserId());
            // 若在线则发送
            for (String friend : friends) {
                CHANNEL_GROUP.writeAndFlush(pyq+"\n",channel -> channel==USER_MAP.get(friend));

                // 暂存消息
                if (!USER_MAP.containsKey(friend)){
                    List<Pyq> offMsg = MSG_MAP.getOrDefault(friend, new ArrayList<>());
                    offMsg.add(pyq);
                    MSG_MAP.put(friend,offMsg);
                }
            }
        }
        // 评论
        else if (msg.startsWith(MsgStatus.MSG)){
            String temp = msg.substring(MsgStatus.MSG.length());
            // 输入格式 msg:messageId replyUserId userId comment
            String[] info = temp.split("-");
            Comment comment = new Comment()
                    .setMessageId(info[0])
                    .setReplyUserId(info[1])
                    .setUserId(info[2])
                    .setContent(info[3])
                    .setDate(new Date());
            String json = MapperUtils.obj2json(comment);
            jedis.rpush("msg:"+info[0],json);
            CHANNEL_GROUP.writeAndFlush("好友: "+comment.getUserId()+" 评论了你!\n",channel -> channel==USER_MAP.get(jedis.get("publish:"+comment.getMessageId())));
        }
        // 点赞
        else if (msg.startsWith(MsgStatus.STAR)){
            String temp = msg.substring(MsgStatus.STAR.length());
            String[] star = temp.split("-");
            // 输入格式 star:messageId userId
            jedis.sadd("star:"+star[0],star[1]);
            CHANNEL_GROUP.writeAndFlush("好友: "+star[1]+" 点赞了你!\n",channel -> channel == USER_MAP.get(jedis.get("publish:"+star[0])));
        }
        // 取消点赞
        else if (msg.startsWith(MsgStatus.UN_STAR)){
            String temp = msg.substring(MsgStatus.UN_STAR.length());
            String[] unStar = temp.split("-");
            // 输入格式 star:messageId userId
            jedis.srem("star:"+unStar[0],unStar[1]);
        }
        // 获取点赞信息
        else if (msg.startsWith(MsgStatus.GET_STAR)){
            String temp = msg.substring(MsgStatus.GET_STAR.length());
            String[] getStar = temp.split("-");
            // 输入格式 star:messageId userId
            String userId = jedis.get("publish:" + getStar[0]);
            // 自己发的pyq
            if (userId.equals(CHANNEL_MAP.get(ch))){
                Set<String> users = jedis.smembers("star:" + getStar[0]);
                CHANNEL_GROUP.writeAndFlush(users+"\n",channel -> channel==ch);
            }
            else {
                Set<String> users = jedis.sinter("star:" + getStar[0], CHANNEL_MAP.get(ch));
                CHANNEL_GROUP.writeAndFlush(users+"\n",channel -> channel==ch);
            }
        }
        // 获取评论信息
        else if (msg.startsWith(MsgStatus.GET_MSG)){
            String temp = msg.substring(MsgStatus.GET_MSG.length());
            String[] getMsg = temp.split("-");
            // 输入格式 getMsg:messageId userId
            List<String> commends = jedis.lrange("msg:"+getMsg[0], 0, -1);
            // 不是自己发的
            if (!getMsg[1].equals(jedis.get("publish:"+getMsg[0]))){
                Set<String> friends = jedis.smembers(getMsg[1]);
                Iterator<String> iterator = commends.iterator();
                while (iterator.hasNext()){
                    String commend = iterator.next();
                    Comment c = (Comment)MapperUtils.json2pojo(commend,Comment.class);
                    // 不是好友，或回复的不是自己的好友
                    if (!friends.contains(c.getUserId())){
                        iterator.remove();
                    }else if (!"null".equals(c.getReplyUserId())&&!friends.contains(c.getReplyUserId())){
                        iterator.remove();
                    }
                }

            }
            CHANNEL_GROUP.writeAndFlush(commends+"\n",channel -> channel==USER_MAP.get(getMsg[1]));
        }

    }
}


class MsgStatus{
    public static final String USER = "user:";
    public static final String PUBLISH = "publish:";
    public static final String MSG = "msg:";
    public static final String STAR = "star:";
    public static final String UN_STAR = "unStar:";
    public static final String GET_STAR = "getStar:";
    public static final String GET_MSG = "getMsg:";
}

# nio-netty-demo

## netty+jedis
模拟朋友圈点赞、评论、上线推送

### 简略流程图
![image-20210811185405549](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/%E6%B5%81%E7%A8%8B%E5%9B%BE.png?raw=true)



**u_1000**

![image-20210811185405549](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/u_1000.png?raw=true)

**用户u_1000看到的消息**
```json
[
    {
        "messageId": "msg1",
        "userId": "u_1001",
        "replyUserId": "null",
        "content": "心情不错呢！",
        "date": 1628678896442
    },
    {
        "messageId": "msg1",
        "userId": "u_1002",
        "replyUserId": "u_1001",
        "content": "你也是呢！",
        "date": 1628678915333
    },
    {
        "messageId": "msg1",
        "userId": "u_1003",
        "replyUserId": "null",
        "content": "我这边也不错！",
        "date": 1628678950608
    }
]
```





**u_1001**

![image-20210811185424093](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/u_1001.png?raw=true)
**用户u_1001看到的消息，因为不是u_1003的好友，看不到该用户发送的消息**

```json
[
    {
        "messageId": "msg1",
        "userId": "u_1001",
        "replyUserId": "null",
        "content": "心情不错呢！",
        "date": 1628678896442
    },
    {
        "messageId": "msg1",
        "userId": "u_1002",
        "replyUserId": "u_1001",
        "content": "你也是呢！",
        "date": 1628678915333
    }
]
```





**u_1002**

![image-20210811185439886](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/u_1002.png?raw=true)


**u_1003**

![image-20210811185453352](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/u_1003.png?raw=true)
**u_1003看到的消息，因为没有好友u_1001,且好友u_1002的消息是回复u_1001的，也看不到好友u_1002的消息**

```json
[
    {
        "messageId": "msg1",
        "userId": "u_1003",
        "replyUserId": "null",
        "content": "我这边也不错！",
        "date": 1628678950608
    }
]
```





**u_1004上线，推送离线消息**

![image-20210811185527132](https://github.com/qnnn/nio-netty-chat-demo/blob/master/photo/u_1004.png?raw=true)


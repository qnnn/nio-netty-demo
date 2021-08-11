package com.kimi.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 郭富城
 */
@Data
@Accessors(chain = true)
public class Comment implements Serializable {

    private String messageId;
    private String userId;
    private String replyUserId;
    private String content;
    private Date date;
}

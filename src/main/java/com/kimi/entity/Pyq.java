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
public class Pyq implements Serializable {

    private String userId;
    private String messageId;
    private String content;
    private Date date;

}

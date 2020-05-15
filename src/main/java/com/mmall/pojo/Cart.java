package com.mmall.pojo;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Cart {
    private Integer id;

    private Integer userId;

    private Integer productId; // 产品id

    private Integer quantity; // 数量

    private Integer checked; // 是否选中

    private Date createTime;

    private Date updateTime;
}
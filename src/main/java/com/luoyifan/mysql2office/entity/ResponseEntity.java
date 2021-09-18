package com.luoyifan.mysql2office.entity;

import lombok.Data;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Data
public class ResponseEntity<T> {
    private Boolean success;
    private String msg;
    private T data;
}

package com.luoyifan.mysql2office.entity;

import lombok.Data;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Data
public class FieldInfo {
    private Integer index;
    private String name;
    private String type;
    private Boolean nullable;
    private String defaultVal;
    private String comment;
}

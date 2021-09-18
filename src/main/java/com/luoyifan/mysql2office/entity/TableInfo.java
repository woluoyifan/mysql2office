package com.luoyifan.mysql2office.entity;

import lombok.Data;

import java.util.List;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Data
public class TableInfo {
    private String name;
    private String comment;
    private List<FieldInfo> fieldInfoList;
}

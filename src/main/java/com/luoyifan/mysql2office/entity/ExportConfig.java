package com.luoyifan.mysql2office.entity;

import lombok.Data;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Data
public class ExportConfig {
    private String host;
    private Integer port;
    private String user;
    private String password;
    private String connProps;
    private String dbName;
    private String tableNameTitle;
    private String tableCommentTitle;
    private String fieldNameTitle;
    private String fieldTypeTitle;
    private String fieldNullableTitle;
    private String fieldDefaultValueTitle;
    private String fieldCommentTitle;
    private String exportType;
}

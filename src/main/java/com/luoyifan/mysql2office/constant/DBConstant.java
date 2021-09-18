package com.luoyifan.mysql2office.constant;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
public class DBConstant {
    public static final String DB_URL = "jdbc:mysql://%s:%d?%s";

    public static final int META_DATA_TABLE_NAME_COLUMN_INDEX = 3;

    public static final String TABLE_LIST_SQL = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='%s'";

    public static final String TABLE_NAME_COLUMN_LABEL = "TABLE_NAME";

    public static final String TABLE_INFO_SQL = "SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA='%s' AND TABLE_NAME='%s'";

    /**
     * 该值必须与#{@link DBConstant#TABLE_INFO_SQL 查询的field名称一致}
     */
    public static final String TABLE_COMMENT_COLUMN_LABEL = "TABLE_COMMENT";

    public static final String FIELD_INFO_SQL = "SHOW FULL COLUMNS FROM %s.%s";

    public static final String FIELD_NAME_COLUMN_LABEL = "Field";
    public static final String FIELD_TYPE_COLUMN_LABEL = "Type";
    public static final String FIELD_NULLABLE_COLUMN_LABEL = "Null";
    public static final String FIELD_DEFAULT_VALUE_COLUMN_LABEL = "Default";
    public static final String FIELD_COMMENT_COLUMN_LABEL = "Comment";

    public static final String FIELD_NULLABLE_YES_VALUE = "YES";
}

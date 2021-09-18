package com.luoyifan.mysql2office.service;

import com.luoyifan.mysql2office.entity.ExportConfig;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.sql.SQLException;
import java.util.List;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
public interface ExportService {
    List<String> listTableName(ExportConfig config) throws SQLException;

    XSSFWorkbook exportExcel(ExportConfig config, List<String> tableNames) throws SQLException;

    XWPFDocument exportWord(ExportConfig config, List<String> tableNames) throws SQLException;
}

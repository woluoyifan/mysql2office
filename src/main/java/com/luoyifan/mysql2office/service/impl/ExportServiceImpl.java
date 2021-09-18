package com.luoyifan.mysql2office.service.impl;

import com.luoyifan.mysql2office.constant.DefaultConfigConstant;
import com.luoyifan.mysql2office.constant.DBConstant;
import com.luoyifan.mysql2office.entity.ExportConfig;
import com.luoyifan.mysql2office.entity.FieldInfo;
import com.luoyifan.mysql2office.entity.TableInfo;
import com.luoyifan.mysql2office.service.ExportService;
import com.luoyifan.mysql2office.utils.WordHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Service
public class ExportServiceImpl implements ExportService {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
        }
    }

    private Connection getConn(ExportConfig config) throws SQLException {
        String connProps = config.getConnProps();
        if (StringUtils.isBlank(connProps)) {
            connProps = DefaultConfigConstant.CONN_PROPS;
        }
        String url = String.format(DBConstant.DB_URL, config.getHost(), config.getPort(), connProps);
        return DriverManager.getConnection(url, config.getUser(), config.getPassword());
    }

    /**
     * 获取数据库下的所有表名
     */
    @Override
    public List<String> listTableName(ExportConfig config) throws SQLException {
        String sql = String.format(DBConstant.TABLE_LIST_SQL, config.getDbName());
        List<String> tableNames = new ArrayList<>();
        try (Connection conn = getConn(config)) {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableNames.add(rs.getString(DBConstant.TABLE_NAME_COLUMN_LABEL));
                }
            }
        }
//            DatabaseMetaData db = conn.getMetaData();
//            try (ResultSet rs = db.getTables(dbName, null, null, new String[]{"TABLE"})) {
//                while (rs.next()) {
//                    tableNames.add(rs.getString(DBConstant.META_DATA_TABLE_NAME_COLUMN_INDEX));
//                }
//            }
        return tableNames;
    }

    @Override
    public XSSFWorkbook exportExcel(ExportConfig config, List<String> tableNames) throws SQLException {
        List<TableInfo> tableInfoList = listTableInfo(config, tableNames);
        return createExcel(config,tableInfoList);
    }

    @Override
    public XWPFDocument exportWord(ExportConfig config, List<String> tableNames) throws SQLException {
        List<TableInfo> tableInfoList = listTableInfo(config, tableNames);
        return createWord(config,tableInfoList);
    }

    private List<TableInfo> listTableInfo(ExportConfig config,List<String> tableNames) throws SQLException{
        try(Connection conn =  getConn(config)){
            List<TableInfo> tableInfoList = new ArrayList<>();
            for(String tableName : tableNames){
                TableInfo tableInfo = getTableInfo(config, conn, tableName);
                tableInfoList.add(tableInfo);
            }
            return tableInfoList;
        }
    }

    private TableInfo getTableInfo(ExportConfig config, Connection conn, String tableName) throws SQLException {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setName(tableName);

        String tableComment = getTableComment(config, conn, tableName);
        tableInfo.setComment(tableComment);

        List<FieldInfo> fieldInfoList = listTableFieldInfo(config, conn, tableName);
        tableInfo.setFieldInfoList(fieldInfoList);
        return tableInfo;
    }

    private String getTableComment(ExportConfig config, Connection conn, String tableName) throws SQLException {
        String sql = String.format(DBConstant.TABLE_INFO_SQL, config.getDbName(), tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                return rs.getString(DBConstant.TABLE_COMMENT_COLUMN_LABEL);
            }
        }
        return null;
    }

    private List<FieldInfo> listTableFieldInfo(ExportConfig config, Connection conn, String tableName) throws SQLException {
        String sql = String.format(DBConstant.FIELD_INFO_SQL, config.getDbName(), tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return result2FieldInfo(rs);
        }
    }

    private List<FieldInfo> result2FieldInfo(ResultSet rs) throws SQLException {
        List<FieldInfo> infoList = new ArrayList<>();
        while (rs.next()) {
            FieldInfo info = new FieldInfo();
            info.setIndex(rs.getRow());
            info.setName(rs.getString(DBConstant.FIELD_NAME_COLUMN_LABEL));
            info.setType(rs.getString(DBConstant.FIELD_TYPE_COLUMN_LABEL));
            info.setNullable(DBConstant.FIELD_NULLABLE_YES_VALUE.equals(rs.getString(DBConstant.FIELD_NULLABLE_COLUMN_LABEL)));
            info.setDefaultVal(rs.getString(DBConstant.FIELD_DEFAULT_VALUE_COLUMN_LABEL));
            info.setComment(rs.getString(DBConstant.FIELD_COMMENT_COLUMN_LABEL));
            infoList.add(info);
        }
        return infoList;
    }

    public XSSFWorkbook createExcel(ExportConfig config, List<TableInfo> tableInfos) {
        prepareConfig(config);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle titleStyle = getTitleStyle(workbook);
        XSSFCellStyle textStyle = getTextStyle(workbook);

        XSSFSheet sheet = workbook.createSheet();

        int rowNum = 0;

        for (TableInfo tableInfo : tableInfos) {
            XSSFCell cell;

            XSSFRow row = sheet.createRow(rowNum);
            for (int i = 0; i < 5; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(textStyle);
            }
            row.getCell(0).setCellValue(config.getTableNameTitle());
            row.getCell(2).setCellValue(config.getTableCommentTitle());
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 4));
            rowNum++;

            row = sheet.createRow(rowNum);
            for (int i = 0; i < 5; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(textStyle);
            }
            row.getCell(0).setCellValue(tableInfo.getName());
            row.getCell(2).setCellValue(tableInfo.getComment());
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 4));
            rowNum++;

            row = sheet.createRow(rowNum++);
            List<String> fieldTitleList = Arrays.asList(config.getFieldNameTitle(),
                    config.getFieldTypeTitle(),
                    config.getFieldNullableTitle(),
                    config.getFieldDefaultValueTitle(),
                    config.getFieldCommentTitle());
            for (int i = 0; i < fieldTitleList.size(); i++) {
                cell = row.createCell(i);
                cell.setCellStyle(titleStyle);
                cell.setCellValue(fieldTitleList.get(i));
            }

            for (FieldInfo fieldInfo : tableInfo.getFieldInfoList()) {
                row = sheet.createRow(rowNum++);
                for (int i = 0; i < 5; i++) {
                    cell = row.createCell(i);
                    cell.setCellStyle(textStyle);
                    cell.setCellType(CellType.STRING);
                }

                row.getCell(0).setCellValue(fieldInfo.getName());
                row.getCell(1).setCellValue(fieldInfo.getType());
                row.getCell(2).setCellValue(fieldInfo.getNullable() ? "Y" : "N");
                row.getCell(3).setCellValue(fieldInfo.getDefaultVal());
                row.getCell(4).setCellValue(fieldInfo.getComment());
            }
            rowNum += 2;
        }
        return workbook;
    }

    public XWPFDocument createWord(ExportConfig config, List<TableInfo> tableInfos) {
        prepareConfig(config);

        XWPFDocument doc = new XWPFDocument();

        for (TableInfo tableInfo : tableInfos) {
            List<FieldInfo> fieldInfoList = tableInfo.getFieldInfoList();
            XWPFTable table = doc.createTable(3 + fieldInfoList.size(), 5);
            table.setWidth("100%");

            int rowNum = 0;
            XWPFTableRow row = table.getRow(rowNum);
            XWPFTableCell cell = row.getCell(0);
            cell.setText(config.getTableNameTitle());
            cell.setColor(DefaultConfigConstant.TITLE_COLOR);
//            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

            WordHelper.mergeCellsHorizontal(table, rowNum, 0, 1);

            cell = row.getCell(2);
            cell.setText(config.getTableCommentTitle());
            cell.setColor(DefaultConfigConstant.TITLE_COLOR);
            WordHelper.mergeCellsHorizontal(table, rowNum, 2, 4);
            rowNum++;

            row = table.getRow(rowNum);
            cell = row.getCell(0);
            cell.setText(tableInfo.getName());
            WordHelper.mergeCellsHorizontal(table, rowNum, 0, 1);

            cell = row.getCell(2);
            cell.setText(tableInfo.getComment());
            WordHelper.mergeCellsHorizontal(table, rowNum, 2, 4);
            rowNum++;

            row = table.getRow(rowNum);
            List<String> fieldTitleList = Arrays.asList(config.getFieldNameTitle(),
                    config.getFieldTypeTitle(),
                    config.getFieldNullableTitle(),
                    config.getFieldDefaultValueTitle(),
                    config.getFieldCommentTitle());
            for (int i = 0; i < fieldTitleList.size(); i++) {
                cell = row.getCell(i);
                cell.setColor(DefaultConfigConstant.TITLE_COLOR);
                cell.setText(fieldTitleList.get(i));
            }
            rowNum++;

            for (FieldInfo col : fieldInfoList) {
                row = table.getRow(rowNum);
                row.getCell(0).setText(col.getName());
                row.getCell(1).setText(col.getType());
                row.getCell(2).setText(col.getNullable() ? "Y" : "N");
                row.getCell(3).setText(col.getDefaultVal());
                row.getCell(4).setText(col.getComment());
                rowNum++;
            }

            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addCarriageReturn();
        }

        return doc;
    }

    private void prepareConfig(ExportConfig config) {
        if (StringUtils.isBlank(config.getTableNameTitle())) {
            config.setTableNameTitle(DefaultConfigConstant.TABLE_NAME_TITLE);
        }
        if (StringUtils.isBlank(config.getTableCommentTitle())) {
            config.setTableCommentTitle(DefaultConfigConstant.TABLE_COMMENT_TITLE);
        }
        if (StringUtils.isBlank(config.getFieldNameTitle())) {
            config.setFieldNameTitle(DefaultConfigConstant.FIELD_NAME_TITLE);
        }
        if (StringUtils.isBlank(config.getFieldTypeTitle())) {
            config.setFieldTypeTitle(DefaultConfigConstant.FIELD_TYPE_TITLE);
        }
        if (StringUtils.isBlank(config.getFieldNullableTitle())) {
            config.setFieldNullableTitle(DefaultConfigConstant.FIELD_NULLABLE_TITLE);
        }
        if (StringUtils.isBlank(config.getFieldDefaultValueTitle())) {
            config.setFieldDefaultValueTitle(DefaultConfigConstant.FIELD_DEFAULT_VALUE_TITLE);
        }
        if (StringUtils.isBlank(config.getFieldCommentTitle())) {
            config.setFieldCommentTitle(DefaultConfigConstant.FIELD_COMMENT_TITLE);
        }
    }

    private void setFullBorder(XSSFCellStyle cellStyle, BorderStyle borderStyle) {
        cellStyle.setBorderTop(borderStyle);
        cellStyle.setBorderBottom(borderStyle);
        cellStyle.setBorderLeft(borderStyle);
        cellStyle.setBorderRight(borderStyle);
    }

    private XSSFCellStyle getTitleStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        setFullBorder(style, BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle getTextStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        setFullBorder(style, BorderStyle.THIN);
        return style;
    }
}

package com.luoyifan.mysql2office.controller;

import com.luoyifan.mysql2office.entity.ExportConfig;
import com.luoyifan.mysql2office.entity.ResponseEntity;
import com.luoyifan.mysql2office.service.ExportService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author EvanLuo
 * @since 2021/9/10
 */
@Controller
@RequestMapping
public class ExportController {
    @Autowired
    private ExportService exportService;

    @RequestMapping("/listTableName")
    @ResponseBody
    public ResponseEntity<List<String>> listTableName(ExportConfig config){
        ResponseEntity<List<String>> entity = new ResponseEntity<>();
        try {
            List<String> tableNameList = exportService.listTableName(config);
            entity.setData(tableNameList);
            entity.setSuccess(true);
        }catch (Exception e){
            entity.setMsg(e.getMessage());
            entity.setSuccess(false);
        }
        return entity;
    }

    @RequestMapping("/export")
    public void exportExcel(ExportConfig config, String[] tableNames, HttpServletResponse response){
        String exportType = config.getExportType();
        String fileName = config.getDbName();
        if("excel".equalsIgnoreCase(exportType)) {
            response.setContentType("application/vnd.ms-excel");
            fileName += ".xlsx";
        }else if("word".equalsIgnoreCase(exportType)){
            response.setContentType("application/vnd.ms-word");
            fileName += ".docx";
        }
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try (OutputStream os = response.getOutputStream()){
            if("excel".equalsIgnoreCase(exportType)) {
                XSSFWorkbook workbook = exportService.exportExcel(config, Arrays.asList(tableNames));
                workbook.write(os);
            }else if("word".equalsIgnoreCase(exportType)){
                XWPFDocument word = exportService.exportWord(config, Arrays.asList(tableNames));
                word.write(os);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

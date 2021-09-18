package com.luoyifan.mysql2office.utils;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.springframework.util.StringUtils;

import java.math.BigInteger;

/**
 * 代码摘抄自 https://blog.csdn.net/qq_37660154/article/details/77148198
 * 原作者 K_GeDream
 * @author EvanLuo
 * @since 2021/9/10
 */
public class WordHelper {
    /***
     *  跨行合并
     * @param table
     * @param col  合并列
     * @param fromRow 起始行
     * @param toRow   终止行
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if ( rowIndex == fromRow ) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /***
     * 跨列合并
     * @param table
     * @param row 所合并的行
     * @param fromCell  起始列
     * @param toCell   终止列
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if ( cellIndex == fromCell ) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    public static void setCellWidthAndVAlign(XWPFTableCell cell, String width, STVerticalJc.Enum typeEnum, STJc.Enum vAlign) {
        CTTc cttc = cell.getCTTc();
        CTTcPr cellPr = cttc.isSetTcPr() ? cttc.getTcPr() : cttc.addNewTcPr();
        if(typeEnum != null) {
            cellPr.addNewVAlign().setVal(typeEnum);
        }
        if(vAlign != null) {
            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(vAlign);
        }
        CTTblWidth tblWidth = cellPr.isSetTcW() ? cellPr.getTcW() : cellPr.addNewTcW();
        if(!StringUtils.isEmpty(width)){
            tblWidth.setW(new BigInteger(width));
            tblWidth.setType(STTblWidth.DXA);
        }
    }
}

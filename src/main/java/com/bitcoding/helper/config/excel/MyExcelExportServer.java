package com.bitcoding.helper.config.excel;

import cn.afterturn.easypoi.excel.annotation.ExcelEntity;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.export.styler.IExcelExportStyler;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelExportEnum;
import cn.afterturn.easypoi.util.PoiExcelGraphDataUtil;
import cn.afterturn.easypoi.util.PoiPublicUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;

public class MyExcelExportServer extends MyBaseExportService {
    private static int MAX_NUM = 60000;

    public MyExcelExportServer() {
    }

    protected int createHeaderAndTitle(ExportParams entity, Sheet sheet, Workbook workbook, List<ExcelExportEntity> excelParams) {
        int rows = 0;
        int fieldLength = this.getFieldLength(excelParams);
        if (entity.getTitle() != null) {
            rows += this.createTitle2Row(entity, sheet, workbook, fieldLength);
        }

        rows += this.createHeaderRow(entity, sheet, workbook, rows, excelParams);
        sheet.createFreezePane(0, rows, 0, rows);
        return rows;
    }

    public int createTitle2Row(ExportParams entity, Sheet sheet, Workbook workbook, int fieldWidth) {
        Row row = sheet.createRow(0);
        row.setHeight(entity.getTitleHeight());
        this.createStringCell(row, 0, entity.getTitle(), this.getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), (ExcelExportEntity)null);

        for(int i = 1; i <= fieldWidth; ++i) {
            this.createStringCell(row, i, "", this.getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), (ExcelExportEntity)null);
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, fieldWidth));
        if (entity.getSecondTitle() == null) {
            return 1;
        } else {
            row = sheet.createRow(1);
            row.setHeight(entity.getSecondTitleHeight());
            CellStyle style = workbook.createCellStyle();
            style.setAlignment((short)3);
            this.createStringCell(row, 0, entity.getSecondTitle(), style, (ExcelExportEntity)null);

            for(int i = 1; i <= fieldWidth; ++i) {
                this.createStringCell(row, i, "", this.getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), (ExcelExportEntity)null);
            }

            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, fieldWidth));
            return 2;
        }
    }

    public void createSheet(Workbook workbook, ExportParams entity, Class<?> pojoClass, Collection<?> dataSet) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Excel export start ,class is {}", pojoClass);
            LOGGER.debug("Excel version is {}", entity.getType().equals(ExcelType.HSSF) ? "03" : "07");
        }

        if (workbook != null && entity != null && pojoClass != null && dataSet != null) {
            try {
                List<ExcelExportEntity> excelParams = new ArrayList();
                Field[] fileds = PoiPublicUtil.getClassFields(pojoClass);
                ExcelTarget etarget = (ExcelTarget)pojoClass.getAnnotation(ExcelTarget.class);
                String targetId = etarget == null ? null : etarget.value();
                this.getAllExcelField(entity.getExclusions(), targetId, fileds, excelParams, pojoClass, (List)null, (ExcelEntity)null);
                this.createSheetForMap(workbook, entity, excelParams, dataSet);
            } catch (Exception var9) {
                LOGGER.error(var9.getMessage(), var9);
                throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, var9.getCause());
            }
        } else {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
    }

    public void createSheetForMap(Workbook workbook, ExportParams entity, List<ExcelExportEntity> entityList, Collection<?> dataSet) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Excel version is {}", entity.getType().equals(ExcelType.HSSF) ? "03" : "07");
        }

        if (workbook != null && entity != null && entityList != null && dataSet != null) {
            super.type = entity.getType();
            if (this.type.equals(ExcelType.XSSF)) {
                MAX_NUM = 1000000;
            }

            if (entity.getMaxNum() > 0) {
                MAX_NUM = entity.getMaxNum();
            }

            Sheet sheet = null;

            try {
                sheet = workbook.createSheet(entity.getSheetName());
            } catch (Exception var7) {
                sheet = workbook.createSheet();
            }

            this.insertDataToSheet(workbook, entity, entityList, dataSet, sheet);
        } else {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
    }

    protected void insertDataToSheet(Workbook workbook, ExportParams entity, List<ExcelExportEntity> entityList, Collection<?> dataSet, Sheet sheet) {
        try {
            this.dataHandler = entity.getDataHandler();
            if (this.dataHandler != null && this.dataHandler.getNeedHandlerFields() != null) {
                this.needHandlerList = Arrays.asList(this.dataHandler.getNeedHandlerFields());
            }

            this.dictHandler = entity.getDictHandler();
            this.setExcelExportStyler((IExcelExportStyler)entity.getStyle().getConstructor(Workbook.class).newInstance(workbook));
            Drawing patriarch = PoiExcelGraphDataUtil.getDrawingPatriarch(sheet);
            List<ExcelExportEntity> excelParams = new ArrayList();
            if (entity.isAddIndex()) {
                excelParams.add(this.indexExcelEntity(entity));
            }

            excelParams.addAll(entityList);
            this.sortAllParams(excelParams);
            int index = entity.isCreateHeadRows() ? this.createHeaderAndTitle(entity, sheet, workbook, excelParams) : 0;
            this.setCellWith(excelParams, sheet);
            this.setColumnHidden(excelParams, sheet);
            short rowHeight = entity.getHeight() != 0 ? entity.getHeight() : this.getRowHeight(excelParams);
            this.setCurrentIndex(1);
            Iterator<?> its = dataSet.iterator();
            ArrayList tempList = new ArrayList();

            while(its.hasNext()) {
                Object t = its.next();
                index += this.createCells(patriarch, index, t, excelParams, sheet, workbook, rowHeight);
                tempList.add(t);
                if (index >= MAX_NUM) {
                    break;
                }
            }

            if (entity.getFreezeCol() != 0) {
                sheet.createFreezePane(entity.getFreezeCol(), 0, entity.getFreezeCol(), 0);
            }

            this.mergeCells(sheet, excelParams, index);
            its = dataSet.iterator();
            int i = 0;

            for(int le = tempList.size(); i < le; ++i) {
                its.next();
                its.remove();
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("List data more than max ,data size is {}", dataSet.size());
            }

            if (dataSet.size() > 0) {
                this.createSheetForMap(workbook, entity, entityList, dataSet);
            } else {
                this.addStatisticsRow(this.getExcelExportStyler().getStyles(true, (ExcelExportEntity)null), sheet);
            }

        } catch (Exception var15) {
            LOGGER.error(var15.getMessage(), var15);
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, var15.getCause());
        }
    }

    private int createHeaderRow(ExportParams title, Sheet sheet, Workbook workbook, int index, List<ExcelExportEntity> excelParams) {
        Row row = sheet.createRow(index);
        int rows = this.getRowNums(excelParams);
        row.setHeight(title.getHeaderHeight());
        Row listRow = null;
        if (rows == 2) {
            listRow = sheet.createRow(index + 1);
            listRow.setHeight(title.getHeaderHeight());
        }

        int cellIndex = 0;
        int groupCellLength = 0;
        CellStyle titleStyle = this.getExcelExportStyler().getTitleStyle(title.getColor());
        int i = 0;

        for(int exportFieldTitleSize = excelParams.size(); i < exportFieldTitleSize; ++i) {
            ExcelExportEntity entity = (ExcelExportEntity)excelParams.get(i);
            if (StringUtils.isBlank(entity.getGroupName()) || !entity.getGroupName().equals(((ExcelExportEntity)excelParams.get(i - 1)).getGroupName())) {
                if (groupCellLength > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(index, index, cellIndex - groupCellLength, cellIndex - 1));
                }

                groupCellLength = 0;
            }

            if (StringUtils.isNotBlank(entity.getGroupName())) {
                this.createStringCell(row, cellIndex, entity.getGroupName(), titleStyle, entity);
                this.createStringCell(listRow, cellIndex, entity.getName(), titleStyle, entity);
                ++groupCellLength;
            } else if (StringUtils.isNotBlank(entity.getName())) {
                this.createStringCell(row, cellIndex, entity.getName(), titleStyle, entity);
            }

            if (entity.getList() == null) {
                if (rows == 2 && StringUtils.isBlank(entity.getGroupName())) {
                    this.createStringCell(listRow, cellIndex, "", titleStyle, entity);
                    sheet.addMergedRegion(new CellRangeAddress(index, index + 1, cellIndex, cellIndex));
                }
            } else {
                List<ExcelExportEntity> sTitel = entity.getList();
                if (StringUtils.isNotBlank(entity.getName()) && sTitel.size() > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(index, index, cellIndex, cellIndex + sTitel.size() - 1));
                }

                int j = 0;

                for(int size = sTitel.size(); j < size; ++j) {
                    this.createStringCell(rows == 2 ? listRow : row, cellIndex, ((ExcelExportEntity)sTitel.get(j)).getName(), titleStyle, entity);
                    ++cellIndex;
                }

                --cellIndex;
            }

            ++cellIndex;
        }

        if (groupCellLength > 1) {
            sheet.addMergedRegion(new CellRangeAddress(index, index, cellIndex - groupCellLength, cellIndex - 1));
        }

        return rows;
    }
}

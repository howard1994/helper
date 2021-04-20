package com.bitcoding.helper.config.excel;

import cn.afterturn.easypoi.cache.ImageCache;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.vo.BaseEntityTypeConstants;
import cn.afterturn.easypoi.excel.export.base.ExportCommonService;
import cn.afterturn.easypoi.excel.export.styler.IExcelExportStyler;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelExportEnum;
import cn.afterturn.easypoi.util.PoiExcelGraphDataUtil;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import cn.afterturn.easypoi.util.PoiPublicUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author LongQi-Howard
 */
public abstract class MyBaseExportService extends ExportCommonService {
    private int currentIndex = 0;
    protected ExcelType type;
    private Map<Integer, Double> statistics;
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");
    protected IExcelExportStyler excelExportStyler;

    public MyBaseExportService() {
        this.type = ExcelType.HSSF;
        this.statistics = new HashMap();
    }

    public int createCells(Drawing patriarch, int index, Object t, List<ExcelExportEntity> excelParams, Sheet sheet, Workbook workbook, short rowHeight) {
        try {
            Row row = sheet.createRow(index);
            if (rowHeight != -1) {
                row.setHeight(rowHeight);
            }

            int maxHeight = 1;
            int cellNum = 0;
            int indexKey = this.createIndexCell(row, index, (ExcelExportEntity)excelParams.get(0));
            cellNum = cellNum + indexKey;
            int k = indexKey;

            ExcelExportEntity entity;
            int paramSize;
            for(paramSize = excelParams.size(); k < paramSize; ++k) {
                entity = (ExcelExportEntity)excelParams.get(k);
                if (entity.getList() == null) {
                    Object value = this.getCellValue(entity, t);
                    if (entity.getType() == BaseEntityTypeConstants.STRING_TYPE) {
                        this.createStringCell(row, cellNum++, value == null ? "" : value.toString(), index % 2 == 0 ? this.getStyles(false, entity) : this.getStyles(true, entity), entity);
                        if (entity.isHyperlink()) {
                            row.getCell(cellNum - 1).setHyperlink(this.dataHandler.getHyperlink(row.getSheet().getWorkbook().getCreationHelper(), t, entity.getName(), value));
                        }
                    } else if (entity.getType() == BaseEntityTypeConstants.DOUBLE_TYPE) {
                        this.createDoubleCell(row, cellNum++, value == null ? "" : value.toString(), index % 2 == 0 ? this.getStyles(false, entity) : this.getStyles(true, entity), entity);
                        if (entity.isHyperlink()) {
                            row.getCell(cellNum - 1).setHyperlink(this.dataHandler.getHyperlink(row.getSheet().getWorkbook().getCreationHelper(), t, entity.getName(), value));
                        }
                    } else {
                        this.createImageCell(patriarch, entity, row, cellNum++, value == null ? "" : value.toString(), t);
                    }
                } else {
                    Collection<?> list = this.getListCellValue(entity, t);
                    int listC = 0;
                    if (list != null && list.size() > 0) {
                        for(Iterator var17 = list.iterator(); var17.hasNext(); ++listC) {
                            Object obj = var17.next();
                            this.createListCells(patriarch, index + listC, cellNum, obj, entity.getList(), sheet, workbook, rowHeight);
                        }
                    }

                    cellNum += entity.getList().size();
                    if (list != null && list.size() > maxHeight) {
                        maxHeight = list.size();
                    }
                }
            }

            cellNum = 0;
            k = indexKey;

            for(paramSize = excelParams.size(); k < paramSize; ++k) {
                entity = (ExcelExportEntity)excelParams.get(k);
                if (entity.getList() != null) {
                    cellNum += entity.getList().size();
                } else if (entity.isNeedMerge() && maxHeight > 1) {
                    for(int i = index + 1; i < index + maxHeight; ++i) {
                        sheet.getRow(i).createCell(cellNum);
                        sheet.getRow(i).getCell(cellNum).setCellStyle(this.getStyles(false, entity));
                    }

                    sheet.addMergedRegion(new CellRangeAddress(index, index + maxHeight - 1, cellNum, cellNum));
                    ++cellNum;
                }
            }

            return maxHeight;
        } catch (Exception var19) {
            LOGGER.error("excel cell export error ,data is :{}", ReflectionToStringBuilder.toString(t));
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, var19);
        }
    }

    public void createImageCell(Drawing patriarch, ExcelExportEntity entity, Row row, int i, String imagePath, Object obj) throws Exception {
        Cell cell = row.createCell(i);
        byte[] value = null;
        if (entity.getExportImageType() != 1) {
            value = (byte[])((byte[])(entity.getMethods() != null ? this.getFieldBySomeMethod(entity.getMethods(), obj) : entity.getMethod().invoke(obj)));
        }

        this.createImageCell(cell, 50.0D * entity.getHeight(), entity.getExportImageType() == 1 ? imagePath : null, value);
    }

    public void createImageCell(Cell cell, double height, String imagePath, byte[] data) throws Exception {
        if (height > (double)cell.getRow().getHeight()) {
            cell.getRow().setHeight((short)((int)height));
        }

        Object anchor;
        if (this.type.equals(ExcelType.HSSF)) {
            anchor = new HSSFClientAnchor(0, 0, 0, 0, (short)cell.getColumnIndex(), cell.getRow().getRowNum(), (short)(cell.getColumnIndex() + 1), cell.getRow().getRowNum() + 1);
        } else {
            anchor = new XSSFClientAnchor(0, 0, 0, 0, (short)cell.getColumnIndex(), cell.getRow().getRowNum(), (short)(cell.getColumnIndex() + 1), cell.getRow().getRowNum() + 1);
        }

        if (StringUtils.isNotEmpty(imagePath)) {
            data = ImageCache.getImage(imagePath);
        }

        if (data != null) {
            PoiExcelGraphDataUtil.getDrawingPatriarch(cell.getSheet()).createPicture((ClientAnchor)anchor, cell.getSheet().getWorkbook().addPicture(data, this.getImageType(data)));
        }

    }

    private int createIndexCell(Row row, int index, ExcelExportEntity excelExportEntity) {
        if (excelExportEntity.getName() != null && "序号".equals(excelExportEntity.getName()) && excelExportEntity.getFormat() != null && excelExportEntity.getFormat().equals("isAddIndex")) {
            this.createStringCell(row, 0, this.currentIndex + "", index % 2 == 0 ? this.getStyles(false, (ExcelExportEntity)null) : this.getStyles(true, (ExcelExportEntity)null), (ExcelExportEntity)null);
            ++this.currentIndex;
            return 1;
        } else {
            return 0;
        }
    }

    public void createListCells(Drawing patriarch, int index, int cellNum, Object obj, List<ExcelExportEntity> excelParams, Sheet sheet, Workbook workbook, short rowHeight) throws Exception {
        Row row;
        if (sheet.getRow(index) == null) {
            row = sheet.createRow(index);
            if (rowHeight != -1) {
                row.setHeight(rowHeight);
            }
        } else {
            row = sheet.getRow(index);
            if (rowHeight != -1) {
                row.setHeight(rowHeight);
            }
        }

        int k = 0;

        for(int paramSize = excelParams.size(); k < paramSize; ++k) {
            ExcelExportEntity entity = (ExcelExportEntity)excelParams.get(k);
            Object value = this.getCellValue(entity, obj);
            if (entity.getType() == BaseEntityTypeConstants.STRING_TYPE) {
                this.createStringCell(row, cellNum++, value == null ? "" : value.toString(), row.getRowNum() % 2 == 0 ? this.getStyles(false, entity) : this.getStyles(true, entity), entity);
                if (entity.isHyperlink()) {
                    row.getCell(cellNum - 1).setHyperlink(this.dataHandler.getHyperlink(row.getSheet().getWorkbook().getCreationHelper(), obj, entity.getName(), value));
                }
            } else if (entity.getType() == BaseEntityTypeConstants.DOUBLE_TYPE) {
                this.createDoubleCell(row, cellNum++, value == null ? "" : value.toString(), index % 2 == 0 ? this.getStyles(false, entity) : this.getStyles(true, entity), entity);
                if (entity.isHyperlink()) {
                    row.getCell(cellNum - 1).setHyperlink(this.dataHandler.getHyperlink(row.getSheet().getWorkbook().getCreationHelper(), obj, entity.getName(), value));
                }
            } else {
                this.createImageCell(patriarch, entity, row, cellNum++, value == null ? "" : value.toString(), obj);
            }
        }

    }

    public void createStringCell(Row row, int index, String text, CellStyle style, ExcelExportEntity entity) {
        Cell cell = row.createCell(index);
        if (style != null && style.getDataFormat() > 0 && style.getDataFormat() < 12) {
            cell.setCellValue(Double.parseDouble(text));
            cell.setCellType(0);
        } else {
            Object rtext;
            if (this.type.equals(ExcelType.HSSF)) {
                rtext = new HSSFRichTextString(text);
            } else {
                rtext = new XSSFRichTextString(text);
            }

            cell.setCellValue((RichTextString)rtext);
        }

        if (style != null) {
            cell.setCellStyle(style);
        }

        this.addStatisticsData(index, text, entity);
    }

    public void createDoubleCell(Row row, int index, String text, CellStyle style, ExcelExportEntity entity) {
        Cell cell = row.createCell(index);
        cell.setCellType(0);
        if (text != null && text.length() > 0) {
            cell.setCellValue(Double.parseDouble(text));
        }
        else {
            cell.setCellValue("");
        }
        if (style != null) {
            cell.setCellStyle(style);
        }

        this.addStatisticsData(index, text, entity);
    }

    public void addStatisticsRow(CellStyle styles, Sheet sheet) {
        if (this.statistics.size() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("add statistics data ,size is {}", this.statistics.size());
            }

            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = this.statistics.keySet();
            this.createStringCell(row, 0, "合计", styles, (ExcelExportEntity)null);
            Iterator var5 = keys.iterator();

            while(var5.hasNext()) {
                Integer key = (Integer)var5.next();
                this.createStringCell(row, key, DOUBLE_FORMAT.format(this.statistics.get(key)), styles, (ExcelExportEntity)null);
            }

            this.statistics.clear();
        }

    }

    private void addStatisticsData(Integer index, String text, ExcelExportEntity entity) {
        if (entity != null && entity.isStatistics()) {
            Double temp = 0.0D;
            if (!this.statistics.containsKey(index)) {
                this.statistics.put(index, temp);
            }

            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException var6) {
            }

            this.statistics.put(index, (Double)this.statistics.get(index) + temp);
        }

    }

    public int getImageType(byte[] value) {
        String type = PoiPublicUtil.getFileExtendName(value);
        if ("JPG".equalsIgnoreCase(type)) {
            return 5;
        } else {
            return "PNG".equalsIgnoreCase(type) ? 6 : 5;
        }
    }

    private Map<Integer, int[]> getMergeDataMap(List<ExcelExportEntity> excelParams) {
        Map<Integer, int[]> mergeMap = new HashMap();
        int i = 0;
        Iterator var4 = excelParams.iterator();

        while(true) {
            while(var4.hasNext()) {
                ExcelExportEntity entity = (ExcelExportEntity)var4.next();
                if (entity.isMergeVertical()) {
                    mergeMap.put(i, entity.getMergeRely());
                }

                if (entity.getList() != null) {
                    for(Iterator var6 = entity.getList().iterator(); var6.hasNext(); ++i) {
                        ExcelExportEntity inner = (ExcelExportEntity)var6.next();
                        if (inner.isMergeVertical()) {
                            mergeMap.put(i, inner.getMergeRely());
                        }
                    }
                } else {
                    ++i;
                }
            }

            return mergeMap;
        }
    }

    public CellStyle getStyles(boolean needOne, ExcelExportEntity entity) {
        return this.excelExportStyler.getStyles(needOne, entity);
    }

    public void mergeCells(Sheet sheet, List<ExcelExportEntity> excelParams, int titleHeight) {
        Map<Integer, int[]> mergeMap = this.getMergeDataMap(excelParams);
        PoiMergeCellUtil.mergeCells(sheet, mergeMap, titleHeight);
    }

    public void setCellWith(List<ExcelExportEntity> excelParams, Sheet sheet) {
        int index = 0;

        for(int i = 0; i < excelParams.size(); ++i) {
            if (((ExcelExportEntity)excelParams.get(i)).getList() != null) {
                List<ExcelExportEntity> list = ((ExcelExportEntity)excelParams.get(i)).getList();

                for(int j = 0; j < list.size(); ++j) {
                    sheet.setColumnWidth(index, (int)(256.0D * ((ExcelExportEntity)list.get(j)).getWidth()));
                    ++index;
                }
            } else {
                sheet.setColumnWidth(index, (int)(256.0D * ((ExcelExportEntity)excelParams.get(i)).getWidth()));
                ++index;
            }
        }

    }

    public void setColumnHidden(List<ExcelExportEntity> excelParams, Sheet sheet) {
        int index = 0;

        for(int i = 0; i < excelParams.size(); ++i) {
            if (((ExcelExportEntity)excelParams.get(i)).getList() != null) {
                List<ExcelExportEntity> list = ((ExcelExportEntity)excelParams.get(i)).getList();

                for(int j = 0; j < list.size(); ++j) {
                    sheet.setColumnHidden(index, ((ExcelExportEntity)list.get(j)).isColumnHidden());
                    ++index;
                }
            } else {
                sheet.setColumnHidden(index, ((ExcelExportEntity)excelParams.get(i)).isColumnHidden());
                ++index;
            }
        }

    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void setExcelExportStyler(IExcelExportStyler excelExportStyler) {
        this.excelExportStyler = excelExportStyler;
    }

    public IExcelExportStyler getExcelExportStyler() {
        return this.excelExportStyler;
    }
}

package com.bitcoding.helper.config.excel;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.export.ExcelBatchExportService;
import cn.afterturn.easypoi.excel.export.template.ExcelExportOfTemplateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author LongQi-Howard
 */
public class MyExcelExportUtil {
    public MyExcelExportUtil() {
    }

    public static Workbook exportBigExcel(ExportParams entity, Class<?> pojoClass, Collection<?> dataSet) {
        ExcelBatchExportService batchService = ExcelBatchExportService.getExcelBatchExportService(entity, pojoClass);
        return batchService.appendData(dataSet);
    }

    public static Workbook exportBigExcel(ExportParams entity, List<ExcelExportEntity> excelParams, Collection<?> dataSet) {
        ExcelBatchExportService batchService = ExcelBatchExportService.getExcelBatchExportService(entity, excelParams);
        return batchService.appendData(dataSet);
    }

    public static void closeExportBigExcel() {
        ExcelBatchExportService batchService = ExcelBatchExportService.getCurrentExcelBatchExportService();
        if (batchService != null) {
            batchService.closeExportBigExcel();
        }

    }

    public static Workbook exportExcel(ExportParams entity, Class<?> pojoClass, Collection<?> dataSet) {
        Workbook workbook = getWorkbook(entity.getType(), dataSet.size());
        (new MyExcelExportServer()).createSheet(workbook, entity, pojoClass, dataSet);
        return workbook;
    }

    private static Workbook getWorkbook(ExcelType type, int size) {
        if (ExcelType.HSSF.equals(type)) {
            return new HSSFWorkbook();
        } else {
            return (Workbook)(size < 100000 ? new XSSFWorkbook() : new SXSSFWorkbook());
        }
    }

    public static Workbook exportExcel(ExportParams entity, List<ExcelExportEntity> entityList, Collection<?> dataSet) {
        Workbook workbook = getWorkbook(entity.getType(), dataSet.size());
        (new MyExcelExportServer()).createSheetForMap(workbook, entity, entityList, dataSet);
        return workbook;
    }

    public static Workbook exportExcel(List<Map<String, Object>> list, ExcelType type) {
        Workbook workbook = getWorkbook(type, 0);
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            Map<String, Object> map = (Map)var3.next();
            MyExcelExportServer service = new MyExcelExportServer();
            service.createSheet(workbook, (ExportParams)map.get("title"), (Class)map.get("entity"), (Collection)map.get("data"));
        }

        return workbook;
    }

    /** @deprecated */
    @Deprecated
    public static Workbook exportExcel(TemplateExportParams params, Class<?> pojoClass, Collection<?> dataSet, Map<String, Object> map) {
        return (new ExcelExportOfTemplateUtil()).createExcleByTemplate(params, pojoClass, dataSet, map);
    }

    public static Workbook exportExcel(TemplateExportParams params, Map<String, Object> map) {
        return (new ExcelExportOfTemplateUtil()).createExcleByTemplate(params, (Class)null, (Collection)null, map);
    }

    public static Workbook exportExcel(Map<Integer, Map<String, Object>> map, TemplateExportParams params) {
        return (new ExcelExportOfTemplateUtil()).createExcleByTemplate(params, map);
    }

}

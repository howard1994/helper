package com.bitcoding.helper;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelCollection;
import cn.afterturn.easypoi.excel.annotation.ExcelEntity;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.bitcoding.helper.config.excel.MyExcelExportUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author LongQi-Howard
 */
@Slf4j
public class ExcelUtils {

    public static void exportExcel(List<?> list, String title, Class<?> pojoClass,
                                   String fileName, boolean isCreateHeader, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(title, "sheet1");
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, pojoClass, fileName, response, exportParams);
    }

    public static void exportExcel(List<?> list, String title, Class<?> pojoClass, String fileName,
                                   HttpServletResponse response) {
        defaultExport(list, pojoClass, fileName, response, new ExportParams(title, "sheet1", ExcelType.XSSF));
    }

    public static void exportExcel(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        defaultExport(list, fileName, response);
    }

    private static void defaultExport(List<?> list, Class<?> pojoClass, String fileName,
                                      HttpServletResponse response, ExportParams exportParams) {
        Workbook workbook = MyExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        if (workbook != null) {
            downLoadExcel(fileName, response, workbook);
        }
    }

    private static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {
        try (OutputStream out = response.getOutputStream()) {
            fileName = CommonUtils.setFileName(fileName);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            response.setHeader("Content-Length", String.valueOf(baos.size()));
            out.write(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void defaultExport(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        Workbook workbook = MyExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        if (workbook != null) {
            downLoadExcel(fileName, response, workbook);
        }
    }

    public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(new File(filePath), pojoClass, params);
        } catch (NoSuchElementException e) {
            throw new RuntimeException("??????????????????");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    public static <T> List<T> importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (file == null) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(file.getInputStream(), pojoClass, params);
            //??????
            //list = list.stream().distinct().collect(Collectors.toList());
        } catch (NoSuchElementException e) {
            throw new RuntimeException("excel??????????????????");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }

    public static Map<String, String> dynamicChangeAndSaveSourceAnnotation(Map<String, String> headers, Class clazz, Map<String, String> excelMap) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // @Excel??????
            if (field.isAnnotationPresent(Excel.class)) {
                // ???????????????????????????????????????????????????????????????
                if (!headers.containsKey(field.getName())) {
                    Excel annotation = field.getAnnotation(Excel.class);
                    // ????????????
                    excelMap.put(field.getName(), annotation.name());
                    InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                    changeAnnotationValue(handler, "");
                }
                // @ExcelCollection??????
            } else if (field.isAnnotationPresent(ExcelCollection.class) && field.getType().isAssignableFrom(List.class)) {
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) type;
                    Class collectionClazz = (Class) pt.getActualTypeArguments()[0];
                    // ??????@ExcelCollection???????????????????????????????????????java.lang.IllegalArgumentException: The 'to' col (15) must not be less than the 'from' col (16)
                    // ?????????????????????????????????@ExcelCollection??????
                    Field[] collectionFields = collectionClazz.getDeclaredFields();
                    boolean flag = false;
                    for (Field temp : collectionFields) {
                        if (!temp.isAnnotationPresent(Excel.class)) {
                            continue;
                        }
                        if (headers.containsKey(temp.getName())) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        dynamicChangeAndSaveSourceAnnotation(headers, collectionClazz, excelMap);
                    } else {
                        ExcelCollection annotation = field.getAnnotation(ExcelCollection.class);
                        excelMap.put(field.getName(), annotation.name());
                        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                        changeAnnotationValue(handler, "");
                    }
                }
                // @ExcelEntity??????
            } else if (field.isAnnotationPresent(ExcelEntity.class)) {
                Class entityClazz = field.getType();
                dynamicChangeAndSaveSourceAnnotation(headers, entityClazz, excelMap);
            }
        }
        return excelMap;
    }
    // ?????????????????????????????????????????????

    private static void changeAnnotationValue(InvocationHandler handler, String name) {
        try {
            Field field = handler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            Map<String, Object> memberValues = (Map<String, Object>) field.get(handler);
            memberValues.put("name", name);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ????????????@Excel?????????name??????
     */
    public static void dynamicResetAnnotation(Class clazz, Map<String, String> excelMap) {
        if (excelMap.isEmpty()) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Excel.class)) {
                    if (excelMap.containsKey(field.getName())) {
                        Excel annotation = field.getAnnotation(Excel.class);
                        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                        String sourceName = excelMap.get(field.getName());
                        changeAnnotationValue(handler, sourceName);
                    }
                } else if (field.isAnnotationPresent(ExcelCollection.class) && field.getType().isAssignableFrom(List.class)) {
                    // ExcelCollection???????????????????????????
                    if (excelMap.containsKey(field.getName())) {
                        ExcelCollection annotation = field.getAnnotation(ExcelCollection.class);
                        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
                        String sourceName = excelMap.get(field.getName());
                        changeAnnotationValue(handler, sourceName);
                        // ExcelCollection???????????????????????????????????????
                    } else {
                        Type type = field.getGenericType();
                        if (type instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) type;
                            Class collectionClazz = (Class) pt.getActualTypeArguments()[0];
                            dynamicResetAnnotation(collectionClazz, excelMap);
                        }
                    }
                } else if (field.isAnnotationPresent(ExcelEntity.class)) {
                    Class entityClazz = field.getType();
                    dynamicResetAnnotation(entityClazz, excelMap);
                }
            }
        } catch (Exception e) {
            log.error("???????????????????????????????????????????????????", e);
        }
    }

    /**
     * create by: liumeng
     * description: ???????????????
     * create time: 2021/3/3 9:25
     *
     * @param list
     * @param export
     * @param fileName
     * @param clazz
     * @return: void
     */
    public static void CommonExport(List<?> list, Map<String, String> export, String fileName, Class<?> clazz) {
        if (list.size() < 1) {
            throw new RuntimeException("??????????????????");
        }
        HttpServletResponse response = CommonUtils.getResponse();
        if (export != null && export.size() > 1) {
            Map<String, String> excelMap = new HashMap<>();
            ExcelUtils.dynamicChangeAndSaveSourceAnnotation(export, clazz, excelMap);
            ExcelUtils.exportExcel(list, fileName, clazz, fileName + ".xlsx", response);
            ExcelUtils.dynamicResetAnnotation(clazz, excelMap);
        } else {
            ExcelUtils.exportExcel(list, fileName, clazz, fileName + ".xlsx", response);
        }
    }
}

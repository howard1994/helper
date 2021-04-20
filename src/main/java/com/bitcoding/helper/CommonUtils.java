package com.bitcoding.helper;

import com.alibaba.fastjson.JSON;
import com.bitcoding.helper.aop.PermFilter;
import com.bitcoding.helper.entity.common.FieldQuery;
import com.bitcoding.helper.entity.common.OrderBy;
import com.bitcoding.helper.entity.common.PageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/2 15:10
 *
 * @author LongQi-Howard
 */
public class CommonUtils {
    /**
     * create by: liumeng
     * description: fileName编码
     * create time: 2020/12/8 16:54
     *
     * @param fileName
     * @return: java.lang.String
     */
    public static String setFileName(String fileName) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String agent = request.getHeader("USER-AGENT").toLowerCase();
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            if (agent.contains("firefox")) {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * description: 获取响应
     * create by: liumeng
     * create time: 2021/1/25 14:17
     *
     * @param
     * @return: javax.servlet.http.HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletRequestAttributes.getResponse();
    }

    /**
     * description: 判断字符串是否为空
     * create by: liumeng
     * create time: 2021/3/4 16:00
     *
     * @param str
     * @return: boolean
     */
    public static boolean isNullOrSpace(String str) {
        boolean isNullOrSpace = false;
        if (str == null || str.trim().equals("")) {
            isNullOrSpace = true;
        }
        return isNullOrSpace;
    }

    /**
     * description: 获取response
     * create by: liumeng
     * create time: 2021/3/3 16:03
     *
     * @param
     * @return: java.lang.String
     */
    public static String getAuthToken() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String authorization = request.getHeader("Authorization");
        if (CommonUtils.isNullOrSpace(authorization)) {
            throw new RuntimeException("token不能为空");
        }
        authorization = authorization
                .replaceFirst("bearer ", "")
                .replaceFirst("Bearer", "");
        return authorization;
    }

    /**
     * description: 时间转字符串
     * create by: liumeng
     * create time: 2021/3/4 16:04
     *
     * @param date   时间
     * @param format 为空时 yyyy-MM-dd HH:mm:ss
     * @return: java.lang.String
     */
    public static String dateToStr(Date date, String format) {
        format = format == null ? "yyyy-MM-dd HH:mm:ss" : format;
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * description: 字符串转时间
     * create by: liumeng
     * create time: 2021/3/4 16:05
     *
     * @param dateStr
     * @param format
     * @return: java.util.Date
     */
    public static Date strToDate(String dateStr, String format) {
        format = format == null ? "yyyy-MM-dd HH:mm:ss" : format;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date parse = null;
        try {
            parse = sdf.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parse;
    }

    /**
     * description: 时间加减
     * create time: 2020/12/22 16:02
     *
     * @param date 初始时间
     * @param type Calendar.DATE
     * @param i    数量
     * @return: java.util.Date
     */
    public static Date dateCalender(Date date, int type, int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(type, i);
        return cal.getTime();
    }

    /**
     * description: 拼装url
     * create by: liumeng
     * create time: 2021/3/9 14:48
     *
     * @param url
     * @param params
     * @return: java.lang.String
     */
    public static String mapToUrl(String url, Map<String, String> params) {
        if (params != null) {
            url += "?";
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                url += key + "=" + value + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 对象转数组
     *
     * @param obj
     * @return
     */
    public static byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            throw new RuntimeException("对象转数组出现异常:" + ex.getMessage());
        }
        return bytes;
    }

    /**
     * description:
     * create by: liumeng
     * create time: 2021/3/24 15:21
     *
     * @param code
     * @return: org.springframework.http.HttpStatus
     */
    public static HttpStatus code2HttpStatus(Integer code) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        for (HttpStatus httpStatus : HttpStatus.values()) {
            boolean b = code == httpStatus.value();
            if (b) {
                return httpStatus;
            }
        }
        return status;
    }

    /**
     * create by: liumeng
     * description: 组装存储过程参数
     * create time: 2020/10/14 16:00
     *
     * @param pageRequest
     * @param getAll
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    public static Map<String, Object> setProcPage(PageRequest pageRequest, boolean getAll) {
        Map<String, Object> map = new HashMap<>();
        if (getAll) {
            map.put("PageSize", 2147483647);
            map.put("PageNum", 1);
        } else {
            map.put("PageSize", pageRequest.getPageSize());
            map.put("PageNum", pageRequest.getPageNum());
        }
        if (null != pageRequest.getQuery() && pageRequest.getQuery().size() > 0) {
            for (FieldQuery f : pageRequest.getQuery()) {
                if (f.getOption().equals("in")) {
                    List<String> list = (List<String>) f.getValue();
                    String join = Joiner.on(",").join(list);
                    f.setValue(join);
                }
                map.put(f.getKey(), f.getValue());
            }
        }
        return map;
    }

    /**
     * create by: liumeng
     * description: 组装排序字段
     * create time: 2020/9/15 17:24
     *
     * @param order
     * @return: java.lang.String
     */
    public static String transToOrder(List<OrderBy> order) {
        String queryBy = null;
        if (null == order || order.size() == 0) {
            return queryBy;
        }
        for (OrderBy o : order) {
            if (null == queryBy) {
                queryBy = o.getBy() + " " + o.getOrder();
            } else {
                queryBy += ", " + o.getBy() + " " + o.getOrder();
            }
        }
        return queryBy;
    }

    /**
     * description: 生成uuid
     * create by: liumeng
     * create time: 2021/4/20 13:19
     *
     * @param
     * @return: java.lang.String
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

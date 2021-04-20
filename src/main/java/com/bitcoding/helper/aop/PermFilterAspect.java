package com.bitcoding.helper.aop;

import com.alibaba.fastjson.JSON;
import com.bitcoding.helper.CommonUtils;
import com.bitcoding.helper.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @author LongQi-Howard
 */
@Aspect
@Component
@Configuration
public class PermFilterAspect {
    @Value("${helper.auth-server.checkPermUrl}")
    private String url;

    @Resource
    private HttpUtils httpUtils;

    @Pointcut("@annotation(permFilter)")
    public void doPermFilter(PermFilter permFilter) {
    }

    /**
     * 方法前执行
     */
    @Before("doPermFilter(permFilter)")
    public void deBefore(PermFilter permFilter) throws Throwable {
        boolean access = false;
        String authToken = CommonUtils.getAuthToken();
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", authToken);
        String[] perms = ArrayUtils.addAll(permFilter.hasRole(), permFilter.hasPerm());
        if (permFilter.hasPerm().length > 0) {
            String s = httpUtils.httpPost(url, map, JSON.toJSONString(perms));
            ObjectMapper objectMapper = new ObjectMapper();
            Map res = objectMapper.readValue(s, Map.class);
            boolean success = (boolean) res.get("success");
            access = success;
        }
        if (access) {
            return ;
        } else {
            throw new RuntimeException("没有访问该方法的权限");
        }
    }

}

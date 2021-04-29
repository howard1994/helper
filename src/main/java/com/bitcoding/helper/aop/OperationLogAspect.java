package com.bitcoding.helper.aop;

import com.alibaba.fastjson.JSON;
import com.bitcoding.helper.CommonUtils;
import com.bitcoding.helper.JwtUtils;
import com.bitcoding.helper.entity.common.LogModel;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/19 14:44
 *
 * @author LongQi-Howard
 */
@Aspect
@Component
@Configuration
@Slf4j
public class OperationLogAspect {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${helper.rabbitmq.exchange}")
    private String exchange;

    @Value("${helper.rabbitmq.routing}")
    private String routingKey;


    @Pointcut("@annotation(operationLog)")
    public void doOperationLog(OperationLog operationLog) {
    }

    @Before("doOperationLog(operationLog)")
    public void before(JoinPoint jp, OperationLog operationLog) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String authToken = CommonUtils.getAuthToken();
        Claims claims = JwtUtils.parserToken(authToken);
        String email = String.valueOf(claims.get("email"));
        Object[] args = jp.getArgs();
        List<Object> objectList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            objectList.add(args[i]);
        }
        String s = JSON.toJSONString(objectList);
        rabbitTemplate.convertAndSend(exchange, routingKey, new LogModel(operationLog.fromClass(), operationLog.level(), email + ":" + operationLog.message() + operationLog.fromClass(), s));
        log.info(JSON.toJSONString(new LogModel(operationLog.fromClass(), operationLog.level(), email + ":" + operationLog.message() + operationLog.fromClass(), s)));
    }

    @AfterThrowing(value = "doOperationLog(operationLog)", throwing = "ex")
    public void ex(JoinPoint jp, OperationLog operationLog, Exception ex) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        String authToken = CommonUtils.getAuthToken();
        Claims claims = JwtUtils.parserToken(authToken);
        String email = String.valueOf(claims.get("email"));
        Object[] args = jp.getArgs();
        List<Object> objectList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            objectList.add(args[i]);
        }
        String s = JSON.toJSONString(objectList);
        rabbitTemplate.convertAndSend(exchange, routingKey, new LogModel(operationLog.fromClass(), "ERROR", email + ":调用" + operationLog.fromClass() + "出现异常:" + ex.getMessage(), s));
        log.error(JSON.toJSONString(new LogModel(operationLog.fromClass(), "ERROR", email + ":调用" + operationLog.fromClass() + "出现异常:" + ex.getMessage(), s)));
    }
}

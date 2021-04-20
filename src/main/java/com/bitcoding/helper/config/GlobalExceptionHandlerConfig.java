package com.bitcoding.helper.config;

import com.bitcoding.helper.entity.common.ResponseInfo;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * create by: liumeng
 * description: 全局异常处理
 * create time: 2020/11/19 9:32
 *
 * @author LongQi-Howard
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandlerConfig  {

    /**
     * 运行异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseInfo runtimeExceptionHandler(RuntimeException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 空指针
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseInfo nullExceptionHandler(NullPointerException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 类型转换异常
     */
    @ExceptionHandler(ClassCastException.class)
    public ResponseInfo classExceptionHandler(ClassCastException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseInfo ioExceptionHandler(IOException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 未知方法异常
     */
    @ExceptionHandler(NoSuchMethodException.class)
    public ResponseInfo noSuchExceptionHandler(NoSuchMethodException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 数组越界异常
     */
    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseInfo indexOutExceptionHandler(IndexOutOfBoundsException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 栈溢出
     */
    @ExceptionHandler(StackOverflowError.class)
    public ResponseInfo stackOverflowError(StackOverflowError ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 除数不能为0
     */
    @ExceptionHandler(ArithmeticException.class)
    public ResponseInfo arithmeticException(ArithmeticException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    /**
     * 400错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseInfo httpMessageExceptionHandler(HttpMessageNotReadableException ex) {
        return ResponseInfo.fail(400, ex.getMessage());
    }

    /**
     * 400错误
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseInfo typeMismatchExceptionHandler(TypeMismatchException ex) {
        return ResponseInfo.fail(400, ex.getMessage());
    }

    /**
     * 400错误
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseInfo missingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseInfo.fail(400, ex.getMessage());
    }

    /**
     * 405错误
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseInfo httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseInfo.fail(405, ex.getMessage());
    }

    /**
     * 406错误
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseInfo httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex) {
        return ResponseInfo.fail(406, ex.getMessage());
    }

    /**
     * 500错误
     */
    @ExceptionHandler({ConversionNotSupportedException.class})
    public ResponseInfo conversionNotSupportedException(ConversionNotSupportedException ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseInfo exception(Exception ex) {
        return ResponseInfo.fail(500, ex.getMessage());
    }
}

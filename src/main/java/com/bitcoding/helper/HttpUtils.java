package com.bitcoding.helper;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/9 14:31
 *
 * @author LongQi-Howard
 */
public class HttpUtils {

    private RestTemplate restTemplate;

    public HttpUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * description: get请求,有参
     * create by: liumeng
     * create time: 2021/3/9 14:40
     *
     * @param url   CommonUtils.mapToUrl(String url, Map<String, String> params)
     * @param heads
     * @return: java.lang.String
     */
    public String httpGet(String url, Map<String, String> heads) {
        HttpHeaders headers = new HttpHeaders();
        if (heads != null) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                headers.add(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity request = new HttpEntity<>(headers);
        ResponseEntity responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return String.valueOf(responseEntity.getBody());
    }

    /**
     * description: POST请求
     * create by: liumeng
     * create time: 2021/3/16 14:20
     *
     * @param url
     * @param heads
     * @param params
     * @return: java.lang.String
     */
    public String httpPost(String url, Map<String, String> heads, String params) {
        HttpHeaders headers = new HttpHeaders();
        if (heads != null) {
            for (Map.Entry<String, String> entry : heads.entrySet()) {
                headers.add(entry.getKey(), entry.getValue());
            }
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity<>(params, headers);
        ResponseEntity responseEntity = restTemplate.postForEntity(url, request, String.class);
        return String.valueOf(responseEntity.getBody());
    }
}

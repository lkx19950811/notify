package com.lee.leo.controller;

import com.alibaba.fastjson.JSONObject;
import com.huifu.saturn.cfca.CFCASignature;
import com.huifu.saturn.cfca.VerifyResult;
import com.lee.leo.util.DataHelper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 描述:
 *
 * @author Leo
 * @create 2018-03-13 上午 2:11
 */
@RestController
public class TestController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());//
    @RequestMapping(value = "/test/show",produces = MediaType.TEXT_PLAIN_VALUE)
    public String show(HttpServletRequest request) throws Exception {
        Map<String,String[]> map = request.getParameterMap();
        String str = DataHelper.getStrFromREQ(request);
        JSONObject json = null;
        String xml = null;
        try {
            json = JSONObject.parseObject(str);
        }catch (Exception e){
            xml = str;
        }
        if (map.size()==0){
            logger.info("在body中的参数:{}",str);
            if (json!=null){
                return "SUCCESS";
            }else if (xml!=null){
                return "SUCCESS";
            }else {
                return "nothing";
            }
        }else {
            StringBuilder sb = new StringBuilder();
            map.forEach((k,v)-> sb.append(k).append("=").append(v[0]).append("&"));
            logger.info("使用 参数{}",sb.toString());
            if (request.getParameter("check_value")!=null){
                JSONObject jsonObject = JSONObject.parseObject(
                        parseResult(request.getParameter("check_value"),"/home/key/huifu/CFCA_ACS_OCA31.cer"));
                logger.info(jsonObject.toJSONString());
                logger.info("RECV_ORD_ID_" + jsonObject.getString("order_id"));
                logger.info(jsonObject.toJSONString());
                return "RECV_ORD_ID_" + jsonObject.getString("order_id");
            }
            return "SUCCESS";
        }
    }
    @RequestMapping(value = "/show/{show}",produces = MediaType.TEXT_PLAIN_VALUE)
    public String showStats(HttpServletRequest request, @PathVariable("show")String show) throws Exception {
        Map<String,String[]> map = request.getParameterMap();
        String str = DataHelper.getStrFromREQ(request);
        if (map.size()==0){
            logger.info("在body中的参数:{}",str);
        }else {
            StringBuilder sb = new StringBuilder();
            map.forEach((k,v)-> sb.append(k).append("=").append(v[0]).append("&"));
            logger.info("使用 参数{}",sb.toString());
        }
        return show;
    }
    private String parseResult(String responseJson, String key) throws Exception {
        // 解签用的证书，请换成商户自己下载的证书
        VerifyResult verifyResult = CFCASignature.verifyMerSign("100001", responseJson, "utf-8", key);
        if ("000".equals(verifyResult.getCode())) {
            String content = new String(verifyResult.getContent(), Charset.forName("utf-8"));
            return new String(Base64.decodeBase64(content), Charset.forName("utf-8"));
        } else {
            return "验签失败";
        }
    }
}

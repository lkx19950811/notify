package com.lee.leo.util;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.java.emoji.EmojiConverter;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author leon
 * @date 2018-06-01 15:10
 * @desc 数据处理类
 */
public class DataHelper {
    private static EmojiConverter emojiConverter = EmojiConverter.getInstance();

    /**
     * 字节数据转字符串专用集合
     */
    private static final char[] HEX_CHAR= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     *  排序过滤拼接字符串
     * @param params 注满参数的Map
     * @param filterKey 你想要过滤的字符串
     * @return
     */
    public static String GetSortFilterQueryString(Map<String, String> params, String[] filterKey)
    {
        List<Map.Entry<String, String>> keyValues = new ArrayList<>(params.entrySet());

        Collections.sort(keyValues, Comparator.comparing(o -> (o.getKey())));
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<keyValues.size();i++) {
            boolean filter=false;
            if(filterKey!=null&&filterKey.length>0)
            {
                for(int index=0;index<filterKey.length;index++)
                {
                    if(filterKey[index].equalsIgnoreCase(keyValues.get(i).getKey()))
                    {
                        filter=true;
                        break;
                    }
                }
            }
            //过滤的KEY不参与
            if(filter) continue;
            sb.append(keyValues.get(i).getKey()+ "=" + keyValues.get(i).getValue());
            sb.append("&");
        }
        return sb.substring(0, sb.length()-1);
    }

    /**
     * 不排序,直接拼接请求字符串
     * @param params 注满参数的map
     * @return
     */
    public static String GetQueryString(Map<String, String> params)
    {
        Iterator<Map.Entry<String, String>> iter = params.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            sb.append(key + "=" +val).append("&");
        }
        if(sb.length()==0) return "";
        return sb.substring(0, sb.length()-1);
    }

    /**
     * 排序过后拼接字符串并转化为小写(按照ASCII码排序) 例子 a=1&b=c&bc=ab&c=5
     * @param params
     * @return
     */
    public static String GetSortQueryToLowerString(Map<String, String> params)
    {
        List<Map.Entry<String, String>> keyValues = new ArrayList<>(params.entrySet());

        Collections.sort(keyValues, Comparator.comparing(o -> (o.getKey())));

        StringBuilder sb = new StringBuilder();
        for (int i=0;i<keyValues.size();i++) {
            if(keyValues.get(i).getValue()==null)
            {
                sb.append(keyValues.get(i).getKey()+ "= " );
            }
            else
            {
                sb.append(keyValues.get(i).getKey()+ "=" + keyValues.get(i).getValue().toLowerCase());
            }
            sb.append("&");
        }

        return sb.substring(0, sb.length()-1);

    }

    /**
     * 排序过后拼接字符串(按照ASCII码排序) 例子:A=1&b=c&bc=ab&c=5
     * @param params 注满参数的map
     * @return
     */
    public static String GetSortQueryString(Map params)
    {
        params.forEach((k,v)->params.put(k,v.toString()));
        List<Map.Entry<String, String>> keyValues =
                new ArrayList<>(params.entrySet());

        Collections.sort(keyValues, Comparator.comparing(o-> (o.getKey())));

        StringBuilder sb = new StringBuilder();
        for (int i=0;i<keyValues.size();i++) {
            sb.append(keyValues.get(i).getKey()+ "=" + keyValues.get(i).getValue());
            sb.append("&");
        }

        return sb.substring(0, sb.length()-1);

    }
    /**
     * 将XML解析成MAP
     * @author Leo
     * @version 0.1
     */
    public static Map<String, String> getXML(String requestXml){
        Map<String, String> map = new HashMap();
        // 将字符串转为XML
        Document doc;
        try {
            doc = DocumentHelper.parseText(requestXml);
            // 获取根节点
            Element rootElm = doc.getRootElement();//从root根节点获取请求报文
            map = parseXML(rootElm, new HashMap());
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return map;
    }
    /**
     * 将xml解析成map键值对
     * <功能详细描述>
     * @param ele 需要解析的xml对象
     * @param map 入参为空，用于内部迭代循环使用
     * @return map键值对
     * @see
     */
    public static Map<String, String> parseXML(Element ele, Map<String, String> map)
    {
        for (Iterator<?> i = ele.elementIterator(); i.hasNext();)
        {
            Element node = (Element)i.next();
            //System.out.println("parseXML node name:" + node.getName());
            if (node.attributes() != null && node.attributes().size() > 0)
            {
                for (Iterator<?> j = node.attributeIterator(); j.hasNext();)
                {
                    Attribute item = (Attribute)j.next();
                    map.put(item.getName(), item.getValue());
                }
            }
            if (node.getText().length() > 0)
            {
                map.put(node.getName(), node.getText());
            }
            if (node.elementIterator().hasNext())
            {
                parseXML(node, map);
            }
        }
        return map;
    }
    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequestParams(String URL)
    {
        Map<String, String> mapRequest = new HashMap<>();
        String[] arrSplit;
        String strUrlParam=URL;
        if(strUrlParam==null)
        {
            return mapRequest;
        }
        strUrlParam=URL.indexOf("?")>0?URL.substring(URL.indexOf("[?]")+1) :URL ;
        //解决返回值中带有URL含参数“?”导致破坏键值对 返回的URL后的参数 需要重新组合
        //例如：key1=value1&key2=value2&key3=url?a=a1&b=b1&key4=value4
        //分别获取后的键值为 key1,key2,key3,akey,bkey,key4
        //如需获取key3 的原始值 key3+"?"+akey+bkey
        strUrlParam=strUrlParam.replaceAll("[?]", "&");
        //每个键值为一组
        arrSplit=strUrlParam.split("[&]");
        for(String strSplit:arrSplit)
        {
            String[] arrSplitEqual;
            arrSplitEqual= strSplit.split("[=]");
            //解析出键值
            if(arrSplitEqual.length>1)
            {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            }
            else
            {
                if(arrSplitEqual[0]!="")
                {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
    /**
     * 字符串转换成为16进制(无需Unicode编码)
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;//取余转换
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }
    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     * @param hexStr
     * @return
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }
    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */

    /**
     * 字节数据转十六进制字符串
     * @param data 输入数据
     * @return 十六进制内容
     */
    public static String byteArrayToString(byte[] data){
        StringBuilder stringBuilder= new StringBuilder();
        for (int i=0; i<data.length; i++){
            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0)>>> 4]);
            //取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i<data.length-1){
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 从httpRequest的body中读取json对象
     * @param request HTTPRequest对象
     * @return json对象
     */
    public static JSONObject getJsonFromREQ(HttpServletRequest request){
        JSONObject res = null;
		BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            StringBuffer sb = new StringBuffer();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
            String params = sb.toString();
            res = JSONObject.parseObject(params);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (JSONException e){
            res = new JSONObject();
        }finally {
			try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
    }
    /**
     * 从httpRequest的body中读取str
     * @param request HTTPRequest对象
     * @return str
     */
    public static String getStrFromREQ(HttpServletRequest request){
        String res = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            StringBuilder sb = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
            res = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
    /**
     * 将emojiStr 转码
     * 正常字符不受牵连
     * @param emojiStr
     * @return
     */
    public static String emojiEncode(String emojiStr){
        return emojiConverter.toAlias(emojiStr);
    }

    /**
     * 带有表情的字符串解码
     * @param str
     * @return
     */
    public static String emojiDecode(String str){
        return emojiConverter.toUnicode(str);
    }

}

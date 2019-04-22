package com.lee.leo.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Aspect
@Component
public class CareTopicLogg {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    @Around("execution(* com.lee.leo.controller..*.*(..))")
    public Object logServiceInvoke(ProceedingJoinPoint joinPoint) throws Throwable
    {
        StringBuffer argsBuf = new StringBuffer();
        if(joinPoint.getArgs().length > 0)
        {
            for(int i = 0 ; i < joinPoint.getArgs().length; i++)
            {
                Object arg = joinPoint.getArgs()[i];

                String argName = getArgName(joinPoint)[i];

                argsBuf.append("参数").append(i + 1).append("-").append(argName).append(":");

                appendArgContent(argsBuf, arg);
            }
        }

        StringBuilder request = new StringBuilder("\n调用服务接口:" + joinPoint.getSignature() + ".\n实现类:" + joinPoint.getTarget() + "\n");

        if(!StringUtils.isEmpty(argsBuf.toString()))
        {
            request.append(argsBuf);
        }

        LOGGER.info(request.toString());

        Object result = joinPoint.proceed();

        StringBuffer response = new StringBuffer("返回：");

        appendArgContent(response, result);

        LOGGER.info(response.toString());
        return result;
    }

    private String[] getArgName(ProceedingJoinPoint joinPoint) throws Exception
    {
        Method interfaceMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method method = joinPoint.getTarget().getClass().getDeclaredMethod(joinPoint.getSignature().getName(), interfaceMethod.getParameterTypes());

        return parameterNameDiscoverer.getParameterNames(method);
    }

    @SuppressWarnings("rawtypes")
    private void appendArgContent(StringBuffer argsBuf, Object arg) {
        if(arg == null){
            argsBuf.append("null\n");
        }else{
            if(arg instanceof ServletResponse){
                argsBuf.append("HttpSevletResponse \n");
            }else if(arg.getClass().isPrimitive() || arg instanceof String){
                argsBuf.append(arg.toString()).append("\n");
            }else if(arg.getClass().isArray()){
                Object[] args = (Object[])arg;
                argsBuf.append("size:").append(args.length).append("\n");
                for (Object sub : args) {
                    appendArgContent(argsBuf, sub);
                }
            }else if(arg instanceof Collection){
                Collection args = (Collection)arg;
                argsBuf.append("size:").append(args.size()).append("\n");
                for (Object sub : args) {
                    appendArgContent(argsBuf, sub);
                }
            }else if(arg instanceof Page){
                argsBuf.append(writeArgToJson(arg)).append("\n");
                appendArgContent(argsBuf, ((Page)arg).getContent());
            }else if(arg instanceof Map){
                Map<?, ?> map = (Map<?, ?>)arg;
                argsBuf.append("size:").append(map.size()).append("\n");
                argsBuf.append(String.valueOf(map));
            }else{
                argsBuf.append(writeArgToJson(arg)).append("\n");
            }
        }
    }
    private ObjectMapper objectMapper = new ObjectMapper();
    private String writeArgToJson(Object arg) {
        try {
            return objectMapper.writeValueAsString(arg);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}

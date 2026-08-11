package com.ruoyi.framework.web.response;

import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Keeps the legacy AjaxResult code and HTTP status synchronized. */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class HttpStatusResponseBodyAdvice implements ResponseBodyAdvice<Object>
{
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType)
    {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response)
    {
        Integer status = resolveStatus(body);
        if (status != null) response.setStatusCode(org.springframework.http.HttpStatus.valueOf(status));
        return body;
    }

    /** Resolve only real HTTP error codes; legacy warning code 601 remains an HTTP 200 response. */
    public static Integer resolveStatus(Object body)
    {
        if (!(body instanceof Map<?, ?> values)) return null;
        Object code = values.get("code");
        if (!(code instanceof Number number)) return null;
        int status = number.intValue();
        return status >= 400 && status <= 599 ? status : null;
    }
}

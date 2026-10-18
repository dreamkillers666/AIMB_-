package com.xz.springboot.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * ENSO AI 助手 —— 同源转发控制器。
 *
 * <p>上游网关 api.gs88.shop 未返回 Access-Control-Allow-Origin，浏览器直连会被 CORS 拦截，
 * 因此统一由后端转发：前端只请求同源的 {@code /ai-gateway/v1/**}，也就无需在页面里暴露 apiKey。</p>
 *
 * <p>开发环境同一条路径由 vue.config.js 的 devServer.proxy 承担，生产环境由本控制器承担，
 * 两边路径完全一致，前端不需要区分环境。</p>
 *
 * <p>注意：流式分支直接写 {@link HttpServletResponse}。不要把 StreamingResponseBody 包进
 * ResponseEntity 返回——Spring MVC 只对裸返回类型启用流式处理器，包一层后会被当作普通
 * 响应体去找消息转换器，抛 HttpMessageNotWritableException（No converter ... text/event-stream）。</p>
 */
@RestController
@RequestMapping("/ai-gateway/v1")
public class AiGatewayController {

    /** 上游 OpenAI Responses 兼容网关 */
    private static final String UPSTREAM = "https://api.gs88.shop/v1";

    /** 网关密钥（如需更换，改这里即可；对外发布建议移到 application.yml 或环境变量） */
    private static final String API_KEY = "sk-9214623399b1dc4ad9bbaaf870794bdeb4339a8d248ca144c3c976ec682dee2a";

    private static final Pattern STREAM_FLAG = Pattern.compile("\"stream\"\\s*:\\s*true");

    private final RestTemplate restTemplate;

    public AiGatewayController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 不缓冲请求体，保证接入流式请求；读超时放宽到 5 分钟，避免长回答被掐断
        factory.setBufferRequestBody(false);
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(300000);
        this.restTemplate = new RestTemplate(factory);
    }

    /** 模型目录：前端用它做「接口可用性 + 模型是否存在」的连通性探测 */
    @GetMapping("/models")
    public ResponseEntity<byte[]> models() {
        return relayOnce(HttpMethod.GET, "/models", null);
    }

    /**
     * 对话接口。请求体带 {@code "stream": true} 时以 SSE 逐段回传，否则一次性回传。
     */
    @PostMapping("/responses")
    public void responses(@RequestBody(required = false) byte[] body, HttpServletResponse httpResponse) throws Exception {
        if (isStreamRequest(body)) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            httpResponse.setContentType("text/event-stream;charset=UTF-8");
            httpResponse.setHeader("Cache-Control", "no-cache, no-transform");
            httpResponse.setHeader("X-Accel-Buffering", "no");
            try {
                relayStream("/responses", body, httpResponse.getOutputStream());
            } catch (HttpStatusCodeException e) {
                // 上游在出流前报错（401/429/5xx 等）：尚未提交响应则原样透传状态与错误体
                if (!httpResponse.isCommitted()) {
                    httpResponse.reset();
                    httpResponse.setStatus(e.getRawStatusCode());
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    byte[] payload = e.getResponseBodyAsByteArray();
                    httpResponse.getOutputStream().write(payload.length > 0 ? payload
                            : "{\"error\":{\"message\":\"AI 网关上游返回错误\"}}".getBytes(StandardCharsets.UTF_8));
                    httpResponse.getOutputStream().flush();
                } else {
                    writeSseError(httpResponse, e.getMessage());
                }
            } catch (Exception e) {
                writeSseError(httpResponse, e.getMessage());
            }
            return;
        }
        ResponseEntity<byte[]> result = relayOnce(HttpMethod.POST, "/responses", body);
        httpResponse.setStatus(result.getStatusCodeValue());
        MediaType contentType = result.getHeaders().getContentType();
        httpResponse.setContentType(contentType != null ? contentType.toString() : "application/json;charset=UTF-8");
        if (result.getBody() != null && result.getBody().length > 0) {
            httpResponse.getOutputStream().write(result.getBody());
        }
        httpResponse.getOutputStream().flush();
    }

    private boolean isStreamRequest(byte[] body) {
        if (body == null || body.length == 0) {
            return false;
        }
        return STREAM_FLAG.matcher(new String(body, StandardCharsets.UTF_8)).find();
    }

    private void writeSseError(HttpServletResponse response, String message) throws Exception {
        if (response.isCommitted()) {
            return;
        }
        String safe = message == null ? "AI 网关转发失败" : message.replace("\"", "'");
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":{\"message\":\"" + safe + "\"}}");
        response.getWriter().flush();
    }

    private HttpHeaders upstreamHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.set("Authorization", "Bearer " + API_KEY);
        return headers;
    }

    /** 一次性转发（保留上游状态码，便于前端区分 401 / 429 等） */
    private ResponseEntity<byte[]> relayOnce(HttpMethod method, String path, byte[] body) {
        try {
            ResponseEntity<byte[]> upstream = restTemplate.exchange(
                    URI.create(UPSTREAM + path),
                    method,
                    new HttpEntity<>(body, upstreamHeaders()),
                    byte[].class);
            HttpHeaders headers = new HttpHeaders();
            MediaType contentType = upstream.getHeaders().getContentType();
            headers.setContentType(contentType != null ? contentType : MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(upstream.getBody(), headers, upstream.getStatusCode());
        } catch (HttpStatusCodeException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(e.getResponseBodyAsByteArray(), headers, e.getStatusCode());
        } catch (Exception e) {
            return gatewayError(e);
        }
    }

    /** 流式转发：边读上游边写下游，保证逐字效果 */
    private void relayStream(String path, byte[] body, OutputStream outputStream) {
        restTemplate.execute(URI.create(UPSTREAM + path), HttpMethod.POST, request -> {
            request.getHeaders().putAll(upstreamHeaders());
            if (body != null && body.length > 0) {
                request.getBody().write(body);
            }
        }, response -> {
            InputStream input = response.getBody();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                outputStream.flush();
            }
            return null;
        });
    }

    private ResponseEntity<byte[]> gatewayError(Exception e) {
        String message = e.getMessage() == null ? "AI 网关转发失败" : e.getMessage().replace("\"", "'");
        String payload = "{\"error\":{\"message\":\"" + message + "\"}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(payload.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_GATEWAY);
    }
}

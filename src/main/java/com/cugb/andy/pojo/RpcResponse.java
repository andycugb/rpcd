package com.cugb.andy.pojo;

/**
 * Created by jbcheng on 3/3/17.
 * response响应封装
 */
public class RpcResponse {
    private String requestId;// 请求唯一标示
    private Throwable error;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}

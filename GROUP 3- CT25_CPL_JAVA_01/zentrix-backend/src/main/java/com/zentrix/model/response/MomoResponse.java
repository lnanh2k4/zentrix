package com.zentrix.model.response;

import lombok.Data;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
@Data
public class MomoResponse {
    protected long responseTime;

    public long getResponseTime() {
        return System.currentTimeMillis();
    }

    protected String message;

    private String partnerCode;
    private String orderId;
    protected Integer resultCode;

    public MomoResponse() {
        this.responseTime = System.currentTimeMillis();
    }

}

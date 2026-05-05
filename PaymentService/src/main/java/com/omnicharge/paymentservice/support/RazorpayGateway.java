package com.omnicharge.paymentservice.support;

public interface RazorpayGateway {
    String createOrderId(Double amount, String receipt, String keyId, String keySecret) throws Exception;
}

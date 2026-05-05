package com.omnicharge.paymentservice.support;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class DefaultRazorpayGateway implements RazorpayGateway {

    @Override
    public String createOrderId(Double amount, String receipt, String keyId, String keySecret) throws Exception {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);

        Order razorpayOrder = client.orders.create(orderRequest);
        return razorpayOrder.get("id");
    }
}

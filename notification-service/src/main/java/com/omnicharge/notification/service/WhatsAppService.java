package com.omnicharge.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.whatsappNumber}")
    private String fromNumber;

    public void sendWhatsAppMessage(String to, String message) {

        String toNumber = "whatsapp:" + to;

        System.out.println("Sending WhatsApp...");
        System.out.println("FROM: " + fromNumber);
        System.out.println("TO: " + toNumber);
        System.out.println("MSG: " + message);

        Message twilioMessage = Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(fromNumber),
                message
        ).create();

        System.out.println("SID: " + twilioMessage.getSid());
        System.out.println("WhatsApp sent to: " + to);
    }
}
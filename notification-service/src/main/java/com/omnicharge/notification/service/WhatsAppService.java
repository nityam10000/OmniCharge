package com.omnicharge.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${twilio.whatsappNumber}")
    private String fromNumber;

    public void sendWhatsAppMessage(String to, String message) {

        String toNumber = "whatsapp:" + to;

        log.info("Sending WhatsApp message - FROM: {}, TO: {}", fromNumber, toNumber);
        log.debug("Message content: {}", message);

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("WhatsApp message sent successfully - SID: {}, To: {}", twilioMessage.getSid(), to);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to: {}", to, e);
            throw e;
        }
    }
}
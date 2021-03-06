package com.flipkart.foxtrot.core.alerts;
/*
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/***
 Created by nitish.goyal on 06/10/18
 ***/
public class EmailClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailClient.class);

    private final EmailConfig emailConfig;
    private final Session session;

    public EmailClient(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
        Properties mailProps = new Properties();
        mailProps.put("mail.transport.protocol", "smtp");
        mailProps.put("mail.smtp.port", emailConfig.getPort());
        mailProps.put("mail.smtp.auth", false);
        mailProps.put("mail.smtp.host", emailConfig.getHost());
        mailProps.put("mail.smtp.startttls.enable", false);
        mailProps.put("mail.smtp.timeout", 10000);
        mailProps.put("mail.smtp.connectiontimeout", 10000);
        this.session = Session.getDefaultInstance(mailProps);
    }

    public void sendEmail(String subject, String content, String recipients) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject(subject);

            InternetHeaders headers = new InternetHeaders();
            headers.addHeader("Content-type", "text/html; charset=UTF-8");

            BodyPart messageBodyPart = new MimeBodyPart(headers, content.getBytes("UTF-8"));
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message, emailConfig.getUser(), emailConfig.getPassword());
        } catch (Exception e) {
            LOGGER.error("Error occurred while sending the email : " + e);

        }
    }
}

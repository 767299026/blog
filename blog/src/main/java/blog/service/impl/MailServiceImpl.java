package blog.service.impl;

import blog.service.MailService;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

@Service
@Transactional
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private MailProperties mailProperties;

    /**
     * 发送普通文本邮件
     *
     * @param toAccount 收件人
     * @param subject 主题
     * @param content 内容
     */
    @Override
    @Async
    public void sendSimpleMail(String toAccount, String subject, String content) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(toAccount);
            message.setSubject(subject);
            message.setText(content);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送HTML邮件
     *
     * @param toAccount 收件人
     * @param subject 主题
     * @param content 内容（可以包含<html>等标签）
     */
    @Override
    @Async
    public void sendHtmlMail(String toAccount, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(mailProperties.getUsername());
            messageHelper.setTo(toAccount);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

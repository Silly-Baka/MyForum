package MyForum.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Date: 2022/8/9
 * Time: 18:39
 *
 * @Author SillyBaka
 *
 * Description： 用于发送电子邮件的客户端工具类
 **/
@Component
@Slf4j
public class MailClient {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    public void sendMail(String to,String subject,String content){
        THREAD_POOL.execute(new sendMailTask(to, subject, content));
    }
    private class sendMailTask implements Runnable{
        private String to;
        private String subject;
        private String content;

        public sendMailTask(String to, String subject, String content) {
            this.to = to;
            this.subject = subject;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                MimeMessage message = mailSender.createMimeMessage();

                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content,true);
                mailSender.send(helper.getMimeMessage());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}

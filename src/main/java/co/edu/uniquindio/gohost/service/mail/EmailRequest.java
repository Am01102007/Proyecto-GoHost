package co.edu.uniquindio.gohost.service.mail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String html;
    private String from;
}


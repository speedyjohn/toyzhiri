package org.example.toy_zhiri.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Запрос на отправку сообщения в чат.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @Size(max = 4000, message = "Сообщение не может превышать 4000 символов")
    private String content;

    /**
     * URL-ы вложений, загруженных заранее через POST /api/v1/files/upload.
     */
    @Size(max = 10, message = "Можно прикрепить не более 10 файлов")
    private List<String> attachmentUrls;
}
package pmesp.helpdesk37bpmm.Exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Dados devolvidos pela API quando ocorre um erro
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaErroDTO {

    // Status HTTP que mostra o tipo de erro ocorrido
    private int status;

    // Código que o frontend poderá usar para identificar o erro
    private String codigo;

    // Mensagem simples que pode ser mostrada para quem usa o sistema
    private String mensagem;

    // Data e hora em que a API devolveu o erro
    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataHora;
}

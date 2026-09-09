package pmesp.helpdesk37bpmm.Exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespostaErroDTO {

    private int status;
    private String codigo;
    private String mensagem;
    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataHora;
}

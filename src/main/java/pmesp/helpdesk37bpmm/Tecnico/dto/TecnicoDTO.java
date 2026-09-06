package pmesp.helpdesk37bpmm.Tecnico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dado necessário para cadastrar um usuário existente como técnico
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoDTO {

    @NotBlank(message = "Informe o RE do usuário.")
    @Pattern(regexp = "[0-9]{1,6}", message = "Informe o RE sem o dígito.")
    private String re;
}

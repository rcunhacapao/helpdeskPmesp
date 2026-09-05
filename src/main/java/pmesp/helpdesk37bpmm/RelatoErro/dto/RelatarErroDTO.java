package pmesp.helpdesk37bpmm.RelatoErro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.RelatoErro.enums.TipoDeErro;

// Dados enviados por quem está relatando um erro encontrado no sistema
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatarErroDTO {

    @NotNull(message = "Selecione o tipo do erro encontrado.")
    private TipoDeErro tipoErro;

    private String observacao;
}

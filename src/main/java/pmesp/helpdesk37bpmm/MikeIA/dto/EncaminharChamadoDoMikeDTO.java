package pmesp.helpdesk37bpmm.MikeIA.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;

// Dados complementares obrigatórios somente quando o atendimento precisa chegar à fila técnica.
@Data
@NoArgsConstructor
public class EncaminharChamadoDoMikeDTO {

    @NotNull(message = "Selecione a categoria para encaminhar o chamado.")
    private ChamadoCategoria categoria;

    @NotBlank(message = "Informe o local ou setor para encaminhar o chamado.")
    @Size(max = 200, message = "O local de atendimento ultrapassou o tamanho permitido.")
    private String localAtendimento;

    @NotNull(message = "Selecione a prioridade para encaminhar o chamado.")
    private ChamadoPrioridade prioridade;

    @NotBlank(message = "O resumo da triagem é obrigatório para encaminhar o chamado.")
    @Size(max = 10000, message = "O resumo da triagem ultrapassou o tamanho permitido.")
    private String resumoAtendimento;

    // Usado somente no fluxo de acesso à pasta da seção. Não cria uma nova coluna:
    // o dado segue no resumo que será entregue ao técnico.
    @Pattern(regexp = "^$|\\d{11}$", message = "Informe um CPF com 11 números.")
    private String cpf;

    public EncaminharChamadoDoMikeDTO(ChamadoCategoria categoria, String localAtendimento, ChamadoPrioridade prioridade) {
        this.categoria = categoria;
        this.localAtendimento = localAtendimento;
        this.prioridade = prioridade;
    }
}

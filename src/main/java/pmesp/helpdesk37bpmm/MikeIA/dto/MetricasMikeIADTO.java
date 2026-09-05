package pmesp.helpdesk37bpmm.MikeIA.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Métricas iniciais para o painel técnico. A estrutura pode receber novos recortes
// (categoria, solução e período) sem mudar o fluxo de abertura de chamado.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricasMikeIADTO {

    private long totalAtendimentosIniciados;
    private long totalResolvidosPeloMike;
    private long totalEncaminhadosParaTecnico;
    private long totalAbandonados;
    private double taxaResolucaoAutomatica;
}

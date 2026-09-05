package pmesp.helpdesk37bpmm.MikeIA.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;

// Uma solução conhecida para um tipo de problema, usada pelo Mike IA para sugerir passos
// antes de um chamado ser aberto. Hoje é cadastrada só via seed inicial (ver
// SolucaoConhecidaSeedRunner); um cadastro administrável fica para uma etapa futura.
@Entity
@Table(name = "tb_solucao_conhecida")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SolucaoConhecida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoCategoria categoria;

    @Column(nullable = false)
    private String titulo;

    // Cada linha do texto é um passo sugerido ao usuário, na ordem em que aparece.
    // Escreva em linguagem simples: quem lê é um policial sem conhecimento técnico,
    // então evite termos como "spooler", "fila de impressão", "driver", etc.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String passos;

    // Palavras separadas por vírgula usadas para encontrar esta solução quando o
    // usuário não escolhe uma categoria (ex.: "impressora, papel, imprimir").
    @Column(name = "palavras_chave", columnDefinition = "TEXT")
    private String palavrasChave;

    @Column(nullable = false)
    private boolean ativo = true;

    // Define qual solução aparece primeiro quando há mais de uma para a mesma categoria
    @Column(nullable = false)
    private int ordem = 0;
}

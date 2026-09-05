package pmesp.helpdesk37bpmm.MikeIA.service;

import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;

// Abstração do "cérebro" do Mike IA: recebe o problema relatado e devolve sugestões de
// solução. A primeira versão (MotorDeResolucaoBaseadoEmRegras) só consulta uma base de
// soluções cadastradas. No futuro, uma nova classe implementando esta mesma interface
// pode usar um LLM/RAG real, sem precisar mudar controller, service ou frontend.
public interface MotorDeResolucaoMikeIA {

    OrientacaoDoMike buscarSugestoes(String descricaoProblema, ChamadoCategoria categoria);
}

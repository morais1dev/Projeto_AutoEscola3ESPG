package br.com.fiap3espg.autoescola3espg.domain.instrucao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    //Instruções canceladas (motivoCancelamento preenchido) não ocupam horário
    boolean existsByInstrutorIdAndDataHoraAndMotivoCancelamentoIsNull(Long idInstrutor, LocalDateTime dataHora);

    boolean existsByAlunoIdAndDataHoraAndMotivoCancelamentoIsNull(Long idAluno, LocalDateTime dataHora);

    long countByAlunoIdAndDataHoraBetweenAndMotivoCancelamentoIsNull(Long idAluno, LocalDateTime inicio, LocalDateTime fim);
}

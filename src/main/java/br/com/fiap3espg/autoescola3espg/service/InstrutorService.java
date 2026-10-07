package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.instrutor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstrutorService {
    private final InstrutorRepository repository;

    @Transactional
    public DadosDetalhamentoInstrutor cadastrarInstrutor(
            DadosCadastroInstrutor dados) {
        if (repository.existsByEmail(dados.email())) {
            throw new ValidacaoException("Já existe um instrutor cadastrado com o e-mail informado!");
        }
        if (repository.existsByCnh(dados.cnh())) {
            throw new ValidacaoException("Já existe um instrutor cadastrado com a CNH informada!");
        }
        Instrutor instrutor = new Instrutor(dados);
        Instrutor saved = repository.save(instrutor);
        return new DadosDetalhamentoInstrutor(saved);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemInstrutor> listarInstrutores(
            Pageable paginacao) {
        return repository
                .findAllByAtivoTrue(paginacao)
                .map(DadosListagemInstrutor::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoInstrutor detalharInstrutor(Long id) {
        Instrutor instrutor = buscarInstrutor(id);
        return new DadosDetalhamentoInstrutor(instrutor);
    }

    @Transactional
    public DadosDetalhamentoInstrutor atualizarInstrutor(DadosAtualizacaoInstrutor dados) {
        //Regras de negócio: não permitir alteração de e-mail, CNH e especialidade
        if (dados.email() != null) {
            throw new ValidacaoException("Não é permitido alterar o e-mail do instrutor!");
        }
        if (dados.cnh() != null) {
            throw new ValidacaoException("Não é permitido alterar a CNH do instrutor!");
        }
        if (dados.especialidade() != null) {
            throw new ValidacaoException("Não é permitido alterar a especialidade do instrutor!");
        }

        Instrutor instrutor = buscarInstrutor(dados.id());
        instrutor.atualizarInformacoes(dados);
        Instrutor saved = repository.save(instrutor);
        return new DadosDetalhamentoInstrutor(saved);
    }

    @Transactional
    public void excluirInstrutor(Long id) {
        Instrutor instrutor = buscarInstrutor(id);
        instrutor.excluir();
        repository.save(instrutor);
    }

    private Instrutor buscarInstrutor(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new InstrutorNotFoundException("ID do instrutor informado não existe!"));
    }
}

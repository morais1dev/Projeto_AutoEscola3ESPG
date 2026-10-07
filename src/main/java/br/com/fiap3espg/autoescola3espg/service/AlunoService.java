package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.aluno.*;
import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoService {
    private final AlunoRepository repository;

    @Transactional
    public DadosDetalhamentoAluno cadastrarAluno(DadosCadastroAluno dados) {
        if (repository.existsByEmail(dados.email())) {
            throw new ValidacaoException("Já existe um aluno cadastrado com o e-mail informado!");
        }
        if (repository.existsByCpf(dados.cpf())) {
            throw new ValidacaoException("Já existe um aluno cadastrado com o CPF informado!");
        }
        Aluno aluno = new Aluno(dados);
        Aluno saved = repository.save(aluno);
        return new DadosDetalhamentoAluno(saved);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemAluno> listarAlunos(Pageable paginacao) {
        return repository
                .findAllByAtivoTrue(paginacao)
                .map(DadosListagemAluno::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoAluno detalharAluno(Long id) {
        return new DadosDetalhamentoAluno(buscarAluno(id));
    }

    @Transactional
    public DadosDetalhamentoAluno atualizarAluno(DadosAtualizacaoAluno dados) {
        //Regras de negócio: não permitir alteração de e-mail e CPF
        if (dados.email() != null) {
            throw new ValidacaoException("Não é permitido alterar o e-mail do aluno!");
        }
        if (dados.cpf() != null) {
            throw new ValidacaoException("Não é permitido alterar o CPF do aluno!");
        }

        Aluno aluno = buscarAluno(dados.id());
        aluno.atualizarInformacoes(dados);
        Aluno saved = repository.save(aluno);
        return new DadosDetalhamentoAluno(saved);
    }

    @Transactional
    public void excluirAluno(Long id) {
        //Exclusão lógica: o aluno passa a ser "inativo"
        Aluno aluno = buscarAluno(id);
        aluno.excluir();
        repository.save(aluno);
    }

    private Aluno buscarAluno(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new AlunoNotFoundException("ID do aluno informado não existe!"));
    }
}

package br.com.fiap3espg.autoescola3espg.service;

import br.com.fiap3espg.autoescola3espg.domain.instrucao.ValidacaoException;
import br.com.fiap3espg.autoescola3espg.domain.usuario.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DadosDetalhamentoUsuario cadastrarUsuario(DadosCadastroUsuario dados) {
        if (repository.existsByLogin(dados.login())) {
            throw new ValidacaoException("Já existe um usuário cadastrado com o login informado!");
        }
        //A senha é criptografada (BCrypt) antes de ser armazenada
        String senhaCriptografada = passwordEncoder.encode(dados.senha());
        Usuario usuario = new Usuario(dados.login(), senhaCriptografada, dados.perfil());
        Usuario saved = repository.save(usuario);
        return new DadosDetalhamentoUsuario(saved);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoUsuario> listarUsuarios(Pageable paginacao) {
        return repository
                .findAll(paginacao)
                .map(DadosDetalhamentoUsuario::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoUsuario detalharUsuario(Long id) {
        return new DadosDetalhamentoUsuario(buscarUsuario(id));
    }

    @Transactional
    public DadosDetalhamentoUsuario atualizarPerfil(Long id, DadosAtualizacaoPerfil dados, String loginUsuarioLogado) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getLogin().equals(loginUsuarioLogado)) {
            throw new ValidacaoException("Um administrador não pode alterar o próprio perfil!");
        }
        usuario.atualizarPerfil(dados.perfil());
        Usuario saved = repository.save(usuario);
        return new DadosDetalhamentoUsuario(saved);
    }

    @Transactional
    public void excluirUsuario(Long id, String loginUsuarioLogado) {
        Usuario usuario = buscarUsuario(id);
        if (usuario.getLogin().equals(loginUsuarioLogado)) {
            throw new ValidacaoException("Um administrador não pode excluir o próprio usuário!");
        }
        repository.delete(usuario);
    }

    @Transactional
    public void alterarPropriaSenha(String loginUsuarioLogado, DadosAlteracaoSenha dados) {
        Usuario usuario = repository.findUsuarioByLogin(loginUsuarioLogado)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário logado não encontrado!"));

        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getSenha())) {
            throw new ValidacaoException("Senha atual incorreta!");
        }
        if (dados.senhaAtual().equals(dados.novaSenha())) {
            throw new ValidacaoException("A nova senha deve ser diferente da senha atual!");
        }

        usuario.alterarSenha(passwordEncoder.encode(dados.novaSenha()));
        repository.save(usuario);
    }

    private Usuario buscarUsuario(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("ID do usuário informado não existe!"));
    }
}

package br.com.fiap3espg.autoescola3espg.domain.usuario;

/**
 * Dados de saída de um usuário. A senha NUNCA é devolvida pela API.
 */
public record DadosDetalhamentoUsuario(
        Long id,
        String login,
        Role perfil) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin(), usuario.getPerfil());
    }
}

-- Usuário administrador inicial (login: admin / senha: admin), com senha criptografada em BCrypt.
-- Só é inserido se ainda não existir um usuário com o login 'admin'.
insert into usuarios (login, senha, perfil)
select 'admin', '$2a$10$4L6EyatrtezLdJaOPSHFsu3fi16lXc5ZTrn/7G7agu.Ovm0d6JRAa', 'ADMIN'
from dual
where not exists (select 1 from (select login from usuarios) u where u.login = 'admin');

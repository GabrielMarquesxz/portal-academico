"""Gera hash BCrypt localmente; nao envie sua senha a sites geradores."""
import getpass
import bcrypt

senha = getpass.getpass('Nova senha (minimo de 12 caracteres): ')
if len(senha) < 12:
    raise SystemExit('Senha muito curta: escolha no minimo 12 caracteres.')
print('\nHash BCrypt para colar no SQL (nao e a senha original):')
print(bcrypt.hashpw(senha.encode('utf-8'), bcrypt.gensalt(rounds=12)).decode('ascii'))

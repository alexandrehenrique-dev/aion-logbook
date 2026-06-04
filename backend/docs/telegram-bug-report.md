# Telegram Bug Report

## 1. Visao geral

O endpoint de BugReport pode enviar notificacoes operacionais para o Telegram usando a Bot API. A integracao e opcional: o bug report sempre e persistido no banco antes da tentativa de envio, e o banco continua sendo a fonte de verdade.

Quando o Telegram esta desabilitado, o backend registra o bug report normalmente e retorna `telegramSent=false`. Quando esta habilitado, o backend tenta enviar uma mensagem para o chat configurado. Se o envio falhar, a falha e registrada de forma segura, `telegramSent=false` e a API continua retornando `201 Created`.

## 2. Variaveis de ambiente

```bash
TELEGRAM_ENABLED=true
TELEGRAM_BOT_TOKEN=123456:ABC...
TELEGRAM_CHAT_ID=-1001234567890
```

- `TELEGRAM_ENABLED`: liga ou desliga o envio de notificacoes para Telegram. O padrao do backend e `false`.
- `TELEGRAM_BOT_TOKEN`: token do bot criado no BotFather. Esse valor e secreto.
- `TELEGRAM_CHAT_ID`: ID do canal, grupo ou chat onde as mensagens serao recebidas.

Nunca versione o token real em arquivos do projeto, README, documentacao, logs ou exemplos commitados.

## 3. Como criar o bot no Telegram

1. Abra o Telegram.
2. Procure por `@BotFather`.
3. Inicie a conversa com o BotFather.
4. Envie `/newbot`.
5. Escolha o nome visivel do bot.
6. Escolha um username terminado em `bot`, por exemplo `aion_logbook_bug_bot`.
7. Copie o token retornado pelo BotFather.
8. Guarde o token com seguranca, preferencialmente em um gerenciador de segredos ou variavel de ambiente protegida.
9. Nunca commite o token.

## 4. Como criar canal ou grupo para receber mensagens

### Opcao A - Canal privado

1. Crie um canal privado no Telegram.
2. Adicione o bot como administrador do canal.
3. Garanta que o bot tenha permissao para postar mensagens.
4. Obtenha o `chat_id` do canal.

### Opcao B - Grupo privado

1. Crie um grupo privado no Telegram.
2. Adicione o bot ao grupo.
3. Se necessario, torne o bot administrador.
4. Obtenha o `chat_id` do grupo.

## 5. Como obter o chat_id

Uma forma pratica usando a Bot API:

1. Adicione o bot no grupo ou canal.
2. Envie uma mensagem no grupo ou canal.
3. Acesse no navegador, substituindo `<TELEGRAM_BOT_TOKEN>` pelo token do bot:

```txt
https://api.telegram.org/bot<TELEGRAM_BOT_TOKEN>/getUpdates
```

4. Localize o campo `chat.id` na resposta JSON.
5. Grupos e canais geralmente usam IDs negativos.
6. Para canais privados, o ID normalmente comeca com `-100`.

Alternativa: use bots auxiliares como `@RawDataBot` para inspecionar os dados do chat. Se usar essa opcao, remova o bot auxiliar do grupo ou canal depois de obter o ID.

## 6. Como configurar localmente

macOS/Linux:

```bash
export TELEGRAM_ENABLED=true
export TELEGRAM_BOT_TOKEN="token_aqui"
export TELEGRAM_CHAT_ID="-1001234567890"
```

Windows PowerShell:

```powershell
$env:TELEGRAM_ENABLED="true"
$env:TELEGRAM_BOT_TOKEN="token_aqui"
$env:TELEGRAM_CHAT_ID="-1001234567890"
```

Depois, suba o backend com o profile local usado no projeto. Exemplo:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 7. Como configurar em producao

Configure as variaveis no ambiente de deploy. Exemplos de locais possiveis:

- Docker Compose.
- Servidor onde a aplicacao roda.
- Pipeline de CI/CD.
- Variaveis de ambiente da plataforma cloud.

Nao coloque o token real em arquivos versionados. Use variaveis de ambiente ou o mecanismo de segredos disponivel no ambiente.

## 8. Como testar

Teste manual:

1. Suba o backend.
2. Autentique e obtenha um JWT valido.
3. Chame `POST /api/v1/bug-reports`.
4. Envie um payload como este:

```json
{
  "title": "Erro ao salvar direcao",
  "description": "Ao clicar no botao salvar direcao, nada acontece na interface.",
  "severity": "HIGH",
  "page": "/directions/new",
  "metadata": {
    "browser": "Chrome",
    "os": "macOS"
  }
}
```

5. Valide que a resposta HTTP e `201 Created`.
6. Valide que a mensagem chegou no Telegram.
7. Teste com `TELEGRAM_ENABLED=false` e confirme que a API continua retornando `201 Created` com `telegramSent=false`.
8. Teste com token invalido e confirme que a API continua retornando `201 Created`, sem expor token em log, response ou erro persistido.

Resposta esperada:

```json
{
  "id": "...",
  "status": "RECEIVED",
  "telegramSent": false,
  "message": "Bug report registrado com sucesso."
}
```

## 9. Seguranca

- Nunca commite token real.
- Nunca exponha token em log.
- Nunca coloque token no README ou em exemplos versionados.
- A metadata do bug report e sanitizada antes de persistir.
- O backend nao deve persistir ou enviar cookies, `localStorage`, `sessionStorage`, `authorization`, `token`, `password` ou secrets.
- O Telegram e apenas um canal operacional de notificacao.
- O banco de dados e a fonte de verdade do bug report.

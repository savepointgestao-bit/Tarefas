# Tarefas (app Android)

O app é uma casca que abre o próprio site (`index.html` da raiz deste repositório). O GitHub copia o site para dentro do app e gera o APK a cada alteração do site ou do app.

## Como pegar o APK

1. Aba **Actions**, execução mais recente de **Gerar APK**.
2. Em **Artifacts**, baixe **tarefas-apk** (um zip com o `Tarefas.apk`).
3. Abra o zip no celular e instale o `Tarefas.apk` (libere "instalar apps desconhecidos" se pedir).

Também dá para rodar na mão: Actions > Gerar APK > Run workflow.

## Assinatura

A chave de assinatura **não fica no repositório** (ele é público). Ela vem de 4 segredos do GitHub (Settings > Secrets and variables > Actions): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`. Com eles, todo APK sai com a mesma assinatura e instala por cima do anterior.

Sem os segredos o build ainda funciona, mas gera o artefato **tarefas-apk-chave-temporaria**, que não instala por cima de outro build.

## Celular

- Os dados ficam na nuvem, como no site. Os ajustes locais (layout do Início, quadro atual, avisos) ficam só no aparelho.
- Avisos de prazo viram notificação do celular enquanto o app estiver aberto.
- O botão voltar do Android fecha o que estiver aberto, volta ao Início e, no Início, sai do app.
- O tema "Automático" segue o tema do celular.

## Nome e pacote

O app se chama **Tarefas**. O identificador interno do pacote continua `com.savepoint.quadro` de propósito: assim a versão nova instala por cima da anterior, sem perder nada.

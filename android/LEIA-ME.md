# SBase (app Android)

O app é uma casca que abre o próprio site (`index.html` da raiz deste repositório). O GitHub copia o site para dentro do app e gera o APK a cada alteração do site ou do app.

## Como pegar o APK

1. Aba **Actions**, execução mais recente de **Gerar APK**.
2. Em **Artifacts**, baixe **sbase-apk** (um zip com o `SBase.apk`).
3. Abra o zip no celular e instale o `SBase.apk` (libere "instalar apps desconhecidos" se pedir).

Também dá para rodar na mão: Actions > Gerar APK > Run workflow.

## Assinatura

A chave de assinatura **não fica no repositório** (ele é público). Ela vem de 4 segredos do GitHub (Settings > Secrets and variables > Actions): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`. Com eles, todo APK sai com a mesma assinatura e instala por cima do anterior.

Sem os segredos o build ainda funciona, mas gera o artefato **sbase-apk-chave-temporaria**, que não instala por cima de outro build.

## Celular

- Os dados ficam na nuvem, como no site. Cartões, colunas, quadros, etiquetas, Finanças, missão e objetivos sincronizam com a equipe. Só o layout do Início, o quadro aberto, os modelos e os avisos ficam no aparelho.
- Avisos de prazo viram notificação do celular enquanto o app estiver aberto.
- O botão voltar do Android fecha o que estiver aberto, volta ao Início e, no Início, sai do app.
- O tema "Automático" segue o tema do celular.

## Nome e pacote

O app se chama **SBase**. O identificador interno do pacote continua `com.savepoint.quadro` de propósito: assim a versão nova instala por cima da anterior, sem perder nada.

## Ícone

O ícone é uma engrenagem com um S. No Android 13 ou mais novo, com "Ícones temáticos" ligado nas configurações do celular, ele ganha a cor do tema (camada `ic_mono`). Nas versões antigas aparece com as cores normais.

## Arquivos e backup

Anexos e a importação de backup abrem o seletor de arquivos do celular. Os anexos vão para a pasta privada `anexos` do Supabase (até 10 MB por arquivo, só a equipe vê). Vale baixar ou copiar um backup das Finanças de vez em quando.

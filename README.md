# 💰 Controle Financeiro - App Android Nativo

Aplicativo nativo Android para controle de finanças pessoais com captura automática de gastos via notificações bancárias (Nubank, Itaú, Bradesco, Inter, Santander, C6, etc.), projeção financeira para 6 meses, relatórios em PDF e arquitetura 100% offline e privada.

---

## 📱 Recursos Principais

1. **Dashboard, Gestão de Orçamento & Insights Inteligentes (Home)**
   - Resumo do mês: Receitas, Despesas e Saldo Líquido.
   - **Card de Insights Financeiros**: Avaliação automática da taxa de poupança, alertas de estouro de teto por categoria e recomendações personalizadas.
   - Barra de progresso animada por categoria de gasto com alertas de limite.
   - Histórico de transações com busca, filtros por categoria e **Snackbar com "Desfazer" (Undo)**.

2. **Captura Automática Inteligente de Notificações Bancárias**
   - Serviço em background (`NotificationListenerService`) para bancos brasileiros: **Nubank, Itaú, Bradesco, Santander, Banco Inter, C6 Bank, Caixa, PicPay, BTG Pactual, Mercado Pago, PagBank, Neon e Nomad**.
   - **Prevenção de Duplicidade**: Janela inteligente de 60 segundos que impede notificações duplicadas ou atualizadas pelo banco de duplicarem o lançamento.
   - **Reconhecimento de Parcelas & Cashback**: Detecção automática de compras parceladas (ex: `em 3x`), estornos e cashback.
   - Simulador integrado com 6 cenários bancários de teste em tempo real.

3. **Backup & Restauração Completa**
   - Exportação e importação de backup em arquivo `.json` pela tela de Configurações, permitindo migrar seus dados para novos celulares ou salvar no Google Drive sem depender de servidores remotos.

4. **Projeção Financeira de 6 Meses**
   - Gráfico interativo desenhado via Canvas com projeção de despesas e economia acumulada.
   - Gerenciador de despesas fixas recorrentes (aluguel, condomínio, internet, assinaturas).

5. **Relatórios e Exportação em PDF**
   - Criação local de relatórios em folha A4 com tabelas detalhadas e balanço mensal.
   - Compartilhamento nativo via e-mail e mensageiros.

6. **Segurança Avançada & Biometria**
   - Suporte a **Biometria Nativa (Impressão Digital e Reconhecimento Facial)** integrada com `BiometricPrompt`.
   - Bloqueio por PIN de 4 a 6 dígitos com feedback tátil (*Haptic Feedback*).
   - Todos os dados ficam salvos unicamente no banco SQLite/Room local do aparelho.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio** (Recomendado: Ladybug / Meerkat ou superior)
- **JDK 17 ou 21**
- **Android SDK** com Platform 34 a 36 instalado

### Passo a Passo

1. **Abrir no Android Studio**:
   - Abra o Android Studio e selecione **Open**.
   - Navegue até a pasta do projeto `Ruddgero12-main` e clique em **OK**.

2. **Sincronizar Gradle**:
   - O Android Studio detectará o arquivo `gradle/wrapper/gradle-wrapper.properties` e baixará o Gradle automaticamente.
   - Aguarde o término do processo **Gradle Sync**.

3. **Executar no Emulador ou Dispositivo Físico**:
   - Selecione um dispositivo (emulador com Android 8.0+ / API 26+ ou seu celular com depuração USB ativada).
   - Clique no botão verde de **Run (Play)** (`Shift + F10`).

---

## ⚙️ Configuração Importante no Primeiro Uso

1. **Acesso às Notificações**:
   - Abra a aba **Bancos** no menu inferior do app.
   - Toque em **"Conceder Acesso às Notificações"** e habilite o **Controle Financeiro** nas configurações do Android.
   - Use o **Simulador de Notificações** para testar as compras de teste em tempo real.


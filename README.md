# Sistema de Agendamento de Transferências Financeiras

Este projeto é um monorepo que contém uma aplicação completa para agendamento de transferências financeiras, desenvolvida com Java Spring Boot no backend e Vue.js com Vite no frontend.

## 🏗️ Arquitetura

O projeto foi estruturado como um monorepo usando **Turborepo** para gerenciar múltiplas aplicações:

```
├── apps/
│   ├── backend/          # API Spring Boot (Java 11)
│   └── frontend/         # Vue.js com Vite
├── package.json          # Configuração do workspace raiz
└── turbo.json           # Configuração do Turborepo
```

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 11** - Linguagem principal
- **Spring Boot 2.7.14** - Framework web
- **Spring Data JPA** - Persistência de dados
- **H2 Database** - Banco de dados em memória
- **Maven** - Gerenciamento de dependências
- **Bean Validation** - Validação de dados

### Frontend
- **Vue 3.5** - Framework JavaScript reativo
- **Vite 7** - Build tool e dev server
- **shadcn-vue** - Componentes UI modernos
- **Tailwind CSS v4** - Framework CSS utilitário de próxima geração
- **TanStack Query (@tanstack/vue-query)** - Gerenciamento de estado do servidor
- **Vue-sonner** - Sistema de notificações toast
- **vee-validate + zod** - Validação de formulários type-safe

### DevOps e Ferramentas
- **Turborepo 2.3** - Monorepo build system de alta performance

## 📋 Funcionalidades

### ✅ Implementadas
1. **Agendamento de Transferências**
   - Validação de contas (10 dígitos)
   - Cálculo automático de taxas
   - Validação de datas
   - Persistência no banco H2

2. **Cálculo de Taxas**
   - Mesmo dia: R$ 3,00 + 2,5%
   - 1-10 dias: R$ 12,00
   - 11-20 dias: 8,2%
   - 21-30 dias: 6,9%
   - 31-40 dias: 4,7%
   - 41-50 dias: 1,7%
   - >50 dias: Erro (sem taxa aplicável)

3. **Extrato de Transferências**
   - Listagem de todas as transferências
   - Ordenação por data de agendamento
   - Resumo com totais
   - Formatação de dados

4. **Interface Moderna**
   - Design responsivo com shadcn-vue
   - Componentes acessíveis e reutilizáveis
   - Feedback visual em tempo real com vue-sonner
   - Navegação intuitiva com loading states
   - Validação de formulários com vee-validate + zod
   - Cache inteligente com TanStack Query

## 🛠️ Instalação e Execução

### Pré-requisitos
- **Node.js** 24+ e npm 10+
- **Java 11**
- **Maven 3.6+**
- **Git**

### 1. Clone o repositório
```bash
git clone https://github.com/Joabesv/wayon-challenge
cd wayon-challenge
```

### 2. Instale as dependências do workspace raiz
```bash
npm install
```

### 4. Execute em modo desenvolvimento

#### Opção A: Executar tudo com Turborepo
```bash
npm run dev
```

#### Opção B: Executar separadamente

**Backend (Terminal 1):**
```bash
npm run backend:dev
# ou
cd apps/backend
mvn spring-boot:run
```

**Frontend (Terminal 2):**
```bash
npm run frontend:dev
# ou
cd apps/frontend
npm run dev
```

### 5. Acesse as aplicações
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: `password`

## 🏗️ Build para Produção

```bash
# Build completo
npm run build

# Build apenas backend
cd apps/backend
mvn clean package 

# Build apenas frontend
cd apps/frontend
npm run build
```

## 🧪 Testes

```bash
# Executar todos os testes
npm run test

# Testes do backend
cd apps/backend
mvn test

# Linting do frontend
cd apps/frontend
npm run lint
```

## 📊 Estrutura do Banco de Dados

### Tabela: financial_transfers
id BigInt primaryKey
source_account VARCHAR(10) conta de origem
destination_account VARCHAR(10) conta destino
transfer_amount DECIMAL(15, 2) Valor da transferencia
fee DECIMAL(15, 2) Taxa calculada
transfer_date DATE Data da transferencia
schedule_date TIMESTAMP Data/hora do agendamento

## 🎯 Decisões Arquiteturais

### 1. Monorepo com Turborepo
- **Razão**: Facilita o desenvolvimento e deployment conjunto
- **Benefícios**: Cache compartilhado, builds otimizados, gestão unificada

### 2. Spring Boot com Arquitetura em Camadas
- **Controller**: Endpoints REST com validação
- **Service**: Lógica de negócio
- **Repository**: Acesso a dados com Spring Data JPA
- **DTO**: Transferência de dados entre camadas


### 4. shadcn-vue
- **Razão**: Componentes modernos e customizáveis
- **Acessibilidade**: Primitivos baseados em Reka UI
- **Design System**: Consistência visual

### 6. TanStack Query + vue-sonner + vee-validate
- **Razão**: Stack moderna para gerenciamento de estado e UX
- **TanStack Query**: Cache, sincronização e loading states automáticos
- **vue-sonner**: Feedback visual elegante e não intrusivo
- **vee-validate + zod**: Validação type-safe e DX aprimorado

## 🔄 Fluxo de Dados

1. **Frontend** → Formulário de agendamento
2. **API Call** → POST `/api/transfers`
3. **Controller** → Validação dos dados
4. **Service** → Cálculo da taxa
5. **Repository** → Persistência no H2
6. **Response** → Confirmação para o frontend
7. **Redirect** → Lista de transferências

## 🚦 API Endpoints

### POST `/api/transfers`
Agenda uma nova transferência
```json
{
  "sourceAccount": "1234567890",
  "destinationAccount": "0987654321",
  "transferAmount": 1000.00,
  "transferDate": "2024-08-30"
}
```

### GET `/api/transfers`
Lista todas as transferências ordenadas por data de agendamento

### POST `/api/transfers/calculate-fee`
Calcula a taxa para uma transferência
```json
{
  "transferAmount": 1000.00,
  "transferDate": "2024-08-30"
}
```

## 🔧 Configurações de Desenvolvimento
Necessário uso da ferramenta [JQ](https://github.com/jqlang/jq) para rodar a api,
permite a melhor visualização de logs estruturados.

## 📝 Próximos Passos

- [ ] Implementar testes unitários
- [ ] Adicionar autenticação/autorização
- [ ] Dockerização das aplicações
- [ ] Pipeline CI/CD
- [ ] Métricas e monitoramento

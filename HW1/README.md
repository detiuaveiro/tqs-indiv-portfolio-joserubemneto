# ZeroMonos - Waste Collection System

Sistema de gestão de recolha de resíduos volumosos desenvolvido para o mid-term assignment de Teste e Qualidade de Software.

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.4.0**
- **Spring Data JPA**
- **H2 Database** (in-memory)
- **Spring WebFlux** (para integração com API externa)
- **Lombok**
- **JUnit 5 & Mockito** (testes unitários)
- **REST-Assured** (testes de integração)
- **WireMock** (mock de API externa)
- **Cucumber** (BDD tests)
- **Selenium WebDriver** (functional tests)
- **Gatling** (performance tests)
- **JaCoCo** (code coverage)
- **SLF4J + Logback** (structured logging)

### Frontend
- **React 18**
- **React Router DOM** (navegação)
- **Axios** (HTTP client)
- **CSS3** (estilização)
- **Vite** (build tool)

### Integração Externa
- **GeoAPI.pt** - API para obter lista de municípios portugueses

## 📋 Funcionalidades

### Para Cidadãos
- ✅ Criar pedido de recolha sem registo obrigatório
- ✅ Selecionar município de uma lista fechada (API externa)
- ✅ Escolher data e horário preferencial
- ✅ Receber token único para consulta
- ✅ Consultar estado do pedido com o token
- ✅ Cancelar pedido (se ainda não estiver completo)

### Para Staff (Gestão)
- ✅ Dashboard com estatísticas em tempo real
- ✅ Visualizar todos os pedidos
- ✅ Filtrar por município e estado
- ✅ Atualizar estado dos pedidos
- ✅ Adicionar notas às mudanças de estado
- ✅ Visualização em cards com informação completa

### Regras de Negócio
- ✅ Data de recolha deve ser no futuro
- ✅ Máximo de 10 pedidos ativos por município por dia
- ✅ Validação de transições de estado
- ✅ Histórico completo de mudanças de estado com timestamps

### Estados do Pedido
1. **RECEIVED** - Pedido recebido
2. **ASSIGNED** - Atribuído a equipa
3. **IN_PROGRESS** - Recolha em progresso
4. **COMPLETED** - Concluído
5. **CANCELLED** - Cancelado

## 🏗️ Estrutura do Projeto

```
HW1/
├── backend/                 # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/zeremonos/wastecollection/
│   │   │   │   ├── config/          # WebClient configuration
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── exception/       # Exception handling
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Spring Data repositories
│   │   │   │   └── service/         # Business logic
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                    # Unit & Integration tests
│   └── pom.xml
├── frontend/                # React application
│   ├── src/
│   │   ├── components/      # React components
│   │   │   ├── Home.jsx
│   │   │   ├── CreateRequest.jsx
│   │   │   └── CheckRequest.jsx
│   │   ├── services/        # API integration
│   │   │   └── api.js
│   │   ├── App.jsx
│   │   └── main.jsx
│   └── package.json
└── docs/                    # Documentation
    └── API.md              # REST API documentation
```

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Node.js 16+
- Maven 3.6+

### Backend

```bash
cd HW1/backend

# Executar testes
./mvnw test

# Iniciar aplicação
./mvnw spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

### Frontend

```bash
cd HW1/frontend

# Instalar dependências
npm install

# Iniciar aplicação
npm run dev
```

O frontend estará disponível em: `http://localhost:5173`

## 🧪 Testes

### Backend

```bash
cd HW1/backend

# Executar todos os testes
./mvnw test

# Executar apenas testes unitários
./mvnw test -Dtest='!*IT,!CucumberTest'

# Executar apenas testes de integração
./mvnw test -Dtest='*IT'

# Executar apenas testes BDD
./mvnw test -Dtest=CucumberTest

# Executar com relatório de cobertura
./mvnw verify

# Ver relatório JaCoCo
open target/site/jacoco/index.html
```

### Testes BDD/Funcionais (Cucumber + Selenium)

```bash
# Opção 1: Script automatizado (recomendado)
./run-bdd-tests.sh test

# Opção 2: Manual (requer backend e frontend running)
# Terminal 1: Start backend
cd backend && ./mvnw spring-boot:run

# Terminal 2: Start frontend
cd frontend && npm run dev

# Terminal 3: Run BDD tests
cd backend && ./mvnw test -Dtest=CucumberTest
```

**Pré-requisitos para BDD tests:**
- ChromeDriver instalado (brew install chromedriver)
- Backend running em http://localhost:8080
- Frontend running em http://localhost:5173

Ver guia completo: [docs/BDD_TESTING.md](docs/BDD_TESTING.md)

### Performance Tests (Gatling)

```bash
# Executar smoke test (rápido)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.BasicSmokeTestSimulation

# Executar load test (5 minutos)
./mvnw gatling:test -Dgatling.simulationClass=com.zeremonos.wastecollection.performance.LoadTestSimulation

# Executar todos os testes de performance
./mvnw gatling:test

# Ver relatório HTML
open target/gatling/*/index.html
```

**Testes disponíveis:**
- ✅ **Smoke Test**: Verificação rápida (30s, 23 users)
- ✅ **Load Test**: Carga realista (5 min, 50-150 users)
- ✅ **Stress Test**: Carga extrema (3 min, até 200+ users/sec)
- ✅ **Spike Test**: Picos súbitos (45s, spikes de 100-150 users)
- ✅ **Endurance Test**: Estabilidade prolongada (30 min, 20 users constantes)

Ver guia completo: [docs/PERFORMANCE_TESTING.md](docs/PERFORMANCE_TESTING.md)

## 📝 Logging

Sistema de logging estruturado com **SLF4J + Logback**:

### Tipos de Logs

1. **Application Logs** (`logs/application.log`)
   - Todos os eventos da aplicação
   - Rotação diária, max 10MB por ficheiro
   - Retenção: 30 dias

2. **Error Logs** (`logs/error.log`)
   - Apenas erros (ERROR level)
   - Retenção: 90 dias
   - Crítico para troubleshooting

3. **Performance Logs** (`logs/performance.log`)
   - Tempos de execução de métodos
   - Formato CSV para análise
   - Retenção: 7 dias

4. **Audit Logs** (`logs/audit.log`)
   - Operações críticas (criar, cancelar, atualizar)
   - Trail de auditoria imutável
   - Retenção: 365 dias

### Features Implementadas

- ✅ **AOP Logging Aspect**: Logging automático de controllers e services
- ✅ **Audit Aspect**: Auditoria de operações de negócio
- ✅ **HTTP Interceptor**: Logging de requests/responses com request ID único
- ✅ **MDC**: Request tracking com IDs únicos
- ✅ **Async Appenders**: Performance otimizada
- ✅ **Log Rotation**: Gestão automática de espaço
- ✅ **Colored Console**: Output colorido para desenvolvimento
- ✅ **Structured Logs**: Formato parseável para análise

### Consultar Logs

```bash
# Ver logs em tempo real
tail -f logs/application.log

# Ver apenas erros
tail -f logs/error.log

# Ver métricas de performance
tail -f logs/performance.log

# Ver audit trail
tail -f logs/audit.log

# Procurar por request ID específico
grep "a1b2c3d4" logs/application.log

# Analisar operações lentas
grep "SLOW" logs/application.log
```

Ver guia completo: [docs/LOGGING.md](docs/LOGGING.md)

### Cobertura de Testes
- ✅ **121 testes unitários e de integração** implementados
  - **33 testes** de validação de modelo (Bean Validation)
  - **24 testes** de serviço (regras de negócio)
  - **36 testes** de integração REST (MockMvc)
  - **9 testes** REST-Assured (full API flow)
  - **19 testes** de repository e models
- ✅ **22 cenários BDD** (Cucumber + Selenium)
  - **11 cenários** de fluxos de cidadão
  - **11 cenários** de gestão staff
- ✅ **5 simulações de performance** (Gatling)
  - **Smoke Test**: Verificação básica
  - **Load Test**: Carga realista
  - **Stress Test**: Carga extrema
  - **Spike Test**: Picos de tráfego
  - **Endurance Test**: Estabilidade prolongada
- ✅ Testes unitários (models, services)
- ✅ Testes de integração (repositories, controllers)
- ✅ Testes funcionais end-to-end (BDD)
- ✅ Testes de performance e carga (Gatling)
- ✅ Testes com WireMock (API externa)

## 📡 Endpoints da API

### Cidadãos

#### Criar Pedido
```http
POST /api/requests
Content-Type: application/json

{
  "municipalityCode": "LISB01",
  "municipalityName": "Lisboa",
  "citizenName": "João Silva",
  "citizenEmail": "joao@example.com",
  "citizenPhone": "912345678",
  "pickupAddress": "Rua Example, 123",
  "itemDescription": "Old refrigerator and washing machine",
  "preferredDate": "2025-11-15",
  "preferredTimeSlot": "MORNING"
}
```

#### Consultar Pedido
```http
GET /api/requests/{token}
```

#### Cancelar Pedido
```http
DELETE /api/requests/{token}
```

### Staff (Gestão)

#### Listar Todos os Pedidos
```http
GET /api/staff/requests?municipality=Lisboa
```

#### Atualizar Estado
```http
PUT /api/staff/requests/{id}/status
Content-Type: application/json

{
  "newStatus": "ASSIGNED",
  "notes": "Assigned to team A"
}
```

### Municípios

#### Obter Lista de Municípios
```http
GET /api/municipalities
```

Ver documentação completa em: [docs/API.md](docs/API.md)

## 👥 Staff Interface

Para aceder à interface de gestão: **http://localhost:5173/staff**

### Funcionalidades
- Dashboard com estatísticas (Total, Received, Assigned, In Progress, Completed, Cancelled)
- Lista de todos os pedidos em cards
- Filtros por município e estado
- Atualização de estado com regras de transição
- Adição de notas em cada mudança de estado
- Interface responsiva e intuitiva

Ver guia completo: [docs/STAFF_GUIDE.md](docs/STAFF_GUIDE.md)

## 🎭 Testes BDD (Behavior-Driven Development)

O projeto inclui testes funcionais completos usando **Cucumber** para BDD e **Selenium WebDriver** para automação de browser.

### Cenários de Teste

#### Citizen Service Requests (11 cenários)
- ✅ **@smoke**: Visualizar home page
- ✅ **@critical**: Criar pedido válido com todos os campos
- ✅ **@critical**: Criar, salvar token e consultar pedido
- ✅ **@critical**: Cancelar pedido pendente
- ✅ Criar pedido para município específico
- ✅ **@validation**: Campos obrigatórios vazios
- ✅ **@validation**: Email inválido
- ✅ **@validation**: Telefone inválido
- ✅ Consultar com token inválido
- ✅ **Scenario Outline**: Múltiplos municípios (3 exemplos)

#### Staff Dashboard Management (11 cenários)
- ✅ **@smoke**: Visualizar dashboard e estatísticas
- ✅ **@critical**: Visualizar modal de atualização de estado
- ✅ **@critical**: Atualizar estado para ASSIGNED
- ✅ Filtrar por município
- ✅ Filtrar por estado
- ✅ Aplicar múltiplos filtros simultaneamente
- ✅ **@workflow**: Workflow completo (RECEIVED → ASSIGNED → IN_PROGRESS → COMPLETED)
- ✅ Visualizar estatísticas do dashboard
- ✅ **Scenario Outline**: Múltiplas atualizações de estado (3 exemplos)
- ✅ **@validation**: Atualizar sem notas
- ✅ Refresh do dashboard

### Arquitetura dos Testes

**Feature Files:**
- `citizen_service_requests.feature` - 11 cenários de fluxos cidadão
- `staff_dashboard.feature` - 11 cenários de gestão staff

**Step Definitions:**
- `CitizenSteps.java` - Steps para fluxos de cidadãos
- `StaffSteps.java` - Steps para fluxos de staff
- `SpringContextSteps.java` - Gestão do contexto Spring
- `Hooks.java` - Setup/teardown e screenshots on failure

**Configuration:**
- `WebDriverConfig.java` - Configuração Selenium (headless Chrome)
- `CucumberTestRunner.java` - Runner principal com tags e reports

### Tags Disponíveis
- **@smoke**: Testes básicos de verificação
- **@critical**: Funcionalidades críticas do sistema
- **@validation**: Testes de validação de dados
- **@workflow**: Fluxos completos end-to-end

### Executar Testes BDD

```bash
# Opção 1: Executar todos os testes BDD
cd HW1/backend
./mvnw test -Dtest=CucumberTestRunner

# Opção 2: Executar apenas testes críticos
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@critical"

# Opção 3: Executar apenas smoke tests
./mvnw test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@smoke"

# Ver relatório HTML
open target/cucumber-reports/cucumber.html
```

**⚠️ Importante**: Backend e Frontend devem estar rodando antes de executar os testes BDD!

Ver documentação completa: [docs/BDD_TESTING.md](docs/BDD_TESTING.md)

## 🗃️ Base de Dados

### H2 Console
Aceder à consola H2 em: `http://localhost:8080/h2-console`

**Configuração:**
- JDBC URL: `jdbc:h2:mem:wastecollectiondb`
- Username: `sa`
- Password: _(deixar vazio)_

### Tabelas Principais
- `service_requests` - Pedidos de recolha
- `status_history` - Histórico de estados

## ⚙️ Configurações

### application.properties
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:wastecollectiondb
spring.h2.console.enabled=true

# External API
geoapi.base-url=https://json.geoapi.pt

# Business Rules
app.max-requests-per-municipality-per-day=10
```

## 📊 Qualidade de Código

### SonarCloud
O projeto está configurado para análise no SonarCloud:

```bash
# Executar análise local
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=your-project-key \
  -Dsonar.organization=your-org \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=your-token
```

## 🎨 Interface do Utilizador

### Páginas Disponíveis

1. **Home** (`/`) - Página inicial com informações e links
2. **Criar Pedido** (`/create`) - Formulário de criação de pedido
3. **Consultar Pedido** (`/check`) - Consulta por token com histórico completo
4. **Staff Dashboard** (`/staff`) - Painel de gestão para staff

### Características da UI
- ✨ Design moderno e responsivo
- 🎯 Formulários com validação
- 📊 Timeline visual do histórico de estados
- 📈 Dashboard com estatísticas em tempo real
- 🎨 Feedback visual para ações
- 🔄 Filtros dinâmicos (município e estado)
- 📱 Mobile-friendly

## 🔒 Validações

### Backend
- Campos obrigatórios
- Email válido
- Telefone com 9 dígitos
- Data no futuro
- Descrição entre 10-500 caracteres
- Limite diário por município

### Frontend
- Validação HTML5
- Feedback imediato de erros
- Mensagens claras de validação

## 📝 Notas de Desenvolvimento

### Decisões Técnicas
1. **H2 in-memory** para facilitar testes e desenvolvimento
2. **WebClient** para chamadas assíncronas à GeoAPI.pt
3. **Cache** para lista de municípios (reduzir chamadas API)
4. **Exception handling** centralizado com @RestControllerAdvice
5. **DTOs** separados para requests e responses
6. **Status History** com timestamps para auditoria completa

### Melhorias Futuras
- [ ] Autenticação para staff
- [ ] Paginação na listagem de pedidos
- [ ] Filtros avançados
- [ ] Notificações por email
- [ ] Upload de fotos dos itens
- [ ] Dashboard com estatísticas

## 👥 Autor

José Rubem Neto - Universidade de Aveiro - TQS 2024/2025

## 📄 Licença

Este projeto foi desenvolvido para fins académicos.

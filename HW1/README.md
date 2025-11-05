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
- **JaCoCo** (code coverage)

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

# Executar com relatório de cobertura
./mvnw verify

# Ver relatório JaCoCo
open target/site/jacoco/index.html
```

### Cobertura de Testes
- ✅ **38 testes** implementados
- ✅ Testes unitários (models, services)
- ✅ Testes de integração (repositories)
- ✅ Testes REST-Assured (controllers)
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

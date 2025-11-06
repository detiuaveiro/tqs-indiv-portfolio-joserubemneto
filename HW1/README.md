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

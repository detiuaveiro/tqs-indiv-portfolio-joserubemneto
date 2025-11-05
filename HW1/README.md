# ZeroMonos Waste Collection System

Sistema de agendamento de recolha de resíduos volumosos para múltiplos municípios.

## Descrição

O ZeroMonos fornece serviços de recolha de lixo para múltiplos municípios e está a implementar um sistema para permitir que os cidadãos agendem a recolha de resíduos volumosos (colchões, eletrodomésticos antigos, etc.). Para melhor satisfação do cliente, a empresa quer um portal web e uma aplicação móvel para permitir que os cidadãos façam auto-agendamento de recolha de itens, na sua conveniência.

## Tecnologias Utilizadas

### Backend
- **Java 17**
- **Spring Boot 3.4.0**
  - Spring Web (REST API)
  - Spring Data JPA
  - Spring WebFlux (WebClient para API externa)
  - Spring Validation
- **H2 Database** (in-memory)
- **Lombok**
- **Maven**

### Frontend
- **React** (com Vite)
- **Axios** (chamadas HTTP)
- **React Router** (navegação)

### API Externa
- **GeoAPI.pt** - fornecimento de lista de municípios portugueses

### Testes
- **JUnit 5** - Unit tests
- **Mockito** - Mocking
- **WireMock/MockWebServer** - Mock de API externa
- **MockMvc** - Integration tests
- **REST-Assured** - API testing
- **Cucumber + Selenium WebDriver** - BDD e testes funcionais
- **JMeter/Gatling** - Performance tests

### Qualidade
- **SonarCloud** - Análise de código e métricas de qualidade

## Estrutura do Projeto

```
HW1/
├── backend/              # API REST Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/zeremonos/wastecollection/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   └── config/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/             # Interface React
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── pages/
│   │   └── utils/
│   └── package.json
├── docs/                 # Documentação e vídeo
└── performance-tests/    # Testes de performance
```

## Como Executar

### Backend

1. Navegar para a pasta backend:
```bash
cd HW1/backend
```

2. Executar com Maven:
```bash
./mvnw spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

Console H2: `http://localhost:8080/h2-console`

### Frontend

1. Navegar para a pasta frontend:
```bash
cd HW1/frontend
```

2. Instalar dependências (se ainda não instaladas):
```bash
npm install
```

3. Executar em modo de desenvolvimento:
```bash
npm run dev
```

O frontend estará disponível em: `http://localhost:5173`

## Funcionalidades

### Para Cidadãos
- Criar pedido de recolha de resíduos volumosos
- Selecionar município de uma lista
- Escolher data e horário preferencial
- Receber token de acesso após agendamento
- Consultar detalhes da reserva com token
- Cancelar reserva (opcional)
- Ver histórico de estados da reserva

### Para Staff
- Visualizar todos os pedidos de recolha
- Filtrar pedidos por município
- Atualizar estado dos pedidos
- Ver histórico de mudanças de estado

## API Endpoints

### Cidadãos
- `POST /api/requests` - Criar nova reserva
- `GET /api/requests/{token}` - Consultar reserva por token
- `DELETE /api/requests/{token}` - Cancelar reserva

### Staff
- `GET /api/staff/requests` - Listar todas as reservas
- `GET /api/staff/requests?municipality={name}` - Filtrar por município
- `PUT /api/staff/requests/{id}/status` - Atualizar status

### Auxiliares
- `GET /api/municipalities` - Obter lista de municípios

## Testes

### Executar todos os testes (backend)
```bash
cd HW1/backend
./mvnw test
```

### Executar testes de integração
```bash
./mvnw verify
```

### Análise SonarCloud
```bash
./mvnw clean verify sonar:sonar -Dsonar.token=<TOKEN>
```

## Autores

- José Rubem Neto

## Disciplina

TQS - Teste e Qualidade de Software
Universidade de Aveiro


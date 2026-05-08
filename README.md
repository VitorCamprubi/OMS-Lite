# OMS-Lite

Pequeno **Order Management System (OMS)** para estudo de backend Java, usando **Spring Boot 4 + Java 24 + MySQL**.

A ideia é ter uma API simples, mas com conceitos de “mundo real”:

- Cadastro de **clientes** e **produtos**
- Criação de **pedidos** com **itens**, total calculado e baixa de estoque
- Organização em **camadas** (controller, service, repository, domain)
- Uso de **JPA/Hibernate** e **MySQL** com `spring-boot-starter-data-jpa`

---

## 🧠 Objetivo do projeto

O OMS-Lite foi criado para:

- Praticar **Java / Spring Boot** com foco em backend
- Simular um fluxo básico de **pedido de venda**
- Servir como projeto de **portfólio** para vagas de desenvolvedor Java backend

Não é um sistema pronto para produção, e sim uma base didática para evoluir.

---

## 🛠 Stack utilizada

- **Linguagem:** Java 24
- **Framework:** Spring Boot 4.0.1
  - spring-boot-starter-web  
  - spring-boot-starter-data-jpa  
  - spring-boot-starter-validation
- **Banco de dados:** MySQL 8.0.x
- **Pool de conexões:** HikariCP
- **ORM:** Hibernate 7.2
- **Build:** Maven
- **IDE:** IntelliJ IDEA

---

## ✅ Funcionalidades atuais

### Produtos

- [x] Cadastrar produto
- [x] Listar todos os produtos
- [x] Campos:
  - `id`
  - `name`
  - `sku`
  - `unitPrice`
  - `stockQuantity`

### Clientes

- [x] Cadastrar cliente
- [x] Listar clientes
- [x] Campos:
  - `id`
  - `name`
  - `document` (CPF/CNPJ)
  - `email`

### Pedidos

- [x] Criar pedido confirmado para um cliente
- [x] Adicionar múltiplos itens
- [x] Calcular total automaticamente
- [x] Baixar estoque dos produtos
- [x] Status do pedido:
  - `CONFIRMED`
  - `CANCELLED` (já previsto no enum para uso futuro)

---

## 🧩 Modelagem de domínio (resumida)

Entidades principais:

- **Customer**
- **Product**
- **Order**
- **OrderItem**
- **OrderStatus** (enum: `CONFIRMED`, `CANCELLED`)

Relações:

- `Customer 1:N Order`
- `Order 1:N OrderItem`
- `Product 1:N OrderItem`

---

## 🚀 Como rodar o projeto

### 1. Pré-requisitos

- Java 24 instalado (`java -version`)
- Maven instalado (`mvn -version`)
- MySQL 8 rodando localmente

### 2. Criar banco de dados

No MySQL:

```sql
CREATE DATABASE oms_lite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
````

### 3. Configurar credenciais do banco

No arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oms_lite?useSSL=false&serverTimezone=America/Sao_Paulo
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Ajuste `username` e `password` para o seu ambiente.

### 4. Rodar a aplicação

Via Maven:

```bash
mvn spring-boot:run
```

Ou pela IDE (IntelliJ), rodando a classe:

```text
com.vitorcamprubi.OMS_Lite.OmsLiteApplication
```

A API sobe em:

```text
http://localhost:8080
```

---

## 📡 Endpoints principais

### Health check

```http
GET /api/ping
```

**Resposta:**

```json
"pong"
```

---

### Produtos

#### 1. Listar produtos

```http
GET /api/products
```

**Exemplo de resposta:**

```json
[
  {
    "id": 1,
    "name": "Teclado Mecânico",
    "sku": "TEC-001",
    "unitPrice": 250.00,
    "stockQuantity": 10
  }
]
```

#### 2. Criar produto

```http
POST /api/products
Content-Type: application/json
```

**Body:**

```json
{
  "name": "Teclado Mecânico",
  "sku": "TEC-001",
  "unitPrice": 250.00,
  "stockQuantity": 10
}
```

---

### Clientes

#### 1. Listar clientes

```http
GET /api/customers
```

#### 2. Criar cliente

```http
POST /api/customers
Content-Type: application/json
```

**Body:**

```json
{
  "name": "João da Silva",
  "document": "12345678900",
  "email": "joao@example.com"
}
```

---

### Pedidos

#### Criar pedido confirmado

```http
POST /api/orders
Content-Type: application/json
```

**Body:**

```json
{
  "customerId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

**Comportamento do backend:**

1. Busca o **cliente** pelo `customerId`
2. Para cada item:

   * Busca o **produto**
   * Verifica se há **estoque suficiente**
   * Baixa o estoque do produto
   * Cria o **OrderItem** com:

     * `unitPrice` = preço atual do produto
     * `totalPrice` = `unitPrice * quantity`
3. Soma o **total** do pedido
4. Define `status = CONFIRMED`
5. Salva o **Order** (com seus `OrderItem`s) no banco

**Exemplo de resposta (resumido):**

```json
{
  "id": 4,
  "createdAt": "2026-01-27T20:26:43.347899",
  "status": "CONFIRMED",
  "totalAmount": 750.00,
  "customer": {
    "id": 1,
    "name": "João da Silva",
    "document": "12345678900",
    "email": "joao@example.com"
  },
  "orderItems": [
    {
      "id": 1,
      "product": {
        "id": 1,
        "name": "Teclado Mecânico",
        "sku": "TEC-001"
      },
      "quantity": 2,
      "unitPrice": 250.00,
      "totalPrice": 500.00
    }
  ]
}
```

---

## 🧮 Fluxo interno da criação de pedido (OrderService)

A lógica principal está em `OrderService#createConfirmedOrder`:

1. **Recebe** `customerId` e a lista de itens (`productId`, `quantity`)
2. **Carrega o cliente** do banco
3. **Instancia o Order**:

   * `customer`
   * `createdAt = now()`
   * `status = CONFIRMED`
   * `totalAmount = 0`
4. Para cada item:

   * **Carrega o Product**
   * **Valida estoque** (`stockQuantity >= quantity`)
   * **Atualiza estoque** (`stockQuantity -= quantity`)
   * Cria `OrderItem` com:

     * `order`
     * `product`
     * `quantity`
     * `unitPrice` (do produto)
     * `totalPrice = unitPrice * quantity`
   * Adiciona o `OrderItem` à lista do `Order`
   * Soma no `totalAmount`
5. **Salva** o `Order` usando `orderRepository.save(order)`
6. Retorna o pedido persistido

Isso mostra na prática:

* Uso de **service** para concentrar regra de negócio
* Uso de **repositories** apenas para acesso a dados
* Uso de **entidades JPA** com relacionamentos

---

## 🧱 Estrutura de pacotes

Pacotes principais:

```text
src/main/java/com/vitorcamprubi/OMS_Lite
├── api
│   ├── PingController
│   ├── ProductController
│   ├── CustomerController
│   └── OrderController
│
├── domain
│   ├── Customer
│   ├── Product
│   ├── Order
│   ├── OrderItem
│   └── OrderStatus (enum)
│
├── repository
│   ├── CustomerRepository
│   ├── ProductRepository
│   └── OrderRepository
│
└── service
    └── OrderService
```

---

## 🔮 Próximos passos / ideias de evolução

Algumas ideias para futuras melhorias:

* [ ] Paginação e filtros em listagens (`/products`, `/customers`, `/orders`)
* [ ] Validações mais completas com Bean Validation (ex.: `@Email`, `@NotBlank`, etc.)
* [ ] Endpoint para cancelar pedido (`CANCELLED`) com regra de devolução de estoque
* [ ] Autenticação e autorização (Spring Security / JWT)
* [ ] Documentação da API com **OpenAPI/Swagger**
* [ ] Testes automatizados (unitários e de integração)

---

## 👤 Autor

**Vitor Camprubi**
🔗 GitHub: [@VitorCamprubi](https://github.com/VitorCamprubi)

Projeto criado para estudo e portfólio como desenvolvedor **Java Backend**.

---

## 📄 Licença

Projeto de estudo, sem licença formal definida ainda.
Sinta-se à vontade para clonar e brincar com o código.

```
::contentReference[oaicite:0]{index=0}
```

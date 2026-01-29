# OMS-Lite 🧾

**OMS-Lite (Order Management System Lite)** é uma API REST em **Java + Spring Boot** para gestão simples de **clientes, produtos e pedidos**.

O objetivo do projeto é simular o backend de um sistema de vendas/estoque:

- Cadastro de clientes e produtos  
- Criação de pedidos com múltiplos itens  
- Baixa automática de estoque  
- Cálculo do total do pedido  
- Status do pedido (confirmado / cancelado)

> Projeto desenvolvido como portfólio para prática de **Java, Spring Boot, JPA/Hibernate e modelagem de domínio**.

---

## 🏗️ Stack Tecnológica

- **Linguagem:** Java 24  
- **Framework:** Spring Boot 4.0.1  
- **Módulos Spring:**  
  - Spring Web (API REST)  
  - Spring Data JPA (persistência)  
  - Spring Validation (validações)  
- **ORM:** Hibernate 7.2  
- **Banco de Dados:** MySQL 8.0  
- **Pool de conexões:** HikariCP  
- **Build:** Maven  
- **Utilitários:** Lombok  

---

## 📂 Estrutura do Projeto

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

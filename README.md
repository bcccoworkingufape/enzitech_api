# 🧪 Enzitech API

O **Enzitech API** é o serviço de backend responsável por gerenciar e processar dados para o aplicativo móvel Enzitech. O sistema permite o registro de experimentos, tratamentos e processos laboratoriais voltados para o cálculo e análise da atividade de enzimas do solo.

Desenvolvido com foco em escalabilidade e segurança, este projeto serve como núcleo de regras de negócios, cálculos matemáticos e persistência de dados para pesquisadores e estudantes.

---

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework:** Spring Boot 4
* **Banco de Dados:** PostgreSQL
* **Autenticação e Segurança:** Spring Security + JWT
* **ORM e Mapeamento:** Hibernate / Spring Data JPA
* **Infraestrutura:** Docker & Docker Compose
* **Gerenciador de Dependências:** Maven
---

## 🏗️ Estrutura de Domínios e Endpoints

### 🔐 Autenticação (`/auth`)
* `POST /auth/login` - Autenticação e geração do token JWT.
* `POST /auth/forgot-password` - Solicitação de recuperação de senha.
* `POST /auth/reset-password` - Redefinição de senha utilizando o token de recuperação.
* `POST /auth/verify-pin` - Verificação do PIN de recuperação.

### 👤 Usuários (`/users`)
* `GET /users` - Lista os usuários cadastrados (paginado).
* `GET /users/{id}` - Retorna os detalhes de um usuário.
* `POST /users` - Cria um novo usuário (Tipos: `USER` ou `ADMIN`).

### 🧬 Enzimas (`/enzymes`)
Gerencia o catálogo de enzimas disponíveis para os cálculos (ex: Beta-Glicosidase).
* `GET /enzymes` - Lista todas as enzimas cadastradas (retorno paginado em `content`).
* `GET /enzymes/{id}` - Busca uma enzima específica.
* `POST /enzymes` - Cadastra uma nova enzima (restrito a `ADMIN`).

### 🧪 Experimentos (`/experiments`)
* `GET /experiments` - Lista os experimentos do usuário logado.
* `POST /experiments` - Cria a estrutura inicial de um experimento (aceita inicialização vazia de tratamentos).
* `GET /experiments/{id}` - Retorna a estrutura detalhada (incluindo progresso, repetições e enzimas vinculadas).
* `POST /experiments/calculate/{id}` - Executa o motor de cálculo enzimático do experimento.
* `POST /experiments/save-result/{id}` - Salva os resultados processados.

### 🧫 Tratamentos (`/treatments`)
* `GET /treatments` - Lista todos os tratamentos.
* `GET /treatments/experiment/{experimentId}` - Lista os tratamentos pertencentes a um experimento específico.
* `POST /treatments` - Cria um novo tratamento exigindo o objeto e o ID do `experiment` vinculado.

---

## ⚙️ Como Rodar o Projeto (Ambiente de Desenvolvimento)

### Pré-requisitos
* Java 21+ instalado
* Maven 3.8+
* .env na raiz do projeto

### Passo a Passo

1. **Acessar o projeto**
   ```bash
   cd enzitech-api

2. **Executar o Maven**
   ```bash
   mvn spring-boot:run

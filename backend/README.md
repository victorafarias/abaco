# Ábaco - Backend

Para executar o backend do Ábaco, subir os containers da Registry, do PostgreSQL e Elasticsearch. Na pasta `src/main/docker` rodar o comando:

```console
user@computador:abaco_codigo_fonte/backend/src/main/docker$ docker-compose up -d
```

A aplicação Spring Boot roda com **Java 8** e pode ser executada com Maven:

```console
user@computador:abaco_codigo_fonte/backend$ ./mvnw
```
Ou pode ser executada pela IDE (IntelliJ ou Eclipse).

O Liquibase roda automaticamente na inicialização e cria a estrutura no banco PostgreSQL bem como o usuário inicial para usar a aplicação(login: `admin` e senha: `admin`)

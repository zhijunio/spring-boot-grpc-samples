# spring-boot-grpc-samples

[Spring Boot 4.1.1](https://docs.spring.io/spring-boot/reference/io/grpc.html) 的 gRPC 示例。形状对齐官方 [samples/grpc-server](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-server)、[samples/grpc-client](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-client)、[samples/grpc-tomcat](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-tomcat)、[samples/grpc-secure](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-secure)、[samples/grpc-tomcat-secure](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-tomcat-secure)、[samples/grpc-oauth2](https://github.com/spring-projects/spring-grpc/tree/main/samples/grpc-oauth2)。配套笔记：[Spring Boot 4 接入 gRPC](https://blog.zhijun.io/posts/spring-boot-grpc)。

仓库：<https://github.com/zhijunio/spring-boot-grpc-samples>。不含 Reactor、Native。

| 模块 | 作用 | 端口 |
|------|------|------|
| `grpc-server` | Netty `Simple`（`SayHello` / `StreamHello`） | gRPC 9090 |
| `grpc-client` | blocking stub + `CommandLineRunner` | 无 HTTP |
| `grpc-server-tomcat` | Tomcat Servlet，HTTP 和 gRPC 同端口 | 9090（HTTP/2） |
| `grpc-server-secure` | Netty + HTTP Basic / preauth | gRPC 9090 |
| `grpc-client-secure` | 对应 `grpc-server-secure`，默认通道 Basic | 无 HTTP |
| `grpc-server-tomcat-secure` | Tomcat + HTTP Basic | 9090（HTTP/2） |
| `grpc-client-tomcat-secure` | 对应 `grpc-server-tomcat-secure`，默认通道 Basic | 无 HTTP |
| `auth-server` | Authorization Server（client_credentials） | HTTP 9000 |
| `grpc-server-oauth2` | Netty + JWT 资源服务器 | gRPC 9090 |
| `grpc-client-oauth2` | 对应 `grpc-server-oauth2`，Bearer client_credentials | 无 HTTP |

需要 **Java 25**、Maven Wrapper。`grpc-client` 连无认证的 `grpc-server`；`grpc-client-secure` 连 `grpc-server-secure`；`grpc-client-tomcat-secure` 连 `grpc-server-tomcat-secure`（后两者默认通道都带 Basic `user`/`user`）。OAuth2：先起 `auth-server`，再起 `grpc-server-oauth2` 和 `grpc-client-oauth2`。

## 跑起来

两个终端（Netty 服务端 + 客户端）：

```bash
cd grpc-server && ./mvnw spring-boot:run
cd grpc-client && ./mvnw spring-boot:run
```

Servlet 同端口：

```bash
cd grpc-server-tomcat && ./mvnw spring-boot:run
```

HTTP Basic（Netty 服务端 + 客户端）：

```bash
cd grpc-server-secure && ./mvnw spring-boot:run
cd grpc-client-secure && ./mvnw spring-boot:run
```

HTTP Basic（Tomcat 同端口 + 客户端）：

```bash
cd grpc-server-tomcat-secure && ./mvnw spring-boot:run
cd grpc-client-tomcat-secure && ./mvnw spring-boot:run
```

OAuth2 JWT（认证服务器 + 资源服务器 + 客户端）：

```bash
cd auth-server && ./mvnw spring-boot:run
cd grpc-server-oauth2 && ./mvnw spring-boot:run
cd grpc-client-oauth2 && ./mvnw spring-boot:run
```

客户端启动后会打印：

```
message: "Hello ==> Alien"
```

直连服务端（需 [grpcurl](https://github.com/fullstorydev/grpcurl)）：

```bash
grpcurl --plaintext localhost:9090 list
grpcurl -d '{"name":"Alien"}' --plaintext localhost:9090 Simple/SayHello
```

带认证（密码和用户名相同）：

```bash
grpcurl --plaintext -H 'X-USER: user' -d '{"name":"Alien"}' localhost:9090 Simple/SayHello
grpcurl --plaintext -H "authorization: Basic $(printf 'user:user' | base64)" \
  -d '{"name":"Alien"}' localhost:9090 Simple/SayHello
```

`StreamHello` 在 `grpc-server-secure` 里要 `admin`。Health / Reflection 在 Netty 安全示例里不需要凭证。`grpc-server-tomcat-secure` 走 Servlet 默认鉴权，`user:user` 即可。`grpc-server-oauth2` 的 `SayHello` 只要合法 JWT；`StreamHello` 要 `SCOPE_profile`。默认 client_credentials 令牌没有这个 scope。

OAuth2 用 curl 换 token 再 grpcurl：

```bash
TOKEN=$(curl -s spring:secret@localhost:9000/oauth2/token -d grant_type=client_credentials | jq -r .access_token)
grpcurl -H "Authorization: Bearer $TOKEN" -d '{"name":"Alien"}' --plaintext localhost:9090 Simple/SayHello
```

测试：

```bash
cd grpc-server && ./mvnw test
cd grpc-client && ./mvnw test
cd grpc-server-tomcat && ./mvnw test
cd grpc-server-secure && ./mvnw test
cd grpc-client-secure && ./mvnw test
cd grpc-server-tomcat-secure && ./mvnw test
cd grpc-client-tomcat-secure && ./mvnw test
cd auth-server && ./mvnw test
cd grpc-server-oauth2 && ./mvnw test
cd grpc-client-oauth2 && ./mvnw test
```

`grpc-server` 测试对齐官方：`GrpcServerApplicationTests`、`GrpcServerSideTests`、`GrpcServerIntegrationTests`、`GrpcServerHealthIntegrationTests`、`GrpcClientApplicationTests`。`grpc-server-tomcat` / `grpc-server-tomcat-secure` 走 Tomcat 随机端口。`grpc-server-secure` 用随机 gRPC 端口加 Basic stub。`grpc-server-oauth2` 用 testjars 另起 Authorization Server，`ClientCredentialsTokenSupplier` 换 token；另有一条 opaque introspection。客户端 mock stub，只覆盖 `CommandLineRunner`。`auth-server` 测 client_credentials 换 token。

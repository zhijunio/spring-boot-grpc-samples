# spring-boot-grpc-samples

[Spring Boot 4.1.1](https://docs.spring.io/spring-boot/reference/io/grpc.html) 的 gRPC 示例：Netty 服务端默认 **9090**，客户端用 MVC 包一层 HTTP。配套笔记：[Spring Boot 4 接入 gRPC](https://blog.zhijun.io/posts/spring-boot-grpc)。

仓库：<https://github.com/zhijunio/spring-boot-grpc-samples>。只覆盖默认 Netty 路径，不含 Reactor、Native、安全、Servlet 同端口。

| 模块 | 作用 | 端口 |
|------|------|------|
| `grpc-server` | `HelloService`（`SayHello` / `LotsOfReplies`） | gRPC 9090 |
| `grpc-client` | HTTP 调 blocking stub | HTTP 8082 |

需要 **Java 25**、Maven Wrapper。客户端默认连 `static://localhost:9090`。

## 跑起来

两个终端：

```bash
cd grpc-server && ./mvnw spring-boot:run
cd grpc-client && ./mvnw spring-boot:run
```

直连服务端（需 [grpcurl](https://github.com/fullstorydev/grpcurl)）：

```bash
grpcurl --plaintext localhost:9090 list
grpcurl -d '{"greeting":"John Doe"}' --plaintext localhost:9090 com.example.HelloService/SayHello
```

走客户端：

```bash
curl -s "http://localhost:8082?greeting=John%20Doe"
curl -s "http://localhost:8082/lots-of-replies?greeting=John%20Doe"
```

测试：

```bash
cd grpc-server && ./mvnw test
cd grpc-client && ./mvnw test
```

服务端测试用 `@AutoConfigureTestGrpcTransport`（进程内，不占 9090）。Initializr 带的 `*ApplicationTests` 会起 Netty，和本机已占用的 9090 冲突，已不保留。

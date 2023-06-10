package io.omid.grpc;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.omid.grpc.service.BlogService;
import io.omid.grpc.service.CalculatorService;
import io.omid.grpc.service.GreetingService;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GRPCServerApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(GRPCServerApplication.class, args);
        Server server = ServerBuilder
                .forPort(8081)
                .addService(new GreetingService())
                .addService(new CalculatorService())
                .addService(BlogService.INSTANCE())
                .addService(ProtoReflectionService.newInstance()) // for reflection use
                .build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}

package io.omid.grpc.service;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import io.omid.grpc.*;
import lombok.SneakyThrows;

import java.util.stream.IntStream;

public class GreetingService extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();
        String lastName = request.getGreeting().getLastName();
        GreetResponse response = GreetResponse.newBuilder()
                .setResult("hi ".concat(firstName).concat(" ").concat(lastName))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @SneakyThrows
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();
        String lastName = request.getGreeting().getLastName();
        for (int i = 0; i < 11; i++) {
            String result = String.format("Hello %s %s, this is the response number: %d", firstName, lastName, i);
            var response = GreetManyTimesResponse.newBuilder().setResult(result).build();
            responseObserver.onNext(response);
            Thread.sleep(1000);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        return new StreamObserver<>() {
            String result = ">>>>>>>>>>>>>>>>>>>>>>>>> ";

            @Override
            public void onNext(LongGreetRequest longGreetRequest) {
                // client sends a message
                result += " Hello " + longGreetRequest.getGreeting().getFirstName() + " " +
                        longGreetRequest.getGreeting().getLastName();
            }

            @Override
            public void onError(Throwable throwable) {
                // client sends an error
            }

            @Override
            public void onCompleted() {
                // client is done
                responseObserver.onNext(
                        LongGreetResponse.newBuilder().setResult(result).build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest greetEveryoneRequest) {
                String result = "Hello ".concat(greetEveryoneRequest.getGreeting().getFirstName())
                        .concat(" ").concat(greetEveryoneRequest.getGreeting().getLastName());
                responseObserver.onNext(GreetEveryoneResponse.newBuilder().setResult(result).build());
            }

            @Override
            public void onError(Throwable throwable) {
                // do nothing
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {

        Context current = Context.current();

        IntStream.range(0, 3).forEach(i -> {
            try {
                if (!current.isCancelled()) {
                    System.out.println("Sleep for 100ms");
                    Thread.sleep(100);
                } else {
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Send response");
        responseObserver.onNext(GreetWithDeadlineResponse.newBuilder()
                .setResult(String.format("Hi %s %s", request.getGreeting().getFirstName(), request.getGreeting().getLastName()))
                .build());
        responseObserver.onCompleted();

    }
}

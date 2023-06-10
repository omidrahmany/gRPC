package com.example.grpccilent;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import io.omid.grpc.*;
import io.omid.grpc.blog.BlogInputDTO;
import io.omid.grpc.blog.BlogOutputDTO;
import io.omid.grpc.blog.BlogServiceGrpc;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GRpcClientApplication {
    private final ManagedChannel channel;
    private final GreetServiceGrpc.GreetServiceBlockingStub stub;
    private final CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorStub;

    public GRpcClientApplication() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 8081)
                .usePlaintext()
                .build();
        System.out.println("Creating stub");
        stub = GreetServiceGrpc.newBlockingStub(channel);
        calculatorStub = CalculatorServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        new GRpcClientApplication().run();
    }

    private void run() {
//        doUnaryCall();
//        doServerStreamingCall();
//        doClientStreamingCall();
//        doBiDiStreamingCall();
//        doErrorCall();
//        doUnaryCallWithDeadline();
        doMongoDbCall();
        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doMongoDbCall() {
        var blogServiceBlockingStub = BlogServiceGrpc.newBlockingStub(channel);
        BlogOutputDTO response = blogServiceBlockingStub.createBlog(BlogInputDTO.newBuilder()
                .setAuthorId("855")
                .setTitle("Instagram")
                .setContent("non stable situation.")
                .build());

        System.out.println(response.getResult());

    }

    private void doUnaryCallWithDeadline() {
        /**
         * the first call (3000ms deadline)
         * */
        try {
            var response = stub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder()
                                            .setFirstName("omid")
                                            .setLastName("InamhaR")
                                            .build())
                                    .build());

            System.out.println(String.format("Here is client, The response received from the server is %s", response.getResult()));
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().equals(Status.DEADLINE_EXCEEDED)) {
                System.out.println("Deadline has been exceeded, we don't want the response.");
            } else {
                ex.printStackTrace();
            }
        }

        /**
         * the second call (1000ms deadline)
         * */
        try {
            var response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadline(
                    GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName("omid")
                                    .setLastName("InamhaR")
                                    .getDefaultInstanceForType())
                            .build());
            System.out.println(String.format("Here is client, The response received from the server is %s", response.getResult()));
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().equals(Status.DEADLINE_EXCEEDED)) {
                System.out.println("Deadline has been exceeded, we don't want the response.");
            } else {
                ex.printStackTrace();
            }
        }
    }

    private void doErrorCall() {
        int number = -1;
        try {

            calculatorStub.squareRoot(SquareRootRequest.newBuilder().setNumber(number).build());
        } catch (StatusRuntimeException ex) {
            System.out.println(">>>>>>>>>>>>");
            System.out.println("Got an exception for square root");
            ex.printStackTrace();
        }


    }

    @SneakyThrows
    private void doBiDiStreamingCall() {
        var asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        var requestStreamObserver = asyncClient.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneResponse greetEveryoneResponse) {
                System.out.println("Response from server: ".concat(greetEveryoneResponse.getResult()));
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data.");
                latch.countDown();
            }
        });
        Arrays.asList("Omid", "Sahar", "Foad", "Atefe").forEach(name -> {
            requestStreamObserver.onNext(
                    GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName(name).setLastName("^_^ *").build())
                            .build());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        requestStreamObserver.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private void doClientStreamingCall() {
        var asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        var requestObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse longGreetResponse) {
                // we get a response from the server
                System.out.println("Received a response from the server:");
                System.out.println(longGreetResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                // we get an error from the server

            }

            @Override
            public void onCompleted() {
                // the server is done sending us data
                System.out.println("Server has completed sending us data.");
                latch.countDown();
            }
        });
        System.out.println("Sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder().setFirstName("OMID").setLastName("RAHMANI").build())
                .build());

        System.out.println("Sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder().setFirstName("FOAD").setLastName("KARIMI").build())
                .build());

        System.out.println("Sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder().setFirstName("HAMID").setLastName("TABAKHAH").build())
                .build());
//        we tell the server that the client is done sending data.
        requestObserver.onCompleted();

        latch.await(3L, TimeUnit.SECONDS);
    }

    private void doServerStreamingCall() {
        var greeting = Greeting.newBuilder()
                .setFirstName("Omid")
                .setLastName("rahmani")
                .build();
        var request = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        stub.greetManyTimes(request).forEachRemaining(greetManyTimesResponse ->
                System.out.println(greetManyTimesResponse.getResult()));
    }

    private void doUnaryCall() {
        GreetResponse response = stub.greet(GreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("omid")
                        .setLastName("rah")
                        .build())
                .build());
        System.out.println(response.getResult());
    }
}

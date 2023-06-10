package io.omid.grpc.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.omid.grpc.CalculatorServiceGrpc;
import io.omid.grpc.SquareRootRequest;
import io.omid.grpc.SquareRootResponse;

public class CalculatorService extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNumber();

        if (number >= 0) {
            double sqrt = Math.sqrt(number);
            responseObserver.onNext(SquareRootResponse.newBuilder().setNumberRoot(sqrt).build());
            responseObserver.onCompleted();
        } else {
            /**
             * Error Handling
             * */
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The number being sent is not positive.")
                    .augmentDescription(String.format("Given Number: %d", number))
                    .asRuntimeException());
        }
    }
}

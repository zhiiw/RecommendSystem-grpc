package com.example.grpc;

import io.grpc.*;

// New import
import io.grpc.stub.*;

public class Client
{
    public static void main( String[] args ) throws Exception
    {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext(true)
                .build();

        // Replace the previous synchronous code with asynchronous code.
        // This time use an async stub:
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);

        // Construct a request


    }
}

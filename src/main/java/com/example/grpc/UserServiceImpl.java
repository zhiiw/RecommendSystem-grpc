package com.example.grpc;

import io.grpc.stub.StreamObserver;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUserRating(UserRequest request, StreamObserver<UserRatingResponse> responseObserver) {
        super.getUserRating(request, responseObserver);
    }

    @Override
    public void getSingleUserRating(UserSingleRequest request, StreamObserver<UserSingleResponse> responseObserver) {
        super.getSingleUserRating(request, responseObserver);
    }

    @Override
    public void getUserProfile(UserRequest request, StreamObserver<UserProfileResponse> responseObserver) {
        super.getUserProfile(request, responseObserver);
    }
}

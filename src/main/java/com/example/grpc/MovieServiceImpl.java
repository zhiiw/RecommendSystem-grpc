package com.example.grpc;

import io.grpc.stub.StreamObserver;

public class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {
    @Override
    public void getMovieProfile(MovieRequest request, StreamObserver<MovieProfileResponse> responseObserver) {
        long movie_id = request.getMovieId();

//        MovieProfileResponse response = MovieProfileResponse.newBuilder();
    }
}

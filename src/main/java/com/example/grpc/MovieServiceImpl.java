package com.example.grpc;
import io.grpc.stub.StreamObserver;

public class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {
    @Override
    public void getMovieBasedRecommend(MovieRequest,MovieResponse){

    }
}

package com.example.grpc.recommend;

class ee implements Comparable<ee> {
    int userid;
    double similarity;

    ee(int userid, double similarity) {
        this.userid = userid;
        this.similarity = similarity;
    }

    @Override
    public int compareTo(ee e) {
        if (e.similarity > this.similarity) {
            return 1;
        }
        return 0;
    }
}

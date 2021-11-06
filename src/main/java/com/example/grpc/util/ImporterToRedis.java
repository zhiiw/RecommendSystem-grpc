package com.example.grpc.util;

import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ImporterToRedis {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("/home/zhiiw/文档/ml-25m/ratings.csv"));
        //parsing a CSV file into the constructor of Scanner class
        Jedis jedis = new Jedis("127.0.0.1",6379);
        sc.next();
        sc.useDelimiter(",");
        //setting comma as delimiter pattern
        int x=0;
        while (sc.hasNext()) {
            if(x>=20000){
                break;
            }
            jedis.hmset();

            x++;
        }
        sc.close();
        System.out.println(jedis.get("113"));
    }
    //tagId -> tag
    //用户看过的电影 加起来
    //
}

package com.example.grpc.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ImporterToRedis {
    public static void main(String[] args) throws FileNotFoundException {
        Jedis jedis = new Jedis("127.0.0.1",6379);
        importUserWatched(jedis);
//        System.out.println(jedis.get("113"));
    }

    public static Jedis cli_pool(String host, int port){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(2);
        JedisPool jedisPool = new JedisPool(config,host,port);
        return jedisPool.getResource();
    }

    private static void importUserWatched(Jedis jedis) throws FileNotFoundException {
        jedis.select(1);
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\ratings_train.csv"));
        //parsing a CSV file into the constructor of Scanner class
        sc.nextLine();
        int count = 0;
        while (sc.hasNext()) {
            String str = sc.nextLine();
            String s[] = str.split(",");
            String user_id = "user_"+s[0];
            String movie_id = "movie_"+s[1];
            jedis.sadd(user_id,movie_id);
            System.out.println(++count);
        }
        sc.close();

        for(int i = 1; i < 100; i++){
            System.out.println(jedis.smembers("user_"+String.valueOf(i)+"_watched"));
        }
    }

    private static void importMovies(Jedis jedis) throws FileNotFoundException {
        jedis.select(2);
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\movies.csv"));
        //parsing a CSV file into the constructor of Scanner class
        sc.nextLine();

        while (sc.hasNext()) {
<<<<<<< HEAD:src/main/java/com/example/grpc/ImporterToRedis.java
            String str = sc.nextLine();
            String s[] = str.split(",");
            String movie_id = "movie_"+s[0];
            Map<String,String> movie_info = new HashMap<>();
            movie_info.put("title",s[1]);
            movie_info.put("genres",s[2]);
            jedis.hmset(movie_id,movie_info);
            System.out.println(jedis.hgetAll(movie_id));
        }
        sc.close();
=======
            if(x>=20000){
                break;
            }
            jedis.hmset();

            x++;
        }
        sc.close();
        System.out.println(jedis.get("113"));
>>>>>>> master:src/main/java/com/example/grpc/util/ImporterToRedis.java
    }
    //tagId -> tag
    //用户看过的电影 加起来
    //
}

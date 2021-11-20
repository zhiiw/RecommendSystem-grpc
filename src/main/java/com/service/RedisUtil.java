package com.service;

import redis.clients.jedis.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.util.*;

public class RedisUtil {
    private static Map<String, Integer> movie_count = new HashMap<>();
    private static Map<String,Double> movie_total = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("127.0.0.1",6379);
//        importUserRating(jedis);
//        importMovies(jedis);
//        importTagsComment(jedis);
//        importScores(jedis);
//        importTags(jedis);
//        loadMovieProfile(jedis);
        loadMovieHistory(jedis);
//        loadUserProfile(jedis);
        System.out.println("finish file import");
    }

    public static Jedis cli_pool(String host, int port){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(2);
        JedisPool jedisPool = new JedisPool(config,host,port);
        return jedisPool.getResource();
    }

    private static void importUserRating(Jedis jedis) throws FileNotFoundException {
        jedis.select(1);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\ratings_train.csv"));
        //parsing a CSV file into the constructor of Scanner class
        System.out.println(sc.nextLine());
        long count = 0;
        while (sc.hasNext()) {
            String str = sc.nextLine();
            String s[] = str.split(",");
            String user_id = "user_"+s[0];
            String movie_rating = s[1]+"_"+s[2];
            if(movie_count.get(s[1]) != null){
                movie_count.put(s[1],movie_count.get(s[1])+1);
                movie_total.put(s[1],movie_total.get(s[1])+Double.parseDouble(s[2]));
            }else{
                movie_count.put(s[1],1);
                movie_total.put(s[1],Double.parseDouble(s[2]));
            }
//            pipe.zadd(user_id, Double.parseDouble(s[3]),movie_rating);
//            System.out.println(++count);
        }
        sc.close();
//        pipe.sync();
        System.out.println("finish rating");
    }

    private static void importMovies(Jedis jedis) throws FileNotFoundException {
        jedis.select(2);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\movies.csv"));
        //parsing a CSV file into the constructor of Scanner class
        System.out.println(sc.nextLine());
        while (sc.hasNext()) {
            String str = sc.nextLine();
            String s[] = str.split(",");
            String movie_id = "movie_"+s[0];
            Map<String,String> movie_info = new HashMap<>();
            movie_info.put("title",s[1]);
            movie_info.put("genres",s[2]);
            pipe.hmset(movie_id,movie_info);
//            System.out.println(pipe.hgetAll(movie_id));
        }
        sc.close();
        pipe.sync();
        System.out.println("finish");
    }

    private static void importTagsComment(Jedis jedis) throws FileNotFoundException {
        jedis.select(3);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\tags.csv"));
        //parsing a CSV file into the constructor of Scanner class
        System.out.println(sc.nextLine());
        long count = 0;
        while (sc.hasNext()) {
            String str = sc.nextLine();
            String s[] = str.split(",");
            String user_id = "user_"+s[0];
            String user_tag = "utag_"+s[0];
            String movie_id = s[1];
            pipe.zadd(user_id, Double.parseDouble(s[3]),movie_id);
            Map<String,String> tags_info = new HashMap<>();
            tags_info.put(movie_id,s[2]);
            pipe.hmset(user_tag,tags_info);
            ++count;
            if(count%5000000 == 0){
                pipe.sync();
            }
        }
        sc.close();
        pipe.sync();
        System.out.println("finish");
    }

    private static void importTags(Jedis jedis) throws FileNotFoundException{
        jedis.select(4);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\genome-tags.csv"));
        System.out.println(sc.nextLine());
        long count = 0;
        while(sc.hasNext()){
            String str = sc.nextLine();
            String s[] = str.split(",");
            String tag_id = "tag_"+s[0];
            String tag_name = s[1];
            pipe.set(tag_id,tag_name);
        }
        sc.close();
        pipe.sync();
        System.out.println("finish tagid");
    }

    private static void importScores(Jedis jedis) throws FileNotFoundException{
        jedis.select(5);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\genome-scores.csv"));
        System.out.println(sc.nextLine());
//        long count = 0;
        while(sc.hasNext()){
            String str = sc.nextLine();
            String s[] = str.split(",");
            if (s.length < 3){
                System.out.println(str);
            }
            String movie_id = "movie_"+s[0];
            String tag_id = s[1];
            Double value_rel = Double.parseDouble(s[2]);
            if(value_rel > 0.05) pipe.zadd(movie_id, value_rel,tag_id);
        }
        sc.close();
        pipe.sync();
        System.out.println("finish scores");
    }

    private static void loadMovieProfile(Jedis jedis) throws Exception {
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\movies.csv"));
        String str,s[],movieId,genre[];
        Map<String,String> info;
        Set<TagInfo> topTags;
        MovieInfo movieInfo;
        Map<String,String> movieStatistic = new HashMap<>();
        System.out.println(sc.nextLine());
        while(sc.hasNext()){
            str = sc.nextLine();
            s = str.split(",");
            movieId = "movie_"+s[0];
            System.out.println(movieId);
            jedis.select(2);
            info = jedis.hgetAll(movieId);
            movieInfo = new MovieInfo(info.get("title"));
            genre = info.get("genres").split("|");
            movieInfo.setGenre(genre);
            jedis.select(5);
            Set<Tuple> topTuple = jedis.zrangeByScoreWithScores(movieId,0,1,0,50);
            topTags = new HashSet<>();
            jedis.select(4);
            Iterator it = topTuple.iterator();
            while (it.hasNext()) {
                Tuple tp = (Tuple) it.next();
                topTags.add(new TagInfo(Integer.parseInt(tp.getElement()),jedis.get("tag_"+tp.getElement()),tp.getScore()));
            }
            movieInfo.setTopTags(topTags);
            jedis.select(6);
            jedis.set("movInfo_"+s[0],SerializeUtil.serializeToString(movieInfo));
            if(movie_count.get(s[0]) == null){
                movieStatistic.put("totalscore","0.0");
                movieStatistic.put("totalcount","0.0");
            }else{
                movieStatistic.put("totalscore",movie_total.get(s[0]).toString());
                movieStatistic.put("totalcount",movie_count.get(s[0]).toString());
            }

            jedis.hmset("movSt_"+s[0],movieStatistic);
        }
        sc.close();
        System.out.println("finish mov profile");
    }

    private static void loadMovieHistory(Jedis jedis) throws Exception{
        jedis.select(7);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("D:\\Codes\\RecommendSystem-grpc\\ratings_train.csv"));
        System.out.println(sc.nextLine());
        String s[],movieId,ratingInfo;
        double score;
        while(sc.hasNext()){
            s = sc.next().split(",");
            movieId = "movie_"+s[1];
            ratingInfo = s[0]+"_"+s[2];
            score = Double.parseDouble(s[3]);
            pipe.zadd(movieId,score,ratingInfo);
        }
        pipe.sync();
        System.out.println("finish movie history");
    }

    private static void loadUserProfile(Jedis jedis) throws Exception{

    }

}

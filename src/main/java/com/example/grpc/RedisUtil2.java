package com.example.grpc;




import com.csvreader.CsvReader;
import com.example.grpc.util.MovieInfo;
import com.example.grpc.util.SerializeUtil;
import com.example.grpc.util.TagInfo;
import io.netty.util.internal.StringUtil;
import redis.clients.jedis.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.grpc.util.CSVUtil.outputToCSV;

//import static com.service.CSVUtil.outputToCSV;

public class RedisUtil2 {
    private static Map<String, Integer> movie_count = new HashMap<>();
    private static Map<String,Integer> movie_high_count = new HashMap<>();
    private static Map<String,Double> movie_total = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Jedis jedis = RedisUtil.cli_pool("127.0.0.1",6379);
        // 1.half xaaa 2.106804
        outputToCSV(jedis);
        System.out.println("finish file export");
//        Jedis jedis = new Jedis("127.0.0.1",6379);
        importUserRating(jedis);
        importMovies(jedis);
        importScores(jedis);
        importTags(jedis);
        loadMovieProfile(jedis);
        loadUserProfile(jedis);
        loadUserProfile(jedis);
//        System.out.println("finish file import");
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
        Scanner sc = new Scanner(new File("/home/zhiiw/PycharmProjects/Recommend/ratings_train.csv"));
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
                if(Double.parseDouble(s[2]) >= 4){
                    movie_high_count.put(s[1],movie_high_count.get(s[1])+1);
                }
                movie_total.put(s[1],movie_total.get(s[1])+Double.parseDouble(s[2]));
            }else{
                movie_count.put(s[1],1);
                movie_high_count.put(s[1],0);
                movie_total.put(s[1],Double.parseDouble(s[2]));
            }
            pipe.zadd(user_id, Double.parseDouble(s[3]),movie_rating);
        }
        sc.close();
        pipe.sync();
        System.out.println("finish rating");
    }

    private static void importMovies(Jedis jedis) throws IOException {
        jedis.select(2);
        Pipeline pipe = jedis.pipelined();
        CsvReader csvReader = new CsvReader("/home/zhiiw/文档/ml-25m/movies.csv");
        //parsing a CSV file into the constructor of Scanner class
        System.out.println(csvReader.readHeaders());
        while (csvReader.readRecord()) {
            String movie_id = "movie_"+csvReader.get("movieId");
            Map<String,String> movie_info = new HashMap<>();
            movie_info.put("title",csvReader.get("title"));
            movie_info.put("genres",csvReader.get("genres"));
            pipe.hmset(movie_id,movie_info);
//            System.out.println(pipe.hgetAll(movie_id));
        }
        csvReader.close();
        pipe.sync();
        System.out.println("finish");
    }

    private static void importTagsComment(Jedis jedis) throws FileNotFoundException {
        jedis.select(3);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("/home/zhiiw/文档/ml-25m/tags.csv"));
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

    private static void countMaxWatched(Jedis jedis) throws Exception{
        jedis.select(1);
        Set<String> key = jedis.keys("user_*");
        System.out.println(key.size()+"hhh");
        long count,maxcount = 0;
        Iterator it = key.iterator();
        while(it.hasNext()){
            String k = (String) it.next();
            count = jedis.zcard(k);
            if(count > 1000){
                maxcount++;
            }
        }
        System.out.println(maxcount+"!!");
    }

    private static void importTags(Jedis jedis) throws FileNotFoundException{
        jedis.select(4);
        Pipeline pipe = jedis.pipelined();
        Scanner sc = new Scanner(new File("/home/zhiiw/文档/ml-25m/genome-tags.csv"));
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
        Scanner sc = new Scanner(new File("/home/zhiiw/文档/ml-25m/genome-scores.csv"));
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
        Scanner sc = new Scanner(new File("/home/zhiiw/文档/ml-25m/movies.csv"));
        String str,s[],movieId,genre[];
        Map<String,String> info;
        Set<TagInfo> topTags;
        MovieInfo movieInfo;
        String title;
        String releaseTime;
        Map<String,String> movieStatistic = new HashMap<>();
        System.out.println(sc.nextLine());
        while(sc.hasNext()){
            str = sc.nextLine();
            s = str.split(",");
            movieId = "movie_"+s[0];
//            System.out.println(movieId);
            jedis.select(2);
            info = jedis.hgetAll(movieId);
            title = info.get("title");
            releaseTime = findNumber(title);
            if(releaseTime == null){
                releaseTime = "0";
            }else{
                releaseTime = releaseTime.substring(1,releaseTime.length()-1);
            }
            movieInfo = new MovieInfo(title,Long.parseLong(releaseTime));
            genre = info.get("genres").split("\\|");
            movieInfo.setGenre(genre);
//            System.out.println(movieInfo.getGenre().size());
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
            jedis.set("movInfo_"+s[0], SerializeUtil.serializeToString(movieInfo));
            if(movie_count.get(s[0]) == null){
                movieStatistic.put("totalscore","0.0");
                movieStatistic.put("totalcount","0");
            }else{
                movieStatistic.put("totalscore",movie_total.get(s[0]).toString());
                movieStatistic.put("totalcount",movie_count.get(s[0]).toString());
            }
            if(movie_high_count.get(s[0]) == null){
                jedis.hset("movSt_"+s[0],"totalhigh","0");
            }else{
                jedis.hset("movSt_"+s[0],"totalhigh",movie_high_count.get(s[0]).toString());
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
        jedis.select(1);
        Set<String> keys = jedis.keys("user_*");
        Iterator<String > it = keys.iterator();
        Set<Tuple> u_ratings;
        List<Tuple> rating_list;
        String uinfo;
        Map<String,String> userStatistic = new HashMap<>();
        int count;
        double lasting;
        int maxCount = 0;
        String lovegre = "";
        while(it.hasNext()){
            maxCount = 0;
            String key = it.next();
            u_ratings = jedis.zrangeWithScores(key,0,-1);
            count = u_ratings.size();
            rating_list = new ArrayList<>(u_ratings);
            Iterator<Tuple> uit = u_ratings.iterator();
            Map<String,Integer> love_count = new HashMap<>();
            int total = 0,totalhigh = 0;
            while(uit.hasNext()){
                Tuple tuple = uit.next();
                String minfo = tuple.getElement();
                String mid = minfo.split("_")[0];
                Double rating = Double.parseDouble(minfo.split("_")[1]);
                if(rating >= 4){
                    totalhigh++;
                }
                total += rating;
                jedis.select(6);
                MovieInfo info = (MovieInfo) SerializeUtil.deserializeToObj(jedis.get("movInfo_"+mid));
                if(info == null){
                    System.out.println("find null");
                }
                List<String> ges = info.getGenre();
//                System.out.println(ges.size());
                for(String ge:ges){
//                    System.out.println(ge);
                    if(love_count.get(ge) == null){
                        love_count.put(ge,0);
                    }else{
                        love_count.put(ge,love_count.get(ge)+1);
                    }
                }
            }
            for (Map.Entry<String, Integer> entry : love_count.entrySet()) {
                int c =  entry.getValue();
                if(c > maxCount){
                    maxCount = c;
                    lovegre = entry.getKey();
                }
            }
            lasting = Math.abs(rating_list.get(0).getScore()-rating_list.get(rating_list.size()-1).getScore());
            uinfo = "userSt_"+key.split("_")[1];
            userStatistic.put("count",String.valueOf(count));
            userStatistic.put("freq",String.valueOf(count/lasting));
            userStatistic.put("totalhigh",String.valueOf(totalhigh));
            userStatistic.put("total",String.valueOf(total));
            userStatistic.put("favor",lovegre);
//            System.out.println(lovegre);
            jedis.select(1);
            jedis.hmset(uinfo,userStatistic);
            jedis.hset(uinfo,"totalhigh",String.valueOf(totalhigh));
            jedis.hset(uinfo,"totalhigh",String.valueOf(totalhigh));
        }
        System.out.println("finish user profile");
    }

    public static String findNumber(String str){
        String pattern = "\\([1-9]\\d*\\)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        if (m.find()) {
            return m.group(0);
        }
        return null;
    }
}

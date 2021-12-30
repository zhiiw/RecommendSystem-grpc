package com.example.grpc.util;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import redis.clients.jedis.Jedis;
import tensorflow.serving.PredictionServiceGrpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.example.grpc.RecommandClient.getPri;

public class CSVUtil {
    public static void main(String[] args) throws Exception {
        Jedis jedis = RedisUtil.cli_pool("127.0.0.1",6379);
        outputToCSV(jedis);
        System.out.println("finish file export");
    }
    public static Comparator<Map<Integer, Float>> mapComparator = new Comparator<Map<Integer, Float>>() {
        public int compare(Map<Integer, Float> m1, Map<Integer, Float> m2) {
            return m2.get(1).compareTo(m1.get(1));
        }
    };

    public static void outputToCSV(Jedis jedis) throws IOException {
        Scanner csvReader = new Scanner(new File("/home/zhiiw/PycharmProjects/data_film/eeeee.csv"));
        String[] headers = {"uid","mid","user_average","user_count","user_frequency","user_highrate","movie_release","movie_average",
                "movie_count","movie_highrate","user_favor","movie_genre","label"};
        String label;
        String utotal,ucount,ufreq,ufavor,urate,mtotal,mcount,mgenre,mrate;
        Double duavg,dmavg,durate,dmrate;
        long mrelease;
        int count =30;
        //java write to csv
        //get max 30 pair
        List<Map<Integer,Float>> list= new ArrayList<>(20);
        while (csvReader.hasNext()){
            jedis.select(1);
            String[] line = csvReader.nextLine().split(" ");
            String user_id = line[0];
            String movie_id = line[1];
            Map<String,String> user_profile = jedis.hgetAll("userSt_"+user_id);
            if(user_profile.isEmpty()){
                duavg = 0.0;
                durate = 0.0;
                ucount = "0";
                ufreq = "0.0";
                urate = "0";
                ufavor = "(no genres listed)";
            }else{
                utotal = user_profile.get("total");
                ucount = user_profile.get("count");
                ufreq = user_profile.get("freq");
                ufavor = user_profile.get("favor");
                urate = user_profile.get("totalhigh");
                if(ufreq == "Infinity"){
                    ufreq = "1.0";
                }
                durate = Double.parseDouble(urate)/Double.parseDouble(ucount);
                duavg = Double.parseDouble(utotal)/Double.parseDouble(ucount);
            }
            jedis.select(6);
            MovieInfo movieInfo = (MovieInfo) SerializeUtil.deserializeToObj(jedis.get("movInfo_"+movie_id));
            Map<String,String> movie_statistic = jedis.hgetAll("movSt_"+movie_id);
            mrelease = movieInfo.getReleaseTime();
            mgenre = movieInfo.getGenre().get(0);
            mcount = movie_statistic.get("totalcount");
            mtotal = movie_statistic.get("totalscore");
            mrate = movie_statistic.get("totalhigh");
            if(Double.parseDouble(mcount) <= 0){
                dmavg = 0.0;
                dmrate = 0.0;
            }else{
                dmavg = Double.parseDouble(mtotal)/Integer.parseInt(mcount);
                dmrate = Double.parseDouble(mrate)/Integer.parseInt(mcount);
            }

            jedis.select(2);
            String[] content = {user_id,movie_id, String.valueOf(duavg),ucount,ufreq,String.valueOf(durate), String.valueOf(mrelease),
                    String.valueOf(dmavg),mcount,String.valueOf(dmrate),ufavor,mgenre};
            ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.43.218", 8500).usePlaintext(true).build();
            // 这里使用block模式
            PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
            float predict = getPri(stub,duavg,Integer.parseInt(ucount),Double.parseDouble(ufreq),ufavor,durate,dmavg,Integer.parseInt(mcount),(int)mrelease,dmrate,mgenre);
            list.add(new HashMap<Integer,Float>(){{put(Integer.valueOf(user_id),(Float) predict);;}});
            if(list.size() == 30){
                //delete the depulicate movie in list
                list.sort(mapComparator);
            }
            int []index = new int[5];
            // get biggest 5 one in list
            //write to csv
            CsvWriter csvWriter = new CsvWriter("/home/zhiiw/PycharmProjects/data_film/result.csv",',',Charset.forName("UTF-8"));
            csvWriter.writeRecord();
            for(int i = 0;i<5;i++){
                index[i] = list.get(i).keySet().iterator().next();
            }

        }
        csvReader.close();
    }


}

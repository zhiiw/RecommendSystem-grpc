package com.example.grpc;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import redis.clients.jedis.Jedis;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RecommandClient {
    public static void main(String[] args) {
        System.out.println("请选择一下两种功能:\n1) 输入uid获得5条电影推荐结果\n2) 输入uid和mid返回");

//        boolean invalid = true;
//        while(invalid){
//            Scanner sc = new Scanner(System.in);
//            switch (sc.nextInt()){
//                case 1:
//                    invalid = false;
//                    recommandMovies(sc);
//                    break;
//                case 2:
//                    invalid = false;
//                    recommandSingle(sc);
//                    break;
//                default:
//                    System.out.println("请重新输入");
//            }
//        }
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.3.159", 8506).usePlaintext(true).build();
//        // 这里使用block模式
//        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
//        long t = System.currentTimeMillis();
//        for (int i = 0; i < 10; i++) {
//            System.out.println(getPri(stub,3.6,100,0.7,"Drama",0.4,
//                    4.2,500,1980,0.5,"Comedy"));
//        }
//        System.out.println((System.currentTimeMillis()-t)/1000);
    }

    public static float getPri(PredictionServiceGrpc.PredictionServiceBlockingStub stub,double uavg_l,int ucnt_l,
                               double ufreq_l,String ufav_l,double urate_l,double mavg_l,int mcnt_l,int mrel_l,
                               double mrate_l,String mgre_l){
        // 创建请求
        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
        // 模型名称和模型方法名预设
        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName("rank_model");
        modelSpecBuilder.setSignatureName("");
        predictRequestBuilder.setModelSpec(modelSpecBuilder);
        // 设置入参,访问默认是最新版本，如果需要特定版本可以使用tensorProtoBuilder.setVersionNumber方法

        TensorProto.Builder uavg_TensorProto = TensorProto.newBuilder();
        uavg_TensorProto.setDtype(DataType.DT_DOUBLE);
        uavg_TensorProto.addDoubleVal(uavg_l);
        TensorShapeProto.Builder uavg_ShapeBuilder = TensorShapeProto.newBuilder();
        uavg_ShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        uavg_TensorProto.setTensorShape(uavg_ShapeBuilder.build());

        TensorProto.Builder ucnt_TensorProto = TensorProto.newBuilder();
        ucnt_TensorProto.setDtype(DataType.DT_INT64);
        ucnt_TensorProto.addInt64Val(ucnt_l);
        TensorShapeProto.Builder ucnt_ShapeBuilder = TensorShapeProto.newBuilder();
        ucnt_ShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        ucnt_TensorProto.setTensorShape(ucnt_ShapeBuilder.build());

        TensorProto.Builder ufreq_TensorProto = TensorProto.newBuilder();
        ufreq_TensorProto.setDtype(DataType.DT_DOUBLE);
        ufreq_TensorProto.addDoubleVal(ufreq_l);
        TensorShapeProto.Builder ufreq_ShapeBuilder = TensorShapeProto.newBuilder();
        ufreq_ShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        ufreq_TensorProto.setTensorShape(ufreq_ShapeBuilder.build());

        TensorProto.Builder ufav_TensorProto = TensorProto.newBuilder();
        ufav_TensorProto.setDtype(DataType.DT_STRING);
        ufav_TensorProto.addStringVal(ByteString.copyFromUtf8(ufav_l));
        TensorShapeProto.Builder ufav_ShapeBuilder = TensorShapeProto.newBuilder();
        ufav_ShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        ufav_TensorProto.setTensorShape(ufav_ShapeBuilder.build());

        TensorProto.Builder urate_TensorProto = TensorProto.newBuilder();
        urate_TensorProto.setDtype(DataType.DT_DOUBLE);
        urate_TensorProto.addDoubleVal(urate_l);
        TensorShapeProto.Builder urateShapeBuilder = TensorShapeProto.newBuilder();
        urateShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        urate_TensorProto.setTensorShape(urateShapeBuilder.build());

        TensorProto.Builder mavg_TensorProto = TensorProto.newBuilder();
        mavg_TensorProto.setDtype(DataType.DT_DOUBLE);
        mavg_TensorProto.addDoubleVal(mavg_l);
        TensorShapeProto.Builder mavgShapeBuilder = TensorShapeProto.newBuilder();
        mavgShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        mavg_TensorProto.setTensorShape(mavgShapeBuilder.build());

        TensorProto.Builder mcnt_TensorProto = TensorProto.newBuilder();
        mcnt_TensorProto.setDtype(DataType.DT_INT64);
        mcnt_TensorProto.addInt64Val(mcnt_l);
        TensorShapeProto.Builder mcntShapeBuilder = TensorShapeProto.newBuilder();
        mcntShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        mcnt_TensorProto.setTensorShape(mcntShapeBuilder.build());

        TensorProto.Builder mrel_TensorProto = TensorProto.newBuilder();
        mrel_TensorProto.setDtype(DataType.DT_INT64);
        mrel_TensorProto.addInt64Val(mrel_l);
        TensorShapeProto.Builder mrelShapeBuilder = TensorShapeProto.newBuilder();
        mrelShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        mrel_TensorProto.setTensorShape(mrelShapeBuilder.build());

        TensorProto.Builder mrate_TensorProto = TensorProto.newBuilder();
        mrate_TensorProto.setDtype(DataType.DT_DOUBLE);
        mrate_TensorProto.addDoubleVal(mrate_l);
        TensorShapeProto.Builder mrateShapeBuilder = TensorShapeProto.newBuilder();
        mrateShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        mrate_TensorProto.setTensorShape(mrateShapeBuilder.build());

        TensorProto.Builder mgre_TensorProto = TensorProto.newBuilder();
        mgre_TensorProto.setDtype(DataType.DT_STRING);
        mgre_TensorProto.addStringVal(ByteString.copyFromUtf8(mgre_l));
        TensorShapeProto.Builder mgreShapeBuilder = TensorShapeProto.newBuilder();
        mgreShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        mgre_TensorProto.setTensorShape(mgreShapeBuilder.build());

        predictRequestBuilder.putInputs("user_average", uavg_TensorProto.build());
        predictRequestBuilder.putInputs("user_count", ucnt_TensorProto.build());
        predictRequestBuilder.putInputs("user_frequency", ufreq_TensorProto.build());
        predictRequestBuilder.putInputs("user_favor", ufav_TensorProto.build());
        predictRequestBuilder.putInputs("user_highrate", urate_TensorProto.build());

        predictRequestBuilder.putInputs("movie_average", mavg_TensorProto.build());
        predictRequestBuilder.putInputs("movie_count", mcnt_TensorProto.build());
        predictRequestBuilder.putInputs("movie_release", mrel_TensorProto.build());
        predictRequestBuilder.putInputs("movie_highrate", mrate_TensorProto.build());
        predictRequestBuilder.putInputs("movie_genre", mgre_TensorProto.build());


        // 访问并获取结果
        Predict.PredictResponse predictResponse = stub.withDeadlineAfter(10, TimeUnit.SECONDS).predict(predictRequestBuilder.build());
        Map<String, TensorProto> result = predictResponse.getOutputsMap();
        // CRF模型结果，发射概率矩阵和状态概率矩阵
//        System.out.println("预测值是:" + result.get("output_1").getFloatVal(0));
        return result.get("output_1").getFloatVal(0);
    }
// uid mid predict
    public void recommandMovies(Scanner sc){
        System.out.println("请输入用户id: ");
        int uid = sc.nextInt();

    }

    public void recommandSingle(Scanner sc){
        System.out.println("请输入用户id: ");
        int uid = sc.nextInt();
        System.out.println("请输入电影id: ");
        int mid = sc.nextInt();
//        Profile profile = getSingleProfile(uid,mid);
    }

//    public void getSingleProfile(int uid,int mid){
//        String label;
//        String utotal,ucount,ufreq,ufavor,urate,mtotal,mcount,mgenre,mrate;
//        Double duavg,dmavg,durate,dmrate;
//        Long mrelease;
//        Jedis jedis = new Jedis();jedis.select(1);
////        String user_id = csvReader.get("userId");
////        String movie_id = csvReader.get("movieId");
////        Double score = Double.parseDouble(csvReader.get("rating"));
////        if(score >= 4){
////            label = "1";
////        }else{
////            label = "0";
////        }
////        Map<String,String> user_profile = jedis.hgetAll("userSt_"+user_id);
//        if(user_profile.isEmpty()){
//            duavg = 0.0;
//            durate = 0.0;
//            ucount = "0";
//            ufreq = "0.0";
//            urate = "0";
//            ufavor = "(no genres listed)";
//        }else{
//            utotal = user_profile.get("total");
//            ucount = user_profile.get("count");
//            ufreq = user_profile.get("freq");
//            ufavor = user_profile.get("favor");
//            urate = user_profile.get("totalhigh");
//            if(ufreq == "Infinity"){
//                ufreq = "1.0";
//            }
//            durate = Double.parseDouble(urate)/Double.parseDouble(ucount);
//            duavg = Double.parseDouble(utotal)/Double.parseDouble(ucount);
//        }
//        jedis.select(6);
//        MovieInfo movieInfo = (MovieInfo) SerializeUtil.deserializeToObj(jedis.get("movInfo_"+movie_id));
//        Map<String,String> movie_statistic = jedis.hgetAll("movSt_"+movie_id);
//        mrelease = movieInfo.getReleaseTime();
//        mgenre = movieInfo.getGenre().get(0);
//        mcount = movie_statistic.get("totalcount");
//        mtotal = movie_statistic.get("totalscore");
//        mrate = movie_statistic.get("totalhigh");
//        if(Double.parseDouble(mcount) <= 0){
//            dmavg = 0.0;
//            dmrate = 0.0;
//        }else{
//            dmavg = Double.parseDouble(mtotal)/Integer.parseInt(mcount);
//            dmrate = Double.parseDouble(mrate)/Integer.parseInt(mcount);
//        }
//
//        jedis.select(2);
//        String[] content = {user_id,movie_id, String.valueOf(duavg),ucount,ufreq,String.valueOf(durate), String.valueOf(mrelease),
//                String.valueOf(dmavg),mcount,String.valueOf(dmrate),ufavor,mgenre,label};
//        csvWriter.writeRecord(content);
//    }

}

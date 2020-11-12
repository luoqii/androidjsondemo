package com.example.androidjsondemo;

import androidx.core.widget.TextViewCompat;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTest extends BaseTest {

    private SampleData mSampleDataFromFastJson;
    private SampleData mSampleDataFromGson;

    List<Data> mDatas;

    @Before
    public void setUp() {
        mSampleDataFromFastJson = null;
        String jsonStr = new String(resourceFileData("sample.json"));
//        mSampleDataFromFastJson = JSON.parseObject(jsonStr, SampleData.class);

        Gson gson = new Gson();
        mSampleDataFromGson = gson.fromJson(jsonStr, SampleData.class);

        mDatas = new ArrayList<>();
        Data d = new Data();
        d.longField = 2;
        d.name = "simple";
        mDatas.add(d);

        d = new Data();
        d.longField = 9223372036854775807L; // 2^63 - 1
        d.name = "big long";
        mDatas.add(d);
    }

    @Test
    public void json(){
        for (Data d : mDatas) {
            String name = d.name;
//            Data dataFromFastJson = findByName(mSampleDataFromFastJson.datas, name);
            Data dataFromGson = findByName(mSampleDataFromGson.datas, name);

            String prefix = name;
//            assertNotNull(prefix + "", dataFromFastJson);
            assertNotNull(prefix + "", dataFromGson);

//            assertTrue(prefix + " not equal expected:" + d + " actural fastjson:" + dataFromFastJson,
//                    d.equalsDeeply(dataFromFastJson));
            assertTrue(prefix + " not equal expected:" + d + " actural fastjson:" + dataFromGson,
                    d.equalsDeeply(dataFromGson));
        }
    }

    Data findByName(List<Data> datas, String name) {
        int index = -1;
        for (int i = 0 ; i < datas.size() ; i++){
            if (name.equalsIgnoreCase(datas.get(i).name)) {
                if (index >= 0) {
                    throw new IllegalStateException("found more than one data. name:" + name);
                }
                index = i;
            }
        };

        return datas.get(index);
    }

    @Test
    public void MapOfBigNumber(){

        String json = "{\"time\":1536061697,\"name\":\"test\"}";
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, Map.class);
        String timeStr = map.get("time").toString();
        System.out.println(timeStr);

        json = "{\"time\":1,\"name\":\"test\"}";
        gson = new Gson();
        map = gson.fromJson(json, Map.class);
        timeStr = map.get("time").toString();
        System.out.println(timeStr);
    }


    public static class SampleData {
        public List<Data> datas;
    }

    public static class Data {
        public String name;

        public long longField;

        public boolean equalsDeeply(Data other) {
            boolean equals = true;
            equals &= (longField == other.longField);

            return equals;
        }
    }
}

package com.example.androidjsondemo;

import androidx.core.widget.TextViewCompat;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public void json() {
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
        for (int i = 0; i < datas.size(); i++) {
            if (name.equalsIgnoreCase(datas.get(i).name)) {
                if (index >= 0) {
                    throw new IllegalStateException("found more than one data. name:" + name);
                }
                index = i;
            }
        }
        ;

        return datas.get(index);
    }

    @Test
    public void mapOfBigNumber() {
        DecimalFormat decimalFormat = new DecimalFormat("###################.###############");
        String json = "{\"time\":153606169766,\"name\":\"test\"}";
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, Map.class);
        String timeStr = map.get("time").toString();
        System.out.println(timeStr);

        json = "{\"time\":9223372036854775807,\"name\":\"test\"}";
        gson = new Gson();
        map = gson.fromJson(json, Map.class);
        Object timeStr1 = map.get("time");
        if (timeStr1 instanceof Double) {
            double time = (double) timeStr1;
            String longString = decimalFormat.format(time);
            System.out.println(time);
            System.out.println(longString);
        }

        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(Map.class, new TypeAdapter<Map>() {
            @Override
            public void write(JsonWriter out, Map value) throws IOException {

            }

            @Override
            public Map read(JsonReader in) throws IOException {
                Map map = new HashMap();
                JsonToken token = in.peek();
                switch (token) {

                    case BEGIN_OBJECT:
                        map = new LinkedTreeMap<String, Object>();
                        in.beginObject();
                        while (in.hasNext()) {
                            map.put(in.nextName(), readValue(in));
                        }
                        in.endObject();
                        break;

                    default:
                        throw new IllegalStateException();
                }

                return map;
            }

            Object readValue(JsonReader in) throws IOException {
                JsonToken token = in.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                        List<Object> list = new ArrayList<Object>();
                        in.beginArray();
                        while (in.hasNext()) {
                            list.add(read(in));
                        }
                        in.endArray();
                        return list;

                    case BEGIN_OBJECT:
                        Map<String, Object> map = new LinkedTreeMap<String, Object>();
                        in.beginObject();
                        while (in.hasNext()) {
                            map.put(in.nextName(), readValue(in));
                        }
                        in.endObject();
                        return map;

                    case STRING:
                        return in.nextString();

                    case NUMBER:
                        String numberStr = in.nextString();
                        if (numberStr.contains(".")
                                || numberStr.contains("e")
                                || numberStr.contains("E")) {
                            return in.nextDouble();
                        } else {
                            try {
                                Long l = Long.parseLong(numberStr);
                                return l;
                            } catch (NumberFormatException e) {
                                return in.nextLong();
                            }
                        }

                    case BOOLEAN:
                        return in.nextBoolean();

                    case NULL:
                        in.nextNull();
                        return null;

                    default:
                        throw new IllegalStateException();
                }
            }
        });
        gson = b.create();
        json = "{\"time\":9223372036854775807,\"name\":\"test\"," +
                " \"object\":{\"time\":9223372036854775807,\"name\":\"test\"}}";
        map = gson.fromJson(json, Map.class);
        System.out.println(map);

    }


    @Test
    public void mapOfNumber() {
        String jsonStr = "";
        String generateJsonStr = null;
        Gson gson = new Gson();
        Map generatedMap = new HashMap();
        TypeAdapter<Map> mapTypeAdapter = new TypeAdapter<Map>() {
            @Override
            public void write(JsonWriter out, Map value) throws IOException {
                if (null == value) {
                    out.nullValue();
                    return;
                }

                out.beginObject();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    out.name(entry.getKey());
                    writeValue(out, entry.getValue());
                }
                out.endObject();

            }

            void writeValue(JsonWriter out, Object value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else if (value instanceof String) {
                    out.value((String) value);
                } else if (value instanceof Long) {
                    out.value((Long) value);
                } else if (value instanceof Number) {
                    out.value((Number) value);
                } else if (value instanceof ArrayList<?>) {
                    out.beginArray();
                    ArrayList l = (ArrayList) value;
                    for (int i = 0; i < l.size(); i++) {
                        writeValue(out, l.get(i));
                    }
                    out.endArray();
                } else if (value instanceof Map) {
                    write(out, (Map) value);
                }

            }


            @Override
            public Map read(JsonReader in) throws IOException {
                Map map = new HashMap();
                JsonToken token = in.peek();
                switch (token) {

                    case BEGIN_OBJECT:
                        map = new LinkedTreeMap<String, Object>();
                        in.beginObject();
                        while (in.hasNext()) {
                            map.put(in.nextName(), readValue(in));
                        }
                        in.endObject();
                        break;

                    case NULL:
                        break;

                    default:
                        throw new IllegalStateException();
                }

                return map;
            }

            Object readValue(JsonReader in) throws IOException {
                // ashamed copied from ObjectTypeAdapter, modify number reading logic only.
                JsonToken token = in.peek();
                switch (token) {
                    case BEGIN_ARRAY:
                        List<Object> list = new ArrayList<Object>();
                        in.beginArray();
                        while (in.hasNext()) {
                            list.add(readValue(in));
                        }
                        in.endArray();
                        return list;

                    case BEGIN_OBJECT:
                        Map<String, Object> map = new LinkedTreeMap<String, Object>();
                        in.beginObject();
                        while (in.hasNext()) {
                            map.put(in.nextName(), readValue(in));
                        }
                        in.endObject();
                        return map;

                    case STRING:
                        return in.nextString();

                    case NUMBER:
                        String numberStr = in.nextString();
                        if (numberStr.contains(".")
                                || numberStr.contains("e")
                                || numberStr.contains("E")) {
                            return Double.parseDouble(numberStr);
                        } else {
                            try {
                                Long l = Long.parseLong(numberStr);
                                return l;
                            } catch (NumberFormatException e) {
                                return Double.parseDouble(numberStr);
                            }
                        }

                    case BOOLEAN:
                        return in.nextBoolean();

                    case NULL:
                        in.nextNull();
                        return null;

                    default:
                        throw new IllegalStateException();
                }
            }
        };

        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(Map.class, mapTypeAdapter);
        gson = b.create();

        // 9223372036854775807 == 2^63 -1
        jsonStr = "{\"time\":9223372036854775807,\"name\":\"test\",\"array\":[1,22,33,4444]," +
                " \"object\":{\"time\":9223372036854775807,\"name\":\"test\",\"array\":[1,22,33,4444]}}";
        generatedMap = gson.fromJson(jsonStr, Map.class);
        System.out.println(generatedMap);
        generateJsonStr = gson.toJson(generatedMap, Map.class);
        System.out.println(generateJsonStr);

        // 9223372036854775808 == 2^63
        jsonStr = "{\"time\":9223372036854775808,\"name\":\"test\",\"array\":[1,22,33,4444]," +
                " \"object\":{\"time\":9223372036854775807,\"name\":\"test\",\"array\":[1,22,33,4444]}}";
        generatedMap = gson.fromJson(jsonStr, Map.class);
        System.out.println(generatedMap);
        generateJsonStr  = gson.toJson(generatedMap);
        System.out.println(generateJsonStr);
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

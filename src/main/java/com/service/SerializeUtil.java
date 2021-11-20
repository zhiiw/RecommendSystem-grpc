package com.service;

import java.io.*;

public class SerializeUtil {
    public static String serializeToString(Object obj) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream objOutput = new ObjectOutputStream(buf);
        objOutput.writeObject(obj);
        return buf.toString();
    }

    public static Object deserializeToObj(String str) throws Exception{
        ByteArrayInputStream buf = new ByteArrayInputStream(str.getBytes());
        ObjectInputStream objInput = new ObjectInputStream(buf);
        return objInput.readObject();
    }
}

package com.example.demo;


import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.io.UnsupportedEncodingException;

public class MyZkSerializer  implements ZkSerializer {
    @Override
    public byte[] serialize(Object o) throws ZkMarshallingError {
        String d = (String)o;
        try {
            return d.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) throws ZkMarshallingError {
        try {
            return new String(bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

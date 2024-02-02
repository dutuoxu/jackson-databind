package com.fasterxml.my;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.my.demo.MyEntity;


public class Tests {



    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MyEntity my = new MyEntity("!111",22);
        String str = mapper.writeValueAsString(my);
        System.out.println(str);
    }
}

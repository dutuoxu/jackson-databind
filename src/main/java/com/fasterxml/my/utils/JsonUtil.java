package com.fasterxml.my.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
	public static final TypeReference<Map<String, String>> TYPEREF_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {};
	public static final TypeReference<Map<String, Object>> TYPEREF_MAP_STRING_OBJECT = new TypeReference<Map<String, Object>>() {};
	public static final TypeReference<List<Map<String, String>>> TYPEREF_LIST_MAP_STRING_STRING = new TypeReference<List<Map<String, String>>>() {};
	public static final TypeReference<List<Map<String, Object>>> TYPEREF_LIST_MAP_STRING_OBJECT = new TypeReference<List<Map<String, Object>>>() {};

	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * 将对象转为 json 字符串
	 * @param obj
	 * @return
	 */
	public static String stringify(Object obj) {
		if (obj == null) {
			return null;
		}
		
		try {
			String str = mapper.writeValueAsString(obj);
			return str;
		} catch (IOException e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
	/**
	 * 将对象转为 json 字符串（格式化后）
	 * @param obj
	 * @return
	 */
	public static String stringifyPretty(Object obj) {
		if (obj == null) {
			return null;
		}
		
		try {
			String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			return str;
		} catch (IOException e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
	/**
	 * 将 json 字符串转为指定类型的对象
	 * @param json
	 * @param toValueType
	 * @return
	 */
	public static <T> T parse(String json, Class<T> toValueType) {
		try {
			JsonNode node = mapper.readTree(json);
			T object = mapper.convertValue(node, toValueType);
			return object;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 将 json 字符串转为指定引用类型的对象，如 Map,List,Set 等
	 * @param json
	 * @param toValueTypeRef
	 * @return
	 */
	public static <T> T parse(String json, TypeReference<T> toValueTypeRef) {
		try {
			JsonNode node = mapper.readTree(json);
			T object = mapper.convertValue(node, toValueTypeRef);
			return object;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 将 json 字符串转为引用类型 {@code Map<String, Object>} 的对象
	 * @param json
	 * @param toValueTypeRef
	 * @return
	 */
	public static Map<String, Object> parseMap(String json) {
		if(json==null|| json.isEmpty()){
			return new HashMap<>();
		}
		try {
			JsonNode node = mapper.readTree(json);
			Map<String, Object> map = mapper.convertValue(node, TYPEREF_MAP_STRING_OBJECT);
			return map;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 将 json 字符串转为引用类型 {@code Map<String, String>} 的对象
	 * @param json
	 * @param toValueTypeRef
	 * @return
	 */
	public static Map<String, String> parseStrValMap(String json) {
		try {
			JsonNode node = mapper.readTree(json);
			Map<String, String> map = mapper.convertValue(node, TYPEREF_MAP_STRING_STRING);
			return map;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 将 json 字符串转为指定类型对象的列表
	 * @param <T>
	 * @param json
	 * @param itemType
	 * @return
	 */
	public static <T> List<T> parseList(String json, Class<T> itemType) {
		try {
			JsonNode node = mapper.readTree(json);
			List<Map<String, Object>> list = mapper.convertValue(node, TYPEREF_LIST_MAP_STRING_OBJECT);
			List<T> targetList = new ArrayList<>();
			for (Map<String, Object> map : list) {
				targetList.add(JsonUtil.convert(map, itemType));
			}
			return targetList;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static <T> T convert(Object fromValue, Class<T> toValueType) {
		try {
			T target = mapper.convertValue(fromValue, toValueType);
			return target;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static <T> T convert(Object fromValue, TypeReference<T> toValueTypeRef) {
		try {
			T target = mapper.convertValue(fromValue, toValueTypeRef);
			return target;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * 对象列表类型转换
	 * @param fromList
	 * @param itemType
	 * @return
	 */
	public static <T> List<T> convertList(List<?> fromList, Class<T> itemType) {
		try {
			List<Map<String, Object>> list = mapper.convertValue(fromList, TYPEREF_LIST_MAP_STRING_OBJECT);
			List<T> targetList = new ArrayList<>();
			for (Map<String, Object> map : list) {
				targetList.add(JsonUtil.convert(map, itemType));
			}
			return targetList;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	
	public static String  toParam(String str ,String type) {
		if(str==null|| str.isEmpty()){
			str=",null";
			return str;
		}
		if(type.equals("int")){
			str =","+str;
		}else{
			str =",'"+str+"'";
		}
		return str;
		
	}

	public static <T> T itemConver(String jsonStr, Class<T> toValueType) {
		try {
			JsonNode node = null;
			try {
				node = mapper.readTree(jsonStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			T object = mapper.convertValue(node.get("item"), toValueType);
			return object;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static Map<String, Object> objectToMap(Object obj, boolean nullToEmpty) throws Exception {    
        if(obj == null){    
            return null;    
        }   
  
        Map<String, Object> map = new HashMap<>();    
  
        Field[] declaredFields = obj.getClass().getDeclaredFields();    
        for (Field field : declaredFields) { 
            field.setAccessible(true);  
            map.put(field.getName(), nullToEmpty == false ? field.get(obj) : field.get(obj) == null ? "":field.get(obj));  
        }    
  
        return map;  
    }
	
	public static <T> List<T> objectConverList(String jsonStr , Class<T> itemType) {
		try {
			JsonNode node = null;
			String className = itemType.getSimpleName();
		    char[] chars = className.toCharArray();
	        //首字母小写方法，大写会变成小写，如果小写首字母会消失
		    if(chars[0] >= 65 && chars[0] <= 90) {
		    	chars[0] +=32;
		    }
	        className = String.valueOf(chars);
			try {
				node = mapper.readTree(jsonStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<Map<String, Object>> listMap =new ArrayList<>();
			for(int i = 0 ; node.size()>i;i++){
				Map<String, Object> map = mapper.convertValue(node.get(i),TYPEREF_MAP_STRING_OBJECT);
				listMap.add(map);
			}
			List<T> targetList = new ArrayList<>();
			for (Map<String, Object> map : listMap) {
				targetList.add(JsonUtil.convert(map, itemType));
			}
			return targetList;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

}

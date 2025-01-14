# ObjectMapper的writeValueAsString方法
## protected final void _writeValueAndClose(JsonGenerator g, Object value)
## com.fasterxml.jackson.databind.ser.DefaultSerializerProvider的public void serializeValue(JsonGenerator gen, Object value)方法
### com.fasterxml.jackson.databind.SerializerProvider的public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType,boolean cache, BeanProperty property)方法
### com.fasterxml.jackson.databind.SerializerProvider的protected JsonSerializer<Object> _createAndCacheUntypedSerializer(Class<?> rawType)方法
### com.fasterxml.jackson.databind.ser.BeanSerializerFactory的createSerializer(SerializerProvider prov,JavaType origType)方法
### com.fasterxml.jackson.databind.ser.BeanSerializerFactory的protected JsonSerializer<?> _createSerializer2(SerializerProvider prov,JavaType type, BeanDescription beanDesc, boolean staticTyping)方法
### com.fasterxml.jackson.databind.ser.BasicSerializerFactory的protected final JsonSerializer<?> findSerializerByAnnotations(SerializerProvider prov,JavaType type, BeanDescription beanDesc)方法
### com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector的public AnnotatedMember getJsonValueAccessor()方法
### com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector的protected void collectAll()方法
### com.fasterxml.jackson.databind.ser.BeanSerializerFactory 的protected JsonSerializer<Object> constructBeanOrAddOnSerializer(SerializerProvider prov,JavaType type, BeanDescription beanDesc, boolean staticTyping)
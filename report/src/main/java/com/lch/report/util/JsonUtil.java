/**
 * 
 * <p>Title��</p>
 * 
 * <p>Description:</p>
 * 
 * <p>Copyright: Copyright (c) 2011</p>
 * 
 * <p>Company: ������Ѷ�Ƽ���www.enersun.cn</p>
 * 
 * <p>project:KM3.0
 *
 * <p>Author: ��Ӧ��</p>
 * 
 * <p>Email: </p>
 * 
 * <p>Version: 1.0</p>
 * 
 * <p>Create Date:2014-4-13-����01:41:54</p>
 *
 */
package com.lch.report.util;

import java.io.IOException;
import java.io.StringWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static String beanToJson(Object obj) {

		StringWriter writer = new StringWriter();

		try {
			JsonGenerator gen = new JsonFactory().createJsonGenerator(writer);
			objectMapper.writeValue(gen, obj);

			gen.close();

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return writer.toString();
	}
	public static <T> T jsonToBean(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {

	}
}

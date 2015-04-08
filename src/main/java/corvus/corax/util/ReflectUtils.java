/**
 * Copyright (c) 2010-2014 Corvus Corax Entertainment
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Corvus Corax Entertainment nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package corvus.corax.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author Vlad Ravenholm
 *
 */
public class ReflectUtils {

	public synchronized static Field[] getFieldsWithAnnotation(Class<? extends Annotation> type, Class<?> obj) {
		Field[] flds = obj.getDeclaredFields();
		
		ArrayList<Field> fields = new ArrayList<Field>();
		
		for (Field field : flds) {
			
			if(field.isAnnotationPresent(type)) {
				field.setAccessible(true);
				fields.add(field);
			}
		}
		
		if(obj.getSuperclass() != null) {
			Field[] flds2 = getFieldsWithAnnotation(type, obj.getSuperclass());
			
			if(flds2.length > 0) {
				for (int i = 0; i < flds2.length; i++) {
					fields.add(flds2[i]);
				}
			}
		}
		
		flds = fields.toArray(new Field[fields.size()]);
		return flds;
	}

	public synchronized static Method[] getMethodsWithAnnotation(Class<? extends Annotation> type, Class<?> obj) {
		return getMethodsWithAnnotation(type, obj, false);
	}
	
	public synchronized static Method[] getMethodsWithAnnotation(Class<? extends Annotation> type, Class<?> obj, boolean includeInterfaces) {
		Method[] meths = obj.getDeclaredMethods();
		
		ArrayList<Method> methods = new ArrayList<Method>();
		
		for (Method field : meths) {
			
			if(field.isAnnotationPresent(type)) {
				field.setAccessible(true);
				methods.add(field);
			}
		}
		
		if(obj.getSuperclass() != null) {
			Method[] flds2 = getMethodsWithAnnotation(type, obj.getSuperclass(), includeInterfaces);
			
			if(flds2.length > 0) {
				for (int i = 0; i < flds2.length; i++) {
					methods.add(flds2[i]);
				}
			}
		}
		
		if(includeInterfaces) {
			Class<?>[] interfs = obj.getInterfaces();
			
			for (int i = 0; i < interfs.length; i++) {
				Class<?> interf = interfs[i];
				
				// for now, no recursion
				Method[] flds2 = getMethodsWithAnnotation(type, interf);

				if(flds2.length > 0) {
					for (int j = 0; j < flds2.length; j++) {
						methods.add(flds2[j]);
					}
				}
			}
		}

		meths = methods.toArray(new Method[methods.size()]);
		return meths;
	}
	
	/**
	 * @param type
	 * @param objClass <br>
	 * Will return the first field.
	 */
	public synchronized static Field getFieldWithAnnotation(Class<? extends Annotation> type, Class<?> objClass) {
		Field[] flds = objClass.getDeclaredFields();
		
		
		for (Field field : flds) {
			
			if(field.isAnnotationPresent(type)) {
				field.setAccessible(true);
				return field;
			}
		}
		
		return null;
	}

	public static boolean annotationPresent(String memberName, Class<? extends Annotation> type, ElementType elemType, Class<?> objClass, Class<?>... paramTypes) throws Exception {
		
		if(objClass == null)
			return false;
		
		switch (elemType) {
			case FIELD:
				return objClass.getDeclaredField(memberName).isAnnotationPresent(type);
			case TYPE:
				return objClass.isAnnotationPresent(type);
			case METHOD:
				return objClass.getDeclaredMethod(memberName, paramTypes).isAnnotationPresent(type);
		}
		
		
		return false;
	}
}

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
package corvus.corax.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import corvus.corax.Corax;
import corvus.corax.CoraxProcessor;
import corvus.corax.Describer;
import corvus.corax.util.ReflectUtils;

/**
 * @author Vlad
 *
 */
public class InjectProcessor implements CoraxProcessor {
	private static final Logger log = Logger.getLogger(InjectProcessor.class.getName());
	
	@Override
	public void process(Describer describer, Corax corax) {
		try { // Inject annotations
			Object obj = describer.value;
			
			Field[] fields = ReflectUtils.getFieldsWithAnnotation(Inject.class, obj.getClass());
			
			for(Field field : fields) {
				
				Annotation[] annos = field.getAnnotations();

				if(annos.length > 1) {
					for (int i = 0; i < annos.length; i++) {
						Annotation paraAnno = annos[i];
	
						if(paraAnno.annotationType() == Inject.class)
							continue;
						
						Object getter = paraAnno.annotationType();
	
						if(paraAnno instanceof Named)
							getter = paraAnno;
						
						Object dep = corax.getDependency(getter);
						if(dep != null) {
							field.set(obj, dep);
							break;
						} // TODO: Maybe a warning for nulls ?
					}
				}
				else {
					Object dependancy = corax.getDependency(field.getType());
					
					if(dependancy != null) { // injectzor
						field.set(obj, dependancy);
					} // TODO: Maybe a warning for nulls ?
				}
			}
			
			Method[] meths = ReflectUtils.getMethodsWithAnnotation(Inject.class, obj.getClass(), true);
			
			for(Method meth : meths) {
				
				Class<?>[] types = meth.getParameterTypes();
				Object[] rezult = new Object[types.length];
				Annotation[][] annons = meth.getParameterAnnotations();
				
				for (int i = 0; i < types.length; i++) {
					
					if(annons[i].length > 0)  {
						Annotation[] paraAnnos = annons[i];
						
						for (int j = 0; j < paraAnnos.length; j++) {
							Annotation paraAnno = paraAnnos[j];

							Object getter = paraAnno.annotationType();

							if(paraAnno instanceof Named)
								getter = paraAnno;
							
							Object dep = corax.getDependency(getter);
							rezult[i] = dep;
						}
					}
					else {
						Class<?> type = types[i];
						Object dependancy = corax.getDependency(type);
						
						// Atm we include nulls also, unlike fields some methods might read a null
						rezult[i] = dependancy;
					}
				}

				meth.invoke(obj, rezult);
			}
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Faild processing Injection annotations.", e);
		}
	}

	@Override
	public boolean isInitializer() {
		return false;
	}

}

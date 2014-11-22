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
package corvus.corax.processing;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import corvus.corax.Corax;
import corvus.corax.Describer;
import corvus.corax.annotations.Inject;

/**
 * @author Vlad
 *
 */
public class ConstructorInitializer implements CoraxProcessor {

	private static final Logger log = Logger.getLogger(ConstructorInitializer.class.getName());
	
	@Override
	public void process(Describer describer, Corax engine) {
		Class<?> target = describer.target;
		
		try {
			Constructor<?>[] cons = target.getDeclaredConstructors();

			int constructors = 0;
			Constructor<?> use = null, def = null;
			for (int i = 0; i < cons.length; i++) {
				Constructor<?> con = cons[i];
				
				if(con.isAnnotationPresent(Inject.class)) {
					if(con.getParameterTypes().length == 0)
						def = con;

					use = con;
					constructors++;
				}
			}
			
			if(constructors > 1) {
				if(def != null) {
					log.warning("Multiple constructors with inject annotations in class["+target.getSimpleName()+"], using paramitereless constructor.");
					use = def;
				}
				else {
					log.log(Level.WARNING, "Multiple constructors with inject annotations in class["+target.getSimpleName()+"]. No solution found!", new RuntimeException("No fitting constructo!"));
					return;
				}
			}
			
			if (use != null) {// else try on another
				describer.value = use.newInstance(engine.getDependencies(use.getParameterTypes()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isInitializer() {
		return true;
	}
}

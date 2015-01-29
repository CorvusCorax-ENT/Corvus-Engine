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
package corvus.corax.config;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigFieldSubscriber {
	private final Field field;
	private final Object parent;

	public ConfigFieldSubscriber(Field field, Object parent) {
		this.field = field;
		this.parent = parent;
	}

	/**
	 * TODO: Set to default
	 */
	public void unset() {
		try {
			if(field.get(parent) instanceof Number)
				set(0);
			else if(field.get(parent) instanceof Boolean)
				set(false);
			else
				set(null);
		}
		catch(Exception e) {
			Logger log = Logger.getLogger(getClass().getName());
			log.log(Level.SEVERE, "Failed unsetting subscriber field["+field.getName()+"-"+parent.getClass().getSimpleName()+"]!", e);
		}
	}

	public void set(Object value) {
		try {
			field.set(parent, value);
		}
		catch(Exception e) {
			Logger log = Logger.getLogger(getClass().getName());
			log.log(Level.SEVERE, "Failed setting subscriber field["+field.getName()+"-"+parent.getClass().getSimpleName()+"]!", e);
		}
	}
}
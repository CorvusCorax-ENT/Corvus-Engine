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
package corvus.corax;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import corvus.corax.annotation.Singleton;

/**
 * @author Vlad
 *
 */
public abstract class CoraxBuilder {
	
	protected ArrayList<Describer> describers = new ArrayList<Describer>();
	
	private Describer describer;
	private Scope defaultScope;
	
	protected abstract void build(Corax corax);

	/**
	 * @param defaultScope the defaultScope to set
	 */
	public void setDefaultScope(Scope defaultScope) {
		this.defaultScope = defaultScope;
	}
	
	/**
	 * Clears any set default for the specific build segment
	 */
	public void clearDefaults() {
		defaultScope = null;
	}
	
	protected void clean(ArrayList<Describer> descs) {
		if(describers != null)
			describers.clear();

		describer = null;
	}
	
	protected final void begin(Corax corax) {
		describers = new ArrayList<Describer>();
		describer = null;
		build(corax);
		flush(corax);
	}
	
	protected final void end() {
		ready();
		describers.clear();
		describer = null;
		describers = null;
	}

	public void flush(Corax corax) {
		if(describer != null && !corax.isBinded(describer.key)) {
			completePreviousBind();
			describer = null;
		}
	}
	
	protected CoraxBuilder bind(Class<?> key) {
		completePreviousBind();
		describer = new Describer(this, key, key, null);
		return this;
	}
	
	private void completePreviousBind() {
		if(describer != null && describer.isValid()) {

			if (defaultScope != null && describer.scope == null)
				describer.scope = defaultScope;
			else if (describer.scope == null)
				describer.scope = Scope.Default;

			try {
				if(describer.key != null) {
					Singleton anon = null;
					
					if(describer.key.isAnnotationPresent(Singleton.class)) {
						anon = describer.key.getAnnotation(Singleton.class);
					}
					else if(describer.target.isAnnotationPresent(Singleton.class)) {
						anon = describer.target.getAnnotation(Singleton.class);
					}
					
					if(anon != null) {
						describer.key.isAnnotationPresent(Singleton.class);
						describer.scope = anon.eager() ? Scope.EagerSingleton : Scope.Singleton;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			describers.add(describer);
			describer = null;
		}
	}
	
	public CoraxBuilder to(Class<?> target) {
		if(describer != null) {
			
			if(describer.key.isAssignableFrom(target))
				describer.target = target;
			else {
				String k = describer.key.getSimpleName();
				String t = target.getSimpleName();
				throw new RuntimeException("Failed binding "+t+" to "+k+". "+t+" dose not implement "+k+"!");
			}
		}
				
		return this;
	}

	public CoraxBuilder to(Instructor instructor) {
		if(describer != null)
			describer.value = instructor;
		return this;
	}

	public CoraxBuilder bindConstant(Object cons) {
		completePreviousBind();
		
		describer = new Describer(this, null, null, defaultScope != null ? defaultScope : Scope.Default);
		describer.value = cons;
		return this;
	}
	
	/**
	 * This method dose not bind! Calling it before a bind was completed will throw a {@link RuntimeException}
	 * @param cons
	 * @return
	 */
	public CoraxBuilder constant(Object cons) {
		if(describer != null && (describer.key != null && describer.key.isInstance(cons))) {
			describer.value = cons;
			describer.scope = Scope.Singleton;
		}
		else if(describer.key == null) {
			throw new RuntimeException("constant() was called on a non completed bind!");
		}

		return this;
	}
	
	public void as(Scope scope) {
		if(describer != null) {
			describer.scope = scope;
		}
	}

	public CoraxBuilder annotatedWith(Annotation annotationType) {
		if(describer != null) {
			describer.annotation = annotationType;
		}
		return this;
	}

	public CoraxBuilder annotatedWith(Class<? extends Annotation> annotationType) {
		if(describer != null) {
			describer.annotationType = annotationType;
		}
		return this;
	}

	/**
	 * Triggered when all the bindings are done. <br>
	 * The builder is ready for anything o/
	 */
	public void ready() { }
	
}

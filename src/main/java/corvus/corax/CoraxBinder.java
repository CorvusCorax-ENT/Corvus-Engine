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

/**
 * @author Vlad
 *
 */
public abstract class CoraxBinder {
	
	protected ArrayList<Describer> describers = new ArrayList<Describer>();
	
	private Describer describer;
	
	protected abstract void bind(Corax corax);

	protected void clean() {
		describers.clear();
		describer = null;
	}
	
	protected final void begin(Corax corax) {
		describers = new ArrayList<Describer>();
		describer = null;
		bind(corax);
		flush(corax);
	}
	
	protected final void end() {
		describers.clear();
		describer = null;
		describers = null;
	}

	public void flush(Corax corax) {
		if(describer != null && !corax.isBinded(describer.key)) {
			describers.add(describer);
			describer = null;
		}
	}
	
	protected CoraxBinder bind(Class<?> key) {
		if(describer != null && describer.isValid())
			describers.add(describer);
		
		describer = new Describer(this, key, null, Scope.Default);
		
		return this;
	}
	
	public CoraxBinder to(Class<?> target) {
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

	public CoraxBinder to(Instructor instructor) {
		if(describer != null)
			describer.value = instructor;
		return this;
	}

	public CoraxBinder constant(Object cons) {
		if(describer != null && describer.key.isInstance(cons))
			describer.value = cons;

		return this;
	}

	public CoraxBinder annotatedWith(Annotation annotationType) {
		return this;
	}

	public CoraxBinder annotatedWith(Class<? extends Annotation> annotationType) {
		return this;
	}

}

/**
 * Copyright (c) 2013-2014 Corvus Corax Entertainment
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

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import corvus.corax.CoraxDependency.MemberType;
import corvus.corax.initialize.ConstructorInitializer;
import corvus.corax.inject.InjectProcessor;
import corvus.corax.provide.ProvideProcessor;

/**
 * @author Vlad Ravenholm
 * All these collections... meh, later
 */
public class Corax {

	private static final Logger log = Logger.getLogger(Corax.class.getName());

	private static Corax instance;
	
	private final HashMap<Class<?>, Describer> binds = new HashMap<>();
	private final HashMap<Class<?>, CoraxDependency> dependency = new HashMap<>();
	
	private final ArrayList<CoraxProcessor> processors = new ArrayList<>();
	private final ArrayList<CoraxBinder> binders = new ArrayList<>();
	
	private Corax(CoraxBinder... binders) {
		log.info("Initializing corvus engine 1.5.0");
		// not sure about this
		processors.add(new ConstructorInitializer()); // always first

		processors.add(new InjectProcessor());
		processors.add(new ProvideProcessor());

		addBinders(binders);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type) {
		Describer describer = binds.get(type);
		T obj = (T) describer.value;
		
		if(describer.scope == Scope.Singleton || describer.scope == Scope.EagerSingleton) {
			if(describer.value == null) {
				initialize(describer);
				obj = (T) describer.value;
			}
		}
		else if(describer.target != null) {
			initialize(describer);
			obj = (T) describer.value;
		}
		else if(describer.value instanceof Instructor) {
			Instructor ret = (Instructor) describer.value;
			obj = (T) ret.design();
		}
		
		if(obj == null) {
			log.warning("Did not initialize "+type.getSimpleName()+"!");
			return null;
		}
		
		// And we're done
		process(describer);
		
		return obj;
	}

	public Describer getDescriber(Class<?> type) {
		Describer describer = binds.get(type);
		return describer;
	}

	public synchronized void addBinders(CoraxBinder... binders) {
		addBinders(true, binders);
	}

	public synchronized void addBinders(boolean saveBinder, CoraxBinder... binders) {
		for (int i = 0; i < binders.length; i++) {
			CoraxBinder binder = binders[i];
			
			binder.begin(this);
			
			for (int j = 0; j < binder.describers.size(); j++) {
				Describer describer = binder.describers.get(j);
				
				binds.put(describer.key, describer);
				if(describer.scope == Scope.EagerSingleton)
					getInstance(describer.key);
			}
			
			this.binders.add(binder);
			binder.end();
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public CoraxBinder getBinder(Class<? extends CoraxBinder> type) {
		for (int i = 0; i < binders.size(); i++) {
			CoraxBinder binder = binders.get(i);
			
			if(type.isInstance(binder))
				return binder;
		}
		
		return null;
	}

	public boolean isBinded(Class<?> key) {
		return binds.containsKey(key);
	}
	
	public synchronized void addDependency(Class<?> clstype, MemberType type, Object owner, Member member) {
		dependency.put(clstype, new CoraxDependency(owner, clstype, type, member));
	}
	
	public Object[] getDependencies(Class<?>... types) throws Exception {
		ArrayList<Object> data = new ArrayList<Object>();

		for (int i = 0; i < types.length; i++)
			data.add(getDependency(getClass()));
		
		return data.toArray();
	}
	
	public Object getDependency(Class<?> type) throws Exception {
		
		if(binds.containsKey(type))
			return getInstance(type);
		
		CoraxDependency dep = dependency.get(type);
		
		if(dep == null) {
			log.warning("Dependency["+type.getSimpleName()+"] could not be resolved! Either the Dependency is not mapped yet, or its not at all.");
			return null;
		}
		
		return dep.getInstance();
	}
	
	public void process(Describer describer) {
		for (int i = 0; i < processors.size(); i++) {
			CoraxProcessor prc = processors.get(i);

			if(!prc.isInitializer())
				prc.process(describer, this);
		}
	}
	
	public void initialize(Describer describer) {
		for (int i = 0; i < processors.size(); i++) {
			CoraxProcessor prc = processors.get(i);

			if(prc.isInitializer()) {
				prc.process(describer, this);
			
				if(describer.value != null)
					break;
			}
		}
	}
	
	public void destroy() {
		for (int i = 0; i < binders.size(); i++)
			binders.get(i).clean();

		processors.clear();
		dependency.clear();
		binders.clear();
		binds.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeBinders(Class<? extends CoraxBinder>... binders) {
		for (int i = 0; i < binders.length; i++) {
			Class<? extends CoraxBinder> type = binders[i];
			
			CoraxBinder binder = getBinder(type);
			
			if(binder != null)
				removeBinder(binder);
		}
	}

	public synchronized void removeBinders(CoraxBinder... binders) {
		for (int i = 0; i < binders.length; i++) {
			CoraxBinder binder = binders[i];
			removeBinder(binder);
		}
	}

	public synchronized void removeBinder(CoraxBinder binder) {
		ArrayList<Describer> descs = new ArrayList<>();
		
		// purge dependency
		for(Describer desc : binds.values()) {
			if(desc.binder == binder) 
				descs.add(desc);
		}
		
		for (int j = 0; j < descs.size(); j++) {
			Describer desc = descs.get(j);
			
			binds.remove(desc.key);
			purgeDependency(desc);
		}
		
		binder.clean();
		this.binders.remove(binder);
	}

	public void purgeDependency(Describer describer) {
		ArrayList<CoraxDependency> deps = new ArrayList<>();

		for (CoraxDependency dep : dependency.values()) {
			if(dep.isOwner(describer.value)) 
				deps.add(dep);
		}

		for (int j = 0; j < deps.size(); j++) {
			CoraxDependency dep = deps.get(j);
			dependency.remove(dep.getTarget());
		}
	}
	
	public static <T> T fetch(Class<T> type) {
		return instance.getInstance(type);
	}
	
	public static void processMembers(Object object) {
		instance.process(new Describer(null, object.getClass(), object.getClass(), Scope.Default));
	}
	
	public static synchronized Corax Install(CoraxBinder... binders) {
		if(instance != null)
			instance.destroy();
			
		return instance = new Corax(binders);
	}

	public static Corax instance() {
		return instance;
	}
}

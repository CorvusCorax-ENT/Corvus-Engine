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
import java.util.logging.Level;
import java.util.logging.Logger;

import corvus.corax.CoraxDependency.MemberType;
import corvus.corax.config.ConfigProcessor;
import corvus.corax.config.CorvusConfig;
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
	private final HashMap<Object, CoraxDependency> dependency = new HashMap<>();
	
	private final ArrayList<CoraxProcessor> processors = new ArrayList<>();
	private final ArrayList<CoraxBuilder> builders = new ArrayList<>();
	
	private CorvusConfig config;
	
	private Corax() {
		log.info("Initializing corvus engine 1.5.0");

		//XXX not sure about this, but for now its gonna be hard coded
		processors.add(new ConstructorInitializer()); // always first

		processors.add(new ConfigProcessor());
		processors.add(new InjectProcessor());
		processors.add(new ProvideProcessor());
	}

	/**
	 * Adds a processor as the last element in the processor list
	 * @param coraxProcessor
	 */
	public void addProcessor(CoraxProcessor coraxProcessor) {
		if(coraxProcessor == null || processors.contains(coraxProcessor))
			return;
		
		processors.add(coraxProcessor);
	}

	@SuppressWarnings("unchecked")
	public <T> T getInstance(Class<T> type) {
		Describer describer = binds.get(type);
		try {
			T obj = (T) describer.value;
			
			if(describer.scope == Scope.Singleton || describer.scope == Scope.EagerSingleton) {
				if(describer.value == null) {
					initialize(describer);
					obj = (T) describer.value;
					process(describer);
				}
				
				return obj;
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
				log.warning("Did not initialize "+type.getSimpleName()+" as scope "+describer.scope+"!");
				return null;
			}
			
			// And we're done
			process(describer);
			return obj;
		}
		catch(Exception e) {
			log.log(Level.SEVERE, "Failed to initialize "+type.getSimpleName()+"!", e);
			throw e;
		}
	}

	public Describer getDescriber(Class<?> type) {
		Describer describer = binds.get(type);
		return describer;
	}

	public synchronized void addBuilders(CoraxBuilder... builders) {
		addBuilders(true, builders);
	}

	public synchronized void addBuilders(boolean saveBuilder, CoraxBuilder... builders) {
		for (int i = 0; i < builders.length; i++) {
			CoraxBuilder builder = builders[i];
			
			builder.begin(this);
			
			for (int j = 0; j < builder.describers.size(); j++) {
				Describer describer = builder.describers.get(j);
				
				if(describer.key == null) {
					dependency.put(describer.annotation == null ? describer.annotationType : describer.annotation,
							new CoraxDependency(describer.value));
					continue;
				}
				
				binds.put(describer.key, describer);
				if(describer.scope == Scope.EagerSingleton)
					getInstance(describer.key);
			}
			
			this.builders.add(builder);
			builder.end();
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public CoraxBuilder getBuilder(Class<? extends CoraxBuilder> type) {
		for (int i = 0; i < builders.size(); i++) {
			CoraxBuilder builder = builders.get(i);
			
			if(type.isInstance(builder))
				return builder;
		}
		
		return null;
	}

	public boolean isBinded(Class<?> key) {
		return binds.containsKey(key);
	}
	
	public synchronized void addDependency(Class<?> clstype, MemberType type, Object owner, Member member) {
		dependency.put(clstype, new CoraxDependency(owner, clstype, type, member));
	}
	
	public Object getDependency(Object type) throws Exception {
		
		if(binds.containsKey(type) && type instanceof Class<?>)
			return getInstance((Class<?>) type);
		
		CoraxDependency dep = dependency.get(type);
		
		if(dep == null) {
			log.warning("Dependency["+(type instanceof Class<?> ? ((Class<?>) type).getSimpleName() : type)+"] could not be resolved! Either the Dependency is not mapped yet, or its not at all.");
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
		for (int i = 0; i < builders.size(); i++) {
			removeBuilder(builders.get(i));
		}

		processors.clear();
		dependency.clear();
		builders.clear();
		binds.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeBuilders(Class<? extends CoraxBuilder>... builders) {
		for (int i = 0; i < builders.length; i++) {
			Class<? extends CoraxBuilder> type = builders[i];
			
			CoraxBuilder builder = getBuilder(type);
			
			if(builder != null)
				removeBuilder(builder);
		}
	}

	public synchronized void removeBuilders(CoraxBuilder... builders) {
		for (int i = 0; i < builders.length; i++) {
			CoraxBuilder builder = builders[i];
			removeBuilder(builder);
		}
	}

	public synchronized void removeBuilder(CoraxBuilder builder) {
		ArrayList<Describer> descs = new ArrayList<>();
		
		// purge dependency
		for(Describer desc : binds.values()) {
			if(desc.builder != null && desc.builder == builder) 
				descs.add(desc);
		}
		
		for (int j = 0; j < descs.size(); j++) {
			Describer desc = descs.get(j);
			
			binds.remove(desc.key);
			purgeDependency(desc);
		}
		
		for (int i = 0; i < builder.describers.size(); i++) {
			Describer des = builder.describers.get(i);
			
			if(des.key != null)
				continue;
			else if(des.annotation != null) {
				dependency.remove(des.annotation);
			}
			else if(des.annotationType != null) {
				dependency.remove(des.annotationType);
			}
		}
		
		builder.clean(descs);
		this.builders.remove(builder);
		
		descs.clear();
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
	
	public static synchronized Corax Install(CoraxBuilder... builders) {
		if(instance != null)
			instance.destroy();
			
		instance = new Corax();
		instance.addBuilders(builders);
		return instance;
	}

	public static CorvusConfig config() {
		
		if(instance == null) {
			log.log(Level.WARNING, "Requested config module when Corax is not initiated.", new RuntimeException("Corax not Initiated."));
			return null;
		}
		
		if(instance.config == null)
			instance.config = new CorvusConfig();
		
		return instance.config;
	}
	
	public static void process(Object obj) {
		process(obj, false);
	}
	
	public static void process(Object obj, boolean saveNew) {
		if(instance == null) {
			log.log(Level.WARNING, "Requested processing module when Corax is not initiated.", new RuntimeException("Corax not Initiated."));
			return;
		}
		
		if(obj instanceof Describer)
			instance.process((Describer)obj);
		else {
			Describer describer = new Describer(null, obj.getClass(), obj.getClass(), Scope.Singleton);
			describer.value = obj;
			instance.process(describer);
			
			if(saveNew && !instance.isBinded(describer.key))
				instance.binds.put(describer.key, describer);
			else
				describer.clean();
		}

	}
	
	public static Corax instance() {
		return instance;
	}

}

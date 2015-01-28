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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import corvus.corax.util.Tools;

/**
 * @author Vlad
 *
 */
public class CorvusConfig {
	private HashMap<String, Object> properties = new HashMap<>();
	
	public static File WorkingDirectory = new File(".");
	
	public void loadDirectory(String path) {
		try {
			File dir = new File(WorkingDirectory, path);

			if(dir.exists() && dir.isDirectory()) {
				File[] files = dir.listFiles();
				
				if(files != null) {
					for (int i = 0; i < files.length; i++) {
						File file = files[i];

						if(!file.isDirectory() && file.length() <= 10_000)
							load(new FileInputStream(files[i]));
					}
				} // else its empty, mby warn?
				
			} // else warn?
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load(String path, String... paths) {
		try {
			load(new FileInputStream(new File(WorkingDirectory, path)));
			
			for (int i = 0; i < paths.length; i++) {
				load(new FileInputStream(new File(WorkingDirectory, paths[i])));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void load(InputStream... streams) {
		Properties props = new Properties();
		for (int i = 0; i < streams.length; i++) {
			try (InputStream io = streams[0]) {
				props.load(io);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				for (Entry<Object, Object> e : props.entrySet())
					addProperty((String) e.getKey(), e.getValue());
				props.clear();
			}
			
		}
	}
	
	public void addProperty(String key, Object value) {
		Object old = properties.put(key, value);
		
		if(old != null) {
			// inform subscribers
		}
	}

	public void removeProperty(String key) {
		Object old = properties.remove(key);
		
		if(old != null) {
			// inform subscribers
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key, T defValue) {
		Class<?> type = defValue != null ? defValue.getClass() : null;
		
		if(!properties.containsKey(key)) {
			if(defValue instanceof String && type != String.class)
				return (T) Tools.parsePrimitiveTypes(type, (String) defValue);
			else {
				return defValue;
			}
		}

		Object result = properties.get(key);		

		//XXX: Once upon a time there was an extender module here..
		
		if(result instanceof String && type != String.class) {
			result = Tools.parsePrimitiveTypes(type, String.valueOf(result));
			properties.put(key, result); // Update so we wont reparse
		}

		return (T) result;
		
	}
}

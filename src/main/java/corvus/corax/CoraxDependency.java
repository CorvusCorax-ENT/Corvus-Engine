/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package corvus.corax;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * @author Seth
 * Legacy class. to remove
 */
public final class CoraxDependency {
	private final Object owner;
	private final Class<?> target;
	private final Member provider;

	private final MemberType type;
	
	public enum MemberType {
		Field,
		Method
	}

	public CoraxDependency(Object owner, Class<?> target, MemberType type, Member data) {
		this.owner = owner;
		this.target = target;
		this.provider = data;
		this.type = type;
	}

	public Object getInstance() throws Exception {
		
		switch (type)
		{
			case Field:
			{
				Field field = (Field)provider;
				return field.get(owner);
			}
			case Method:
			{
				Method meth = (Method)provider;
				return meth.invoke(owner);
			}
			default:
				break;
		}
		
		return null;
	}
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean isOwner(Object obj) {
		return obj == owner;
	}
	
	/**
	 * @return the type
	 */
	public MemberType getType() {
		return type;
	}
	
	public Class<?> getTarget() {
		return target;
	}
}

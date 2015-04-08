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
package corvus.corax.util;

/**
 * @author Vlad Ravenholm
 */
public final class Tools
{
	public static Object parsePrimitiveTypes(Object obj, String value)
	{
		String unTrimed = value;
		value = value.trim();
		try
		{
			if (obj.getClass() == Boolean.class || obj.getClass() == Boolean.TYPE)
				return Boolean.parseBoolean(value);
			else if (obj.getClass() == Byte.class || obj.getClass() == Byte.TYPE)
				return Byte.parseByte(value);
			else if (obj.getClass() == Double.class || obj.getClass() == Double.TYPE)
				return Double.parseDouble(value);
			else if (obj.getClass() == Float.class || obj.getClass() == Float.TYPE)
				return Float.parseFloat(value);
			else if (obj.getClass() == Integer.class || obj.getClass() == Integer.TYPE)
			{
				int pValue = 0;
				if (value.startsWith("0x"))
					pValue = Integer.decode(value);
				else
					pValue = Integer.parseInt(value);
				
				return pValue;
			}
			else if (obj.getClass() == Long.class || obj.getClass() == Long.TYPE)
				return Long.parseLong(value);
			else if (obj.getClass() == Short.class || obj.getClass() == Short.TYPE)
				return Short.parseShort(value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return unTrimed;
	}
	
	public static Object parsePrimitiveTypes(Class<?> type, String value)
	{
		String unTrimed = value;
		value = value.trim();
		try
		{
			if (type == Boolean.class || type == Boolean.TYPE)
				return Boolean.parseBoolean(value);
			else if (type == Byte.class || type == Byte.TYPE)
				return Byte.parseByte(value);
			else if (type == Double.class || type == Double.TYPE)
				return Double.parseDouble(value);
			else if (type == Float.class || type == Float.TYPE)
				return Float.parseFloat(value);
			else if (type == Integer.class || type == Integer.TYPE)
			{
				int pValue = 0;
				if (value.startsWith("0x"))
					pValue = Integer.decode(value);
				else
					pValue = Integer.parseInt(value);
				
				return pValue;
			}
			else if (type == Long.class || type == Long.TYPE)
				return Long.parseLong(value);
			else if (type == Short.class || type == Short.TYPE)
				return Short.parseShort(value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return unTrimed;
	}

	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		System.out.println(s);
	}

}

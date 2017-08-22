package huck.pasta2.viewvars.auto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import huck.pasta2.viewvars.ViewVarList;
import huck.pasta2.viewvars.ViewVarMap;

public class ViewVarCreator {
	private static String prefixName(String name, String prefix) {
		if( 0 == prefix.length() ) {
			return name;
		} else if( prefix.endsWith("_") ) {
			return prefix + name;
		} else {
			return prefix + name.substring(0,1).toUpperCase() + name.substring(1);
		}
	}
	private static void setObjectToMap(ViewVarMap map, ViewVarBean bean, String printType, String namePrefix) throws Exception {
		if( null == bean ) {
			return;
		}
		Method[] methods = bean.getClass().getMethods();
		for( Method m : methods ) {
			if( 0 < m.getParameterTypes().length ) {
				continue;
			}
			ViewVarConfig config = null;
			ViewVarConfigList configList = m.getAnnotation(ViewVarConfigList.class);
			if( null != configList ) {
				for( ViewVarConfig cfg : configList.config() ) {
					if( cfg.printType().equals(printType) ) {
						config = cfg;
						break;
					}
				}
			}
			if( null == config ) {
				ViewVarConfig cfg = m.getAnnotation(ViewVarConfig.class);
				if( null != cfg && cfg.printType().equals(printType) ) {
					config = cfg;
				}
			}
			if( null != config ) {
				String childPrintType = printType;
				if( 0 < config.subPrintType().length() ) {
					childPrintType = config.subPrintType();
				}
				try {
					String childName = prefixName(config.name(), namePrefix);
					if( ViewVarType.CHILD == config.type() || ViewVarType.INCLUDE == config.type() ) {
						Object childObj = m.invoke(bean);
						if( null == childObj ) {
							map.setChildValue(childName, childObj);
						} else if( childObj instanceof ViewVarList ) {
							map.setChildList(childName, (ViewVarList)childObj);
						} else if( childObj instanceof ViewVarMap ) {
							map.setChildMap(childName, (ViewVarMap)childObj);
						} else if( childObj instanceof Map<?,?> ) {
							map.setChildMap(childName, createFromMap((Map<?,?>)childObj, printType));
						} else if( childObj instanceof Iterable<?> ) {
							String listItemName = config.listItemName();
							if( null == listItemName || 0 == listItemName.length() ) {
								listItemName = childName;
							}
							map.setChildList(childName, createFromIterable(listItemName, (Iterable<?>)childObj, childPrintType));
						} else {
							if( ViewVarType.CHILD == config.type() ) {
								if( childObj instanceof ViewVarBean ) {
									map.setChildMap(childName, createFromObject((ViewVarBean)childObj, childPrintType));
								} else {
									map.setChildValue(childName, childObj);
								}
							} else if( ViewVarType.INCLUDE == config.type() ) {
								if( childObj instanceof ViewVarBean ) {
									setObjectToMap(map, (ViewVarBean)childObj, childPrintType, childName);
								} else {
									map.setChildValue(childName, childObj);
								}
							} 
						}
					} else if( ViewVarType.ATTRIBUTE == config.type() ){
						Object childObj = m.invoke(bean);
						map.setAttribute(childName, childObj);
					}
				} catch(InvocationTargetException ex) {
					throw (Exception)ex.getTargetException();
				}
			}
		}
	}
	public static ViewVarMap createFromObject(ViewVarBean bean, String printType) throws Exception {
		if( null == bean ) {
			return null;
		}
		ViewVarMap result = new ViewVarMap();
		setObjectToMap(result, bean, printType, "");
		return result;
	}

	public static ViewVarMap createFromMap(Map<?,?> map, String printType) throws Exception {
		ViewVarMap result = new ViewVarMap();
		for( Map.Entry<?,?> entry : map.entrySet() ) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if( null == value ) {
				result.setAttribute(key, null);
			} else if( value instanceof Collection<?> ) {
				result.setChildList(key, key, (Collection<?>)value, printType);
			} else if( value instanceof Map<?,?> ) {
				result.setChildMap(key, createFromMap((Map<?,?>)value, printType));
			} else if( value instanceof ViewVarMap ) {
				result.setChildMap(key, (ViewVarMap)value);
			} else if( value instanceof ViewVarList ) {
				result.setChildList(key, (ViewVarList)value);
			} else {
				if( value instanceof ViewVarBean ) {
					result.setChildMap(key, createFromObject((ViewVarBean)value, printType));
				} else {
					result.setChildValue(key, value);
				}
			}
		}
		return result;
	}

	public static ViewVarList createFromIterable(String listItemName, Iterable<?> collection, String printType) throws Exception {
		ViewVarList result = new ViewVarList(listItemName);
		for( Object obj : collection ) {
			if( null == obj ) {
				result.add(null);
			} else if( obj instanceof ViewVarList ) {
				result.add((ViewVarList)obj);
			} else if( obj instanceof ViewVarMap ) {
				result.add((ViewVarMap)obj);
			} else if( obj instanceof Map<?,?> ) {
				result.add(createFromMap((Map<?,?>)obj, printType));
			} else if( obj instanceof Iterable<?> ) {
				result.add(createFromIterable(listItemName, (Iterable<?>)obj, printType));
			} else {
				if( obj instanceof ViewVarBean ) {
					result.add(createFromObject((ViewVarBean)obj, printType));
				} else {
					result.add(obj);
				}
			}
		}
		return result;
	}
}

package huck.pasta2.viewvars;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import huck.pasta2.viewvars.auto.ViewVarBean;
import huck.pasta2.viewvars.auto.ViewVarCreator;

public class ViewVarMap {
	private static class ChildValue {
		private ChildValue(Object value) { this.value = value; }
		private Object value;
	}

	private static class AttributeValue {
		private AttributeValue(Object value) { this.value = value; }
		private Object value;
	}

	private LinkedHashMap<String, Object> propMap = new LinkedHashMap<String, Object>();


	public void setChildMap(String name, ViewVarMap obj) {
		propMap.put(name, obj);
	}

	public ViewVarMap setChildMap(String name, ViewVarBean obj, String printType) throws Exception {
		ViewVarMap result = ViewVarCreator.createFromObject(obj, printType);
		propMap.put(name, result);
		return result;
	}

	public void setChildList(String name, ViewVarList list) {
		propMap.put(name, list);
	}

	public ViewVarList setChildList(String name, String itemName, Iterable<?> collection, String printType) throws Exception {
		ViewVarList result = ViewVarCreator.createFromIterable(itemName, collection, printType);
		propMap.put(name, result);
		return result;
	}

	public void setChildValue(String name, Object value) {
		propMap.put(name, new ChildValue(value));
	}

	public void setAttribute(String name, Object value) {
		propMap.put(name, new AttributeValue(value));
	}
	
	public void remove(String name) {
		propMap.remove(name);
	}

	public ViewVarMap createChildMap(String name) {
		ViewVarMap obj = new ViewVarMap();
		propMap.put(name, obj);
		return obj;
	}

	public ViewVarList createChildList(String name, String itemName) {
		ViewVarList list = new ViewVarList(itemName);
		propMap.put(name, list);
		return list;
	}

	public Object get(String name) {
		Object value = propMap.get(name);
		if( value instanceof AttributeValue ) {
			return ((AttributeValue) value).value;
		} if( value instanceof ChildValue ) {
			return ((ChildValue) value).value;
		} else {
			return value;
		}
	}

	public boolean containsKey(String name) {
		return propMap.containsKey(name);
	}


	public static interface TraversalCallback {
		public void startList(String name, int count);
		public void endList();

		public void startMap(String name);
		public void endMap();

		public void meetAttribute(String name, Object value);
		public void meetChildValue(String name, Object value);
	}

	public void traversal(TraversalCallback callback, String rootName, boolean processNullAsEmpty) {
		traversal(callback, rootName, this, processNullAsEmpty);
	}

	private void traversal(TraversalCallback callback, String name, Object obj, boolean processNullAsEmpty) {
		if( null == obj ) {
			if( processNullAsEmpty ) {
				return;
			} else {
				callback.meetChildValue(name, null);
			}
		} else if( obj instanceof ViewVarMap ) {
			ViewVarMap varObj = (ViewVarMap)obj;
			callback.startMap(name);

			Set<String> keySet = varObj.propMap.keySet();
			for( String key : keySet ) {
				Object value = varObj.propMap.get(key);;
				traversal(callback, key, value, processNullAsEmpty);
			}
			callback.endMap();
		} else if( obj instanceof ViewVarList ) {
			ViewVarList varList = (ViewVarList)obj;
			callback.startList(name, varList.size());

			for( Map.Entry<String, Object> entry : varList.attributeEntrySet() ) {
				traversal(callback, entry.getKey(), new AttributeValue(entry.getValue()), processNullAsEmpty);
			}
			int size = varList.size();
			for( int i=0; i<size; i++ ) {
				traversal(callback, varList.getItemName(), varList.get(i), processNullAsEmpty);
			}
			callback.endList();
		} else if( obj instanceof AttributeValue ) {
			Object value = ((AttributeValue)obj).value;
			if( null == value && processNullAsEmpty ) {
				return;
			} else {
				callback.meetAttribute(name, value);
			}
		} else if( obj instanceof ChildValue ) {
			Object value = ((ChildValue)obj).value;
			if( null == value && processNullAsEmpty ) {
				return;
			} else {
				callback.meetChildValue(name, value);
			}
		} else {
			callback.meetChildValue(name, obj);
		}
	}
}

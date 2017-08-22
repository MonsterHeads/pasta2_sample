package huck.pasta2.viewvars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import huck.pasta2.viewvars.auto.ViewVarBean;
import huck.pasta2.viewvars.auto.ViewVarCreator;

public class ViewVarList implements Iterable<Object> {
	private LinkedHashMap<String, Object> attrValueMap = new LinkedHashMap<String, Object>();
	private ArrayList<Object> list = new ArrayList<Object>();

	private String itemName;
	public ViewVarList(String itemName) {
		this.itemName = itemName;
	}
	public String getItemName() {
		return itemName;
	}

	public void setAttribute(String name, Object value) {
		attrValueMap.put(name, value);
	}
	public Object getAttribute(String name) {
		return attrValueMap.get(name);
	}
	public boolean containsAttribute(String name) {
		return attrValueMap.containsKey(name);
	}
	public Set<Map.Entry<String, Object>> attributeEntrySet() {
		return attrValueMap.entrySet();
	}

	public ViewVarMap createChildMap() {
		ViewVarMap obj = new ViewVarMap();
		list.add(obj);
		return obj;
	}

	public ViewVarList createChildList(String itemName) {
		ViewVarList list = new ViewVarList(itemName);
		list.add(list);
		return list;
	}

	public int add(ViewVarBean obj, String printType) throws Exception {
		return add(ViewVarCreator.createFromObject(obj, printType));
	}

	public int add(String itemName, Iterable<?> collection, String printType) throws Exception {
		return add(ViewVarCreator.createFromIterable(itemName, collection, printType));
	}

	public int add(Object obj) {
		list.add(obj);
		return list.size()-1;
	}

	public int size() {
		return list.size();
	}

	public Object get(int idx) {
		return list.get(idx);
	}

	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}
}

package huck.pasta2.sample.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import huck.pasta2.viewvars.ViewVarMap;
import huck.pasta2.viewvars.ViewVarMap.TraversalCallback;


public class JSONViewUtils {
	public static JSONObject convertViewVarMapToJSON(ViewVarMap viewVars) {
		ViewVarsCallback callback = new ViewVarsCallback();		
		viewVars.traversal(callback, "nothing", true);
		return (JSONObject)callback.root;
	}
	
	private static class ViewVarsCallback implements TraversalCallback {
		private Object root;
		private ArrayList<Object> objStack = new ArrayList<Object>();
		
		@Override
		public void startMap(String name) {
			JSONObject newObj = new JSONObject();
			if( 0 < objStack.size() ) {
				Object parent = objStack.get(objStack.size()-1);
				if( parent instanceof JSONObject ) {
					try {
						((JSONObject)parent).put(name, newObj);
					} catch (JSONException e) {
					}
				} else if( parent instanceof JSONArray ) {
					((JSONArray)parent).put(newObj);
				} else {
					try {
						throw new Exception();
					} catch (Exception e) {
					}
				}
			} else {
				root = newObj;
			}
			objStack.add(newObj);
		}
		
		@Override
		public void endMap() {
			objStack.remove(objStack.size()-1);
		}
		
		@Override
		public void meetAttribute(String name, Object value) {
			Object parent = objStack.get(objStack.size()-1);
			if( parent instanceof JSONObject ) {
				try {
					((JSONObject)parent).put(name, value);
				} catch (JSONException e) {
				}
			} else if( parent instanceof JSONArray ) {
				try {
					throw new Exception();
				} catch (Exception e) {
				}
			} else {
				try {
					throw new Exception();
				} catch (Exception e) {
				}				
			}
		}
		@Override
		public void meetChildValue(String name, Object value) {
			Object parent = objStack.get(objStack.size()-1);
			if( parent instanceof JSONObject ) {
				try {
					((JSONObject)parent).put(name, value);
				} catch (JSONException e) {
				}
			} else if( parent instanceof JSONArray ) {
				((JSONArray)parent).put(value);
			} else {
				try {
					throw new Exception();
				} catch (Exception e) {
				}				
			}
		}
		@Override
		public void startList(String name, int count) {
			JSONArray newList = new JSONArray();
			if( 0 < objStack.size() ) {
				Object parent = objStack.get(objStack.size()-1);
				if( parent instanceof JSONObject ) {
					try {
						((JSONObject)parent).put(name, newList);
					} catch (JSONException e) {
					}
				} else if( parent instanceof JSONArray ) {
					((JSONArray)parent).put(newList);
				} else {
					try {
						throw new Exception();
					} catch (Exception e) {
					}
				}
			} else {
				root = newList;
			}
			objStack.add(newList);
		}
		@Override
		public void endList() {
			objStack.remove(objStack.size()-1);
		}
	}
	

}

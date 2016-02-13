/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page;

import eportfolio.exception.ContentCreationException;
import eportfolio.exception.PropertyCreationException;
import static eportfolio.main.Constant.CSS_PROPERTY_VBOX;
import eportfolio.model.page.properties.PageProperty;
import eportfolio.model.page.properties.TextProperty;
import eportfolio.ui.view.MainViewDataInterface;
import java.lang.reflect.Constructor;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 *
 * @author mingchenzhang
 */
public class PagePropertiesManager {
	private final static String JSON_PROPERTY_ARRAY = "properties";
	private final static String JSON_IN_EDIT = "inEdit";

	private PageModel page;
	private Vector<PageProperty> properties = new Vector();
	
	Object[] getPropertiesArray(){return properties.toArray();}

	public PagePropertiesManager(JsonArray json, PageModel page) throws PropertyCreationException {
		this.page = page;
		int size = json.size();
		for (int i = 0; i < size; i++) {
			PageProperty prop;
			try {
				prop = createPropByName(json.getJsonObject(i).getString("propertyType"), json.getJsonObject(i), page);
			} catch (Exception ex) {
				Logger.getLogger(MainViewDataInterface.class.getName()).log(Level.SEVERE, null, ex);
				throw new PropertyCreationException("Failed to create property: " + json.getJsonObject(i).toString());
			}
			properties.add(prop);
		}
	}
	
	public String getPageTitle(){
		for(PageProperty prop: properties){
			if(prop.getConfigName().equals("pageTitle"))
				return ((TextProperty)prop).getContent();
		}
		return null;
	}
	
	public boolean setPageTitle(String title){
		for(PageProperty prop: properties){
			if(prop.getConfigName().equals("pageTitle")){
				((TextProperty)prop).setContent(title);
				return true;
			}
		}
		return false;
	}

	public void addProperty(String type, JsonValue json) throws PropertyCreationException {
		page.getDataInterface().setModified();
		PageProperty prop;
		try {
			prop = createPropByName(type, json, page);
		} catch (Exception ex) {
			throw new PropertyCreationException("Failed to create property: " + type);
		}
		properties.add(prop);
	}
	
	public JsonObject saveAsJson(){
		JsonArrayBuilder propertiesArray = Json.createArrayBuilder();
		for(int i=0; i<properties.size(); i++){
			propertiesArray.add(properties.get(i).saveAsJson());
		}
		return Json.createObjectBuilder().add(JSON_PROPERTY_ARRAY, propertiesArray).build();
	}
	
	public void loadFromJson(JsonObject json) throws ContentCreationException{
		JsonArray contentsArray = json.getJsonArray(JSON_PROPERTY_ARRAY);
		for(int i=0; i<contentsArray.size(); i++){
			properties.get(i).loadFromJson(contentsArray.getJsonObject(i));
		}
	}
	
	public JsonObject export(String workingDirectory, boolean inEdit){
		JsonObjectBuilder json = Json.createObjectBuilder();
		for(PageProperty prop: properties){
			json.add(prop.getConfigName(), prop.export(workingDirectory,inEdit));
		}
		if(inEdit) json.add(JSON_IN_EDIT, inEdit);
		return json.build();
	}
	
	public VBox getEditPane(){
		VBox editPane = new VBox();
		editPane.getStyleClass().add(CSS_PROPERTY_VBOX);
		for(PageProperty prop: properties){
			Pane subEditPane = prop.getEditPane();
			if(subEditPane != null)
				editPane.getChildren().add(subEditPane);
		}
		return editPane;
	}
	
	private PageProperty createPropByName(String name, JsonValue json, PageModel page) throws Exception {
		System.out.println(name);
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		Class<?> class_ = Class.forName("eportfolio.model.page.properties." + name + "Property");
		Constructor<?> ctor = class_.getConstructor(JsonValue.class, PageModel.class);
		PageProperty pageProperty = (PageProperty) ctor.newInstance(json, page);
		return pageProperty;
	}
}

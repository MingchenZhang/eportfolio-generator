/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import eportfolio.exception.ContentCreationException;
import eportfolio.model.page.PageModel;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 *
 * @author mingchenzhang
 */
public abstract class PageProperty {
	protected String configName;
	protected String displayName;
	protected String description;
	protected PageModel page;
	
	public String getConfigName(){return configName;}
	public String getDisplayName(){return displayName;}
	public String getDescription(){return description;}
	
	protected PageProperty(JsonValue json, PageModel page){
		if(json.getValueType().equals(ValueType.OBJECT)){
			JsonObject jsonO = (JsonObject)json;
			configName = jsonO.getString("configName");
			displayName = jsonO.getString("displayName");
			description = jsonO.getString("description");
		}
		this.page = page;
	}
	
	public abstract JsonObject saveAsJson();
	public abstract void loadFromJson(JsonObject json)throws ContentCreationException;
	
	public abstract JsonValue export(String workingDirectory, boolean inEdit);
	
	public abstract Pane getEditPane();
	
	public abstract Dialog getEditDialog();
	
	protected JsonString createJsonString(String str){
		return Json.createObjectBuilder().add("str", str).build().getJsonString("str");
	}
}

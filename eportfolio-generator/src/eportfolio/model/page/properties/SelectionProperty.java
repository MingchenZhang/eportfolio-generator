/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import eportfolio.model.page.PageModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class SelectionProperty extends PageProperty{
	private final static String JSON_SELECTION = "selection";
	
	private JsonArray selectionArray;
	private JsonArray selectionKeyArray;
	private String selection = "";

	public SelectionProperty(JsonValue json, PageModel page) {
		super(json, page);
		if(json.getValueType().equals(JsonValue.ValueType.OBJECT)){
			JsonObject jsonO = (JsonObject)json;
			selectionArray = jsonO.getJsonArray("selections");
			selectionKeyArray = jsonO.getJsonArray("selectionsKey");
			if(selectionArray.size() != selectionKeyArray.size())
				throw new IllegalArgumentException("configration "+configName+" has unpaired selections");
			selection = selectionArray.getString(0);
		}else
			throw new IllegalArgumentException("a selection property received an json array when construct");
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder().add(JSON_SELECTION, selection).build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		selection = json.getString(JSON_SELECTION);
	}

	@Override
	public JsonValue export(String workingDirectory, boolean inEdit) {
		int index = 0;
		for(int i=0; i<selectionArray.size(); i++){
			if(selectionArray.getString(i).equals(selection)){
				index = i;
				break;
			}
		}
		return selectionKeyArray.getJsonString((index<0)?0:index);
	}

	@Override
	public HBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Label typePrompt = new Label(displayName);
		typePrompt.setMinWidth(100);// todo:css
		ComboBox<String> selector = new ComboBox();
		selector.setPrefWidth(200);// todo:css
		ObservableList<String> options = FXCollections.observableArrayList();
		for(int i=0; i<selectionArray.size(); i++){
			options.add(selectionArray.getString(i));
		}
		selector.setItems(options);
		selector.setValue(selection);
		selector.valueProperty().addListener((o,oldValue,newValue)->{
			page.getDataInterface().setModified();
			selection = selector.getValue();
			page.getDataInterface().refreshEditView();
		});
		HBox editPane = new HBox();
		editPane.getChildren().addAll(typePrompt,selector);
		editPane.setSpacing(10);
		editPane.setPadding(new Insets(5.0));
		return editPane;
	}

	@Override
	public Dialog getEditDialog() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}

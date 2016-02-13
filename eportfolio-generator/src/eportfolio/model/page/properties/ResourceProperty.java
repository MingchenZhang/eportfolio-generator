/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import static eportfolio.main.LanguageEnum.BROWSE;
import eportfolio.model.page.PageModel;
import java.io.File;
import java.util.Vector;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class ResourceProperty extends PageProperty{
	private final static String JSON_PATH = "path";
	private final static String JSON_CONFIG_FILETYPE = "fileType";
	
	private String resourcePath = "";
	private Vector<String> fileType = new Vector();

	public ResourceProperty(JsonValue json, PageModel page) {
		super(json, page);
		if(json.getValueType().equals(JsonValue.ValueType.OBJECT)){
			JsonObject jsonO = (JsonObject)json;
			JsonArray fileTypeJson = jsonO.getJsonArray(JSON_CONFIG_FILETYPE);
			for(int i=0; i<fileTypeJson.size(); i++){
				fileType.add(fileTypeJson.getString(i));
			}
		}else
			throw new IllegalArgumentException("a resource property received an json array when construct");
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder().add(JSON_PATH, resourcePath).build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		resourcePath = json.getString(JSON_PATH);
	}

	@Override
	public JsonValue export(String workingDirectory, boolean inEdit) {
		File re = new File(resourcePath);
		if(re.exists()){
			String relativePath = page.getDataInterface().getDataModel().importResource(workingDirectory,resourcePath);
			return createJsonString(relativePath);
		}
		return createJsonString("");
	}

	@Override
	public HBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Label typePrompt = new Label(displayName);
		typePrompt.setMinWidth(100);
		TextField contentField = new TextField();
		contentField.setPrefWidth(120);
		contentField.setText(resourcePath);
		contentField.textProperty().addListener((o,oldText,newText)->{
			resourcePath = contentField.getText();
		});
		contentField.focusedProperty().addListener((o,oldValue,newValue)->{
			if(!newValue) resourcePath = contentField.getText();
		});
		contentField.setOnAction((event)->{
			resourcePath = contentField.getText();
			page.getDataInterface().refreshEditView();
		});
		
		//Browser button
		Button browserButton = new Button(props.getProperty(BROWSE));
		browserButton.setPrefWidth(70);
		browserButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();

			FileChooser.ExtensionFilter[] filters = new FileChooser.ExtensionFilter[fileType.size()];
			for(int i=0; i<fileType.size(); i++){
				filters[i] = new FileChooser.ExtensionFilter(fileType.get(i).toUpperCase()+" files (*."+fileType.get(i).toLowerCase()+")", "*."+fileType.get(i).toUpperCase(), "*."+fileType.get(i).toLowerCase());
			}
			fileChooser.getExtensionFilters().addAll(filters);

			File file = fileChooser.showOpenDialog(null);

			if (file != null) {
				page.getDataInterface().setModified();
				resourcePath = file.getAbsolutePath();
				page.getDataInterface().refreshEditView();
			}
		});
		
		HBox editPane = new HBox();
		editPane.getChildren().addAll(typePrompt,contentField, browserButton);
		editPane.setSpacing(10);
		editPane.setPadding(new Insets(5.0));
		return editPane;
	}

	@Override
	public Dialog getEditDialog() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}

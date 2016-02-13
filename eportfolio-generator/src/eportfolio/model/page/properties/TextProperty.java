/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import static eportfolio.main.LanguageEnum.BROWSE;
import static eportfolio.main.LanguageEnum.TEXT_PROPERTY_DEFAULT;
import eportfolio.model.page.PageModel;
import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class TextProperty extends PageProperty {

	private final static String JSON_CONTENT = "content";

	private String content = "";

	public String getContent() {
		return content;
	}

	public void setContent(String str) {
		content = str;
	}

	public TextProperty(JsonValue json, PageModel page) {
		super(json, page);
		if (!json.getValueType().equals(JsonValue.ValueType.OBJECT)) {
			throw new IllegalArgumentException("a text property received an json array when construct");
		}
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		content = props.getProperty(TEXT_PROPERTY_DEFAULT);
	}

	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder().add(JSON_CONTENT, content).build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		content = json.getString(JSON_CONTENT);
	}

	@Override
	public JsonValue export(String workingDirectory, boolean inEdit) {
		return createJsonString(content);
	}

	@Override
	public HBox getEditPane() {
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		Label typePrompt = new Label(displayName);
		typePrompt.setMinWidth(100);// todo: css
		TextField contentField = new TextField();
		contentField.setPrefWidth(200);// todo: css
		contentField.setText(content);
		contentField.textProperty().addListener((o, oldText, newText) -> {
			page.getDataInterface().setModified();
			content = contentField.getText();
		});
		contentField.focusedProperty().addListener((o, oldValue, newValue) -> {
			if (!newValue) {
				if (content.equals(contentField.getText())) {
					page.getDataInterface().setModified();
				}
				content = contentField.getText();
			}
		});
		contentField.setOnAction((event) -> {
			page.getDataInterface().setModified();
			content = contentField.getText();
			page.getDataInterface().refreshEditView();
		});
		
		HBox editPane = new HBox();
		editPane.getChildren().addAll(typePrompt, contentField);
		editPane.setSpacing(10); //todo:css
		editPane.setPadding(new Insets(5.0));
		return editPane;
	}

	@Override
	public Dialog getEditDialog() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}

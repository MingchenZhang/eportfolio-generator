/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import eportfolio.exception.ContentCreationException;
import static eportfolio.main.LanguageEnum.ERROR_FAIL_TO_CREATE_ELEMENT;
import eportfolio.model.page.PageModel;
import eportfolio.model.page.elements.ContentElement;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 *
 * @author zmc94
 */
public class ContentProperty extends PageProperty{
	private final static String JSON_CONTENT_LIST = "list";
	private final static String JSON_ELEMENT_ID = "id";
	private final static String JSON_ELEMENT_TYPE = "type";
	private final static String JSON_ELEMENT = "element";
	
	private Vector<String> supportedElements = new Vector<String>();
	private Vector<ContentElement> contentList = new Vector();

	public ContentProperty(JsonValue json, PageModel page) {
		super(json, page);
		if(!json.getValueType().equals(JsonValue.ValueType.ARRAY))
			throw new IllegalArgumentException("content property did not received an json array when construct");
		JsonArray jsonArray = (JsonArray)json;
		for(int i=0; i<jsonArray.size(); i++){
			supportedElements.add(jsonArray.getString(i));
		}
		
		configName = "content";
	}
	
	public String[] getSupportedElements(){
		return (String[]) supportedElements.toArray();
	}
	
	public String addNewElement(String type) throws ContentCreationException{
		if(supportedElements.contains(type)){
			page.getDataInterface().setModified();
			ArrayList<Integer> existedId = new ArrayList<Integer>(contentList.size());
			int candidate = 0;
			for(int i=0; i<contentList.size(); i++){
				if(contentList.get(i).getType().equals(type))
					if(candidate <= contentList.get(i).getElementCount())
						candidate = contentList.get(i).getElementCount()+1;
			}
			try {
				contentList.add(createElemByName(type, candidate, page));
			} catch (Exception ex) {
				throw new ContentCreationException("Failed to create content: "+type);
			}
			return type+"-"+candidate;
		}
		throw new ContentCreationException("content is not support in this layout: "+type);
	}
	
	public boolean removeElement(ContentElement element){
		page.getDataInterface().setModified();
		return contentList.remove(element);
	}
	
	public boolean moveUpElement(ContentElement element){
		int index = contentList.indexOf(element);
		if(index>0){
			page.getDataInterface().setModified();
			contentList.set(index, contentList.get(index-1));
			contentList.set(index-1, element);
			return true;
		}else{
			return false;
		}
	}
	
	public boolean moveDownElement(ContentElement element){
		int index = contentList.indexOf(element);
		if(index<contentList.size()-1 && index>-1){
			page.getDataInterface().setModified();
			contentList.set(index, contentList.get(index+1));
			contentList.set(index+1, element);
			return true;
		}else{
			return false;
		}
	}

	@Override
	public JsonObject saveAsJson() {
		JsonArrayBuilder contentsArray = Json.createArrayBuilder();
		for(int i=0; i<contentList.size(); i++){
			JsonObject contentObject = Json.createObjectBuilder()
					.add(JSON_ELEMENT_ID, contentList.get(i).getID())
					.add(JSON_ELEMENT_TYPE, contentList.get(i).getType())
					.add(JSON_ELEMENT, contentList.get(i).saveAsJson())
					.build();
			contentsArray.add(contentObject);
		}
		return Json.createObjectBuilder().add(JSON_CONTENT_LIST, contentsArray).build();
	}

	@Override
	public void loadFromJson(JsonObject json) throws ContentCreationException{
		JsonArray contentsArray = json.getJsonArray(JSON_CONTENT_LIST);
		for(int i=0; i<contentsArray.size(); i++){
			String id = contentsArray.getJsonObject(i).getString(JSON_ELEMENT_ID);
			String type = contentsArray.getJsonObject(i).getString(JSON_ELEMENT_TYPE);
			try {
				ContentElement content = createElemByName(type, id, page);
				content.loadFromJson(contentsArray.getJsonObject(i).getJsonObject(JSON_ELEMENT));
				contentList.add(content);
			} catch (Exception ex) {
				Logger.getLogger(ContentProperty.class.getName()).log(Level.SEVERE, null, ex);
				throw new ContentCreationException("Failed to create content: "+type);
			}
		}
	}

	@Override
	public JsonValue export(String workingDirectory, boolean inEdit) {
		JsonArrayBuilder json = Json.createArrayBuilder();
		for(ContentElement elem: contentList){
			json.add(elem.export(workingDirectory,inEdit));
		}
		return json.build();
	}

	@Override
	public Pane getEditPane() {
		Vector<Button> buttons = new Vector();
		for(String elem: supportedElements){
			Button addElemButton = new Button(elem);
			addElemButton.setPrefWidth(70);// todo: css
			addElemButton.setOnAction(e->{
				try {
					addNewElement(elem);
					page.getDataInterface().setModified();
					page.getDataInterface().refreshEditView();
				} catch (ContentCreationException ex) {
					page.getDataInterface().getUI().setUserHint(ERROR_FAIL_TO_CREATE_ELEMENT, ex, 2);
				}
			});
			buttons.add(addElemButton);
		}
		FlowPane editPane = new FlowPane();
		editPane.getChildren().addAll(buttons);
		editPane.setHgap(10.0);
		editPane.setVgap(8.0);
		editPane.setMaxWidth(320);
		editPane.setPadding(new Insets(5.0));
		return editPane;
	}

	@Override
	public Dialog getEditDialog() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	public ContentElement getElementById(String id){
		for(ContentElement elem:contentList){
			if(elem.getID().equalsIgnoreCase(id)){
				return elem;
			}
		}
		return null;
	}
	
	private ContentElement createElemByName(String type, int id, PageModel page) throws Exception {
		type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
		Class<?> class_ = Class.forName("eportfolio.model.page.elements." + type + "Element");
		Constructor<?> ctor = class_.getConstructor(int.class,PageModel.class,ContentProperty.class);
		ContentElement pageProperty = (ContentElement)ctor.newInstance(id,page,this);
		return pageProperty;
	}
	
	private ContentElement createElemByName(String type, String id, PageModel page) throws Exception {
		type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
		Class<?> class_ = Class.forName("eportfolio.model.page.elements." + type + "Element");
		Constructor<?> ctor = class_.getConstructor(String.class,PageModel.class,ContentProperty.class);
		ContentElement pageProperty = (ContentElement)ctor.newInstance(id,page,this);
		return pageProperty;
	}
	
	//todo: dispose garbage
	private void paragraphDialog(){
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle("paragraph");
		dialog.setHeaderText("edit paragraph");

		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(okButtonType, cancelButtonType);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		
		Label paragraphLabel = new Label();
		TextArea textField = new TextArea();
		textField.setPrefSize(400, 200);
		HBox linkBox = new HBox();
		Label linkStartLabel = new Label("link start at:         ");
		Label linkEndLabel = new Label("link end at:          ");
		Button confirm = new Button("confirm");
		linkBox.getChildren().addAll(linkStartLabel,linkEndLabel,confirm);
		Label font = new Label("Font:  ");
		ComboBox fontS = new ComboBox();
		fontS.setPrefWidth(100);
		grid.add(paragraphLabel, 0, 0);
		grid.add(textField, 1, 0);
		grid.add(linkBox, 1, 1);
		grid.add(font, 0, 2);
		grid.add(fontS, 1,2);
		
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.showAndWait();
	}
	
	private void listDialog(){
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle("list");
		dialog.setHeaderText("edit list(each item take one line)");

		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(okButtonType, cancelButtonType);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		
		Label paragraphLabel = new Label();
		TextArea textField = new TextArea();
		textField.setPrefSize(400, 200);
		
		grid.add(paragraphLabel, 0, 0);
		grid.add(textField, 1, 0);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.showAndWait();
	}
	
	private void imageDialog(){
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle("image");
		dialog.setHeaderText("edit image");

		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(okButtonType, cancelButtonType);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		
		Button browse = new Button("browse");
		browse.setOnAction(e->{
			
		});
		Label height = new Label("height");
		TextField text01 = new TextField();
		Label width = new Label("width");
		TextField text02 = new TextField();
		ComboBox<String> ali = new ComboBox();
		String[] sel = {"floated left", "floated right", "neither"};
		ObservableList<String> options = FXCollections.observableArrayList();
		options.addAll(sel);
		ali.setItems(options);
		ali.setValue("floated left");
		grid.add(browse, 0, 0);
		grid.add(height, 0, 1);
		grid.add(text01, 1, 1);
		grid.add(width, 0, 2);
		grid.add(text02, 1, 2);
		grid.add(ali, 0, 3);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.showAndWait();
	}
	
	private void videoDialog(){
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle("video");
		dialog.setHeaderText("edit video");

		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		dialog.getDialogPane().getButtonTypes()
				.addAll(okButtonType, cancelButtonType);

		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		
		Button browse = new Button("browse video");
		browse.setOnAction(e->{
			
		});
		Button browse2 = new Button("browse caption");
		browse.setOnAction(e->{
			
		});
		Label height = new Label("height");
		TextField text01 = new TextField();
		Label width = new Label("width");
		TextField text02 = new TextField();
		grid.add(browse, 0, 0);
		grid.add(browse2, 1, 0);
		grid.add(height, 0, 1);
		grid.add(text01, 1, 1);
		grid.add(width, 0, 2);
		grid.add(text02, 1, 2);
		
		dialog.getDialogPane().setContent(grid);
		
		dialog.showAndWait();
	}
	
	
}

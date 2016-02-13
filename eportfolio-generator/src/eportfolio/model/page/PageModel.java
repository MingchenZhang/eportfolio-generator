/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page;

import eportfolio.exception.ContentCreationException;
import eportfolio.exception.PropertyCreationException;
import eportfolio.main.LanguageEnum;
import eportfolio.model.page.elements.ContentElement;
import eportfolio.model.page.properties.ContentProperty;
import eportfolio.model.page.properties.PageProperty;
import eportfolio.ui.view.MainViewDataInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class PageModel {

	private static final String JSON_PAGE_PROPERTY = "pageProperties";
	private static final String JSON_PAGE_NAVIGATION = "navigation";
	private static final String JSON_PAGE_CONTENT = "content";

	private MainViewDataInterface data;

	private PagePropertiesManager pageProperty;

	public MainViewDataInterface getDataInterface() {
		return data;
	}

	public PagePropertiesManager getPropertiesManager() {
		return pageProperty;
	}

	public PageModel(JsonObject json, MainViewDataInterface data) throws PropertyCreationException {
		this.data = data;
		JsonArray pageProperties = json.getJsonArray(JSON_PAGE_PROPERTY);
		pageProperty = new PagePropertiesManager(pageProperties, this);
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		int ramdomName = (int)(Math.random()*100000);
		pageProperty.setPageTitle(props.getProperty(LanguageEnum.TEXT_DEFAULT_PAGE_NAME)+" "+ramdomName);
		if (json.getInt(JSON_PAGE_NAVIGATION) == 1) {
			pageProperty.addProperty(JSON_PAGE_NAVIGATION, json.getJsonNumber(JSON_PAGE_NAVIGATION));
		}
		if (json.containsKey(JSON_PAGE_CONTENT)) {
			pageProperty.addProperty(JSON_PAGE_CONTENT, json.getJsonArray(JSON_PAGE_CONTENT));
		}
	}

	public String getPageTitle() {
		return pageProperty.getPageTitle();
	}

	public boolean setPageTitle(String title) {
		data.setModified();
		return pageProperty.setPageTitle(title);
	}

	public JsonObject saveAsJson() {
		return pageProperty.saveAsJson();
	}

	public void loadFromJson(JsonObject json) throws ContentCreationException {
		pageProperty.loadFromJson(json);
	}

	public void export(String workingDirectory, boolean inEdit) throws FileNotFoundException, IOException {
		JsonObject pageContent = pageProperty.export(workingDirectory, inEdit);
		// INIT THE WRITER
		OutputStream os = new FileOutputStream(workingDirectory + getPageTitle() + ".html.json");
		JsonWriter jsonWriter = Json.createWriter(os);
		jsonWriter.writeObject(pageContent);
		os.close();

		//copyFolder(new File(data.getDataModel().getLayoutFolder()), new File(workingDirectory));
		
		copyFile(new File(data.getDataModel().getLayoutFolder()+"/page.html"), new File(workingDirectory+getPageTitle()+".html"));
	}
	
	public ContentElement getElementById(String id){
		Object[] props = pageProperty.getPropertiesArray();
		for(Object prop:props){
			if(((PageProperty)prop).getConfigName().contains("content")){
				return ((ContentProperty)prop).getElementById(id);
			}
		}
		return null;
	}

	private static String readFile(File file)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
		InputStream is = new FileInputStream(jsonFilePath);
		JsonReader jsonReader = Json.createReader(is);
		JsonObject json = jsonReader.readObject();
		jsonReader.close();
		is.close();
		return json;
	}

	private static void copyFile(File source, File dest)
			throws IOException {
		Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}

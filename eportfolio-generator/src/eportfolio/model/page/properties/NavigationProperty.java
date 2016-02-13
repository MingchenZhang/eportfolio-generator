/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model.page.properties;

import eportfolio.model.page.PageModel;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 *
 * @author zmc94
 */
public class NavigationProperty extends PageProperty{
	
	private static final String JSON_EXPORT_TITLE = "linkName";
	private static final String JSON_EXPORT_LINK = "link";
	private static final String JSON_EXPORT_ACTIVATED = "activated";
	
	public NavigationProperty(JsonValue json, PageModel page) {
		super(json, page);
		configName = "nav";
	}
	
	@Override
	public JsonObject saveAsJson() {
		return Json.createObjectBuilder().build();
	}

	@Override
	public void loadFromJson(JsonObject json) {
		// does nothing
	}

	@Override
	public JsonValue export(String workingDirectory, boolean inEdit) {
		JsonArrayBuilder navArray = Json.createArrayBuilder();
		String[] pageList = page.getDataInterface().getDataModel().getAllPageTitle();
		for(String title: pageList){
			String pagePath = page.getDataInterface().getDataModel().getPagePath(page.getPageTitle(),title);
			boolean currentPage = page.getPageTitle().equals(title);
			JsonObject pageLink = Json.createObjectBuilder()
					.add(JSON_EXPORT_TITLE, title)
					.add(JSON_EXPORT_LINK, pagePath)
					.add(JSON_EXPORT_ACTIVATED, currentPage?1:0)
					.build();
			navArray.add(pageLink);
		}
		return navArray.build();
	}

	@Override
	public HBox getEditPane() {
		return null;
	}

	@Override
	public Dialog getEditDialog() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}

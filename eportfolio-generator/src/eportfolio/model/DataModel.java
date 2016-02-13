/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eportfolio.model;

import eportfolio.exception.ContentCreationException;
import eportfolio.exception.PropertyCreationException;
import eportfolio.exception.TitleDuplicationException;
import static eportfolio.main.Constant.PATH_PREVIEW_FOLDER;
import eportfolio.model.page.PageModel;
import eportfolio.ui.view.MainViewDataInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author zmc94
 */
public class DataModel {

	private static final String JSON_PAGES = "pages";
	private static final String JSON_LAYOUT_FOLDER = "layout";
	private static final String JSON_CONFIG_NAME = "config.json";

	private MainViewDataInterface data;

	private Vector<PageModel> pages = new Vector();
	private JsonObject configJson;
	private String layoutFolder;

	public DataModel(MainViewDataInterface d) {
		data = d;
	}

	public String getLayoutFolder() {
		return layoutFolder;
	}

	public int getPageCount() {
		return pages.size();
	}

	public PageModel getPageModel(int index) {
		if (index >= 0) {
			return pages.get(index);
		} else {
			return null;
		}
	}

	public int getPageModelIndex(String title) {
		int index = -1;
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i).getPageTitle().equals(title)) {
				index = i;
			}
		}
		return index;
	}

	public void setLayoutFolder(String path) throws IOException {
		layoutFolder = path;
		configJson = loadJSONFile(layoutFolder + "/" + JSON_CONFIG_NAME);
		pages.clear();
	}

	public PageModel addPage() throws PropertyCreationException {
		PageModel pageModel = new PageModel(configJson, data);
		pages.add(pageModel);
		data.setModified();
		return pageModel;
	}

	public void removePage(String name) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i).getPageTitle().equals(name)) {
				pages.remove(i);
			}
		}
		data.getUI().displayPageEditView(null);
		data.setModified();
	}

	public void removePage(int index) {
		pages.remove(index);
		data.getUI().displayPageEditView(null);
		data.setModified();
	}

	public String[] getAllPageTitle() {
		String[] titles = new String[pages.size()];
		for (int i = 0; i < pages.size(); i++) {
			titles[i] = pages.get(i).getPageTitle();
		}
		return titles;
	}

	public String getPagePath(String fromTitle, String targetTitle) {
		return targetTitle + ".html";
	}

	public JsonObject saveAsJson() {
		JsonArrayBuilder pagesArray = Json.createArrayBuilder();
		for (int i = 0; i < pages.size(); i++) {
			pagesArray.add(pages.get(i).saveAsJson());
		}
		return Json.createObjectBuilder()
				.add(JSON_PAGES, pagesArray.build())
				.add(JSON_LAYOUT_FOLDER, layoutFolder)
				.build();
	}

	public void loadFromJson(JsonObject json) throws PropertyCreationException, ContentCreationException, IOException {
		setLayoutFolder(json.getString(JSON_LAYOUT_FOLDER));
		JsonArray pagesArray = json.getJsonArray(JSON_PAGES);
		pages.clear();
		for (int i = 0; i < pagesArray.size(); i++) {
			PageModel page = new PageModel(configJson, data);
			page.loadFromJson(pagesArray.getJsonObject(i));
			pages.add(page);
		}
	}

	public void export(File workingDirectory) throws TitleDuplicationException, IOException {
		Vector<String> titleList = new Vector(pages.size());
		for (PageModel page : pages) {
			if (titleList.contains(page.getPageTitle())) {
				throw new TitleDuplicationException("Same name page exist");
			}
			titleList.add(page.getPageTitle());
		}

		copyFolder(new File(layoutFolder), workingDirectory);

		for (int i = 0; i < pages.size(); i++) {
			System.out.println(workingDirectory.getAbsolutePath());
			System.out.println(workingDirectory.getName());
			pages.get(i).export(workingDirectory.getAbsolutePath() + "\\", false);
		}

	}

	public void view() throws TitleDuplicationException, IOException {
		Vector<String> titleList = new Vector(pages.size());
		for (PageModel page : pages) {
			if (titleList.contains(page.getPageTitle())) {
				throw new TitleDuplicationException("Same name page exist");
			}
			titleList.add(page.getPageTitle());
		}

		deleteFolder(new File(PATH_PREVIEW_FOLDER));
		copyFolder(new File(layoutFolder), new File(PATH_PREVIEW_FOLDER));

		for (int i = 0; i < pages.size(); i++) {
			pages.get(i).export(PATH_PREVIEW_FOLDER, false);
		}
	}

	public void preViewExport() throws FileNotFoundException, IOException, TitleDuplicationException {
		Vector<String> titleList = new Vector(pages.size());
		for (PageModel page : pages) {
			if (titleList.contains(page.getPageTitle())) {
				throw new TitleDuplicationException("Same name page exist");
			}
			titleList.add(page.getPageTitle());
		}

		deleteFolder(new File(PATH_PREVIEW_FOLDER));
		copyFolder(new File(layoutFolder), new File(PATH_PREVIEW_FOLDER));

		for (int i = 0; i < pages.size(); i++) {
			pages.get(i).export(PATH_PREVIEW_FOLDER, true);
		}
	}

	public void clear() {
		pages.clear();
	}

	public String importResource(String workingDirectory, String path) {
		if (path.isEmpty()) {
			return "";
		}

		File source = new File(path);
		File resourceFile = new File(workingDirectory + "resources/" + source.getName());// todo: constant
		int existCount = 0;
		while (resourceFile.exists()) {
			String[] fileName = source.getName().split("\\.(?=[^\\.]+$)");// get file name base and extension
			resourceFile = new File(workingDirectory + "resources/" + fileName[0] + "_" + existCount + ((fileName.length > 1) ? "." + fileName[1] : ""));// todo: constant
			existCount++;
		}
		try {
			Files.copy(source.toPath(), resourceFile.toPath());
		} catch (IOException ex) {
			Logger.getLogger(DataModel.class.getName()).log(Level.SEVERE, null, ex);//todo: prompt user
		}
		return "resources/" + source.getName();
	}

	public String importFolderResource(String workingDirectory, String path) {
		if (path.isEmpty()) {
			return "";
		}

		File source = new File(path);
		File resourceFile = new File(workingDirectory + "resources/" + source.getName());// todo: constant
		int existCount = 0;
		while (resourceFile.exists()) {
			String[] fileName = source.getName().split("\\.(?=[^\\.]+$)");// get file name base and extension
			resourceFile = new File(workingDirectory + "resources/" + fileName[0] + "_" + existCount + ((fileName.length > 1) ? "." + fileName[1] : ""));// todo: constant
			existCount++;
		}
		try {
			copyFolder(source, resourceFile);
		} catch (IOException ex) {
			Logger.getLogger(DataModel.class.getName()).log(Level.SEVERE, null, ex);//todo: prompt user
		}
		return "resources/" + source.getName();
	}

	private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
		InputStream is = new FileInputStream(jsonFilePath);
		JsonReader jsonReader = Json.createReader(is);
		JsonObject json = jsonReader.readObject();
		jsonReader.close();
		is.close();
		return json;
	}

	public static void copyFolder(File source, File destination) throws IOException {
		if (source.isDirectory()) {
			if (!destination.exists()) {
				destination.mkdirs();
			}

			String[] fileList = source.list();
			for (int i = 0; i < fileList.length; i++) {
				copyFolder(new File(source, fileList[i]), new File(destination, fileList[i]));
			}
		} else {
			Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static boolean deleteFolder(File dir) throws IOException {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteFolder(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
}

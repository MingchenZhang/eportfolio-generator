/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eporfolio.util;

import eporfolio.util.dialog.AlertUser;
import eportfolio.main.LanguageEnum;
import eportfolio.ui.view.MainView;
import java.io.PrintWriter;
import java.io.StringWriter;
import properties_manager.PropertiesManager;

/**
 *
 * @author zmc94
 */
public class ErrorHandler {

	private MainView ui;

	public ErrorHandler(MainView ui) {
		this.ui = ui;
	}

	public void processError(String head, String error, Throwable detail) {
		if (detail != null) {
			StringWriter sw = new StringWriter();
			detail.printStackTrace(new PrintWriter(sw));
			AlertUser.textDialog(head, error, sw.toString());
		} else {
			PropertiesManager props = PropertiesManager.getPropertiesManager();
			String ok = props.getProperty(LanguageEnum.OK);
			if(ok != null){
				AlertUser.errorAlert(head,head,error,ok);
			}else{
				AlertUser.errorAlert(head,head,error,"ok");
			}
		}
	}
}

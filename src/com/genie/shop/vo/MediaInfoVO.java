package com.genie.shop.vo;

import java.io.File;
import java.io.Serializable;

public class MediaInfoVO extends SongVO {
	public File file = null;
	
	
	public String filePath ="";
	
	public String cdnPath = "";
	
	public String getCdnPath() {
		return cdnPath;
	}

	public void setCdnPath(String cdnPath) {
		this.cdnPath = cdnPath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}

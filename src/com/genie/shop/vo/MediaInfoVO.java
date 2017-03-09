package com.genie.shop.vo;

import java.io.File;
import java.io.Serializable;

public class MediaInfoVO extends SongVO {
	public File file = null;
	
	public String cdnPath = "";
	
	public String getCdnPath() {
		return cdnPath;
	}

	public void setCdnPath(String cdnPath) {
		this.cdnPath = cdnPath;
	}
	
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	
}

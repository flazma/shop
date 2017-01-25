package com.genie.shop.vo;

import java.io.File;
import java.io.Serializable;

public class MediaInfoVO implements Serializable{
	public File file = null;
	
	public Long seq = -1L;

	public Long songUid = -1L;
	
	
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

	public Long getSongUid() {
		return songUid;
	}

	public void setSongUid(Long songUid) {
		this.songUid = songUid;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}
	
	
	
}

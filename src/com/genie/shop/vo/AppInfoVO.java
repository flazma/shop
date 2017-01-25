package com.genie.shop.vo;

import java.io.Serializable;
import java.util.Date;

public class AppInfoVO implements Serializable{

	
	private String appUid;
	private String platform;
	private String version;
	private String market;
	private Date deployDate;
	private String filePath;
	private Date rDate;
	private Date mDate;

	public void setAppInfo(String appUid,String platform,String version,String market,Date deployDate, String filePath,Date rDate,Date mDate){
		this.appUid = appUid;
		this.platform = platform;
		this.version = version;
		this.market = market;
		this.deployDate = deployDate;
		this.filePath = filePath;
		this.rDate = rDate;
		this.mDate = mDate;		
	}
	
	

	public String getAppUid() {
		return appUid;
	}



	public void setAppUid(String appUid) {
		this.appUid = appUid;
	}



	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public Date getDeployDate() {
		return deployDate;
	}
	public void setDeployDate(Date deployDate) {
		this.deployDate = deployDate;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public Date getrDate() {
		return rDate;
	}
	public void setrDate(Date rDate) {
		this.rDate = rDate;
	}
	public Date getmDate() {
		return mDate;
	}
	public void setmDate(Date mDate) {
		this.mDate = mDate;
	}
	
	
	
	
}

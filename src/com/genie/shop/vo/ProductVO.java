package com.genie.shop.vo;

import java.io.Serializable;
import java.util.Date;

public class ProductVO implements Serializable{

	private String rowNo;
	private String mchargeNo;
	private String packageName;
	private Date cosumeDate;
	private Date cexpDate;
	private String ymd;
	private String payToolm;
	private String useState;
	private Date regDate;
	private String packageId;
	
	
	
	public Date getCosumeDate() {
		return cosumeDate;
	}
	public void setCosumeDate(Date cosumeDate) {
		this.cosumeDate = cosumeDate;
	}
	public String getRowNo() {
		return rowNo;
	}
	public void setRowNo(String rowNo) {
		this.rowNo = rowNo;
	}
	public String getMchargeNo() {
		return mchargeNo;
	}
	public void setMchargeNo(String mchargeNo) {
		this.mchargeNo = mchargeNo;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Date getCexpDate() {
		return cexpDate;
	}
	public void setCexpDate(Date cexpDate) {
		this.cexpDate = cexpDate;
	}
	public String getYmd() {
		return ymd;
	}
	public void setYmd(String ymd) {
		this.ymd = ymd;
	}
	public String getPayToolm() {
		return payToolm;
	}
	public void setPayToolm(String payToolm) {
		this.payToolm = payToolm;
	}
	public String getUseState() {
		return useState;
	}
	public void setUseState(String useState) {
		this.useState = useState;
	}
	public Date getRegDate() {
		return regDate;
	}
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	public String getPackageId() {
		return packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}
	
	
}

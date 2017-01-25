package com.genie.shop.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class UserVO implements Serializable{
	private String userId;
	private String userPass;
	private String captchaUid;
	private String captchaImgUrl;
	private String answer;
	
	private Long chainUid;
	private Long shopUid;
	private String rtCode;
	private String rtMsg;
	private String sessionKey;
	
	
	private Boolean OldPasswdCheck;
	private String accountId;	
	private String phoneNumber;
	private String email;
	private String allianceCode;
	private String shopName;
	
	private ArrayList<ProductVO> productList = new ArrayList();
	
	private Date consumeDate;
	private Date cexpDate;
	private String prodName;
	private String bpayNos;
	private String bpayYn;
	
	
	private Long puid;
	private String policyKey;
	private String policyValue;
	private String policyDesc;
	
	
	public ArrayList<ProductVO> getProductList() {
		return productList;
	}
	public void setProductList(ArrayList<ProductVO> productList) {
		this.productList = productList;
	}
	public Date getConsumeDate() {
		return consumeDate;
	}
	public void setConsumeDate(Date consumeDate) {
		this.consumeDate = consumeDate;
	}
	public Date getCexpDate() {
		return cexpDate;
	}
	public void setCexpDate(Date cexpDate) {
		this.cexpDate = cexpDate;
	}
	public String getProdName() {
		return prodName;
	}
	public void setProdName(String prodName) {
		this.prodName = prodName;
	}
	public String getBpayNos() {
		return bpayNos;
	}
	public void setBpayNos(String bpayNos) {
		this.bpayNos = bpayNos;
	}
	public String getBpayYn() {
		return bpayYn;
	}
	public void setBpayYn(String bpayYn) {
		this.bpayYn = bpayYn;
	}
	public Boolean getOldPasswdCheck() {
		return OldPasswdCheck;
	}
	public void setOldPasswdCheck(Boolean oldPasswdCheck) {
		OldPasswdCheck = oldPasswdCheck;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAllianceCode() {
		return allianceCode;
	}
	public void setAllianceCode(String allianceCode) {
		this.allianceCode = allianceCode;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public Long getPuid() {
		return puid;
	}
	public void setPuid(Long puid) {
		this.puid = puid;
	}
	public String getPolicyKey() {
		return policyKey;
	}
	public void setPolicyKey(String policyKey) {
		this.policyKey = policyKey;
	}
	public String getPolicyValue() {
		return policyValue;
	}
	public void setPolicyValue(String policyValue) {
		this.policyValue = policyValue;
	}
	public String getPolicyDesc() {
		return policyDesc;
	}
	public void setPolicyDesc(String policyDesc) {
		this.policyDesc = policyDesc;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public String getCaptchaUid() {
		return captchaUid;
	}
	public void setCaptchaUid(String captchaUid) {
		this.captchaUid = captchaUid;
	}
	public String getCaptchaImgUrl() {
		return captchaImgUrl;
	}
	public void setCaptchaImgUrl(String captchaImgUrl) {
		this.captchaImgUrl = captchaImgUrl;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getRtCode() {
		return rtCode;
	}
	public void setRtCode(String rtCode) {
		this.rtCode = rtCode;
	}
	public String getRtMsg() {
		return rtMsg;
	}
	public void setRtMsg(String rtMsg) {
		this.rtMsg = rtMsg;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserPass() {
		return userPass;
	}
	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}
	public Long getChainUid() {
		return chainUid;
	}
	public void setChainUid(Long chainUid) {
		this.chainUid = chainUid;
	}
	public Long getShopUid() {
		return shopUid;
	}
	public void setShopUid(Long shopUid) {
		this.shopUid = shopUid;
	}
	

}

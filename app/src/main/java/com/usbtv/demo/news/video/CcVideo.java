package com.usbtv.demo.news.video;


import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;


public class CcVideo {

	@DatabaseField(generatedId = true)
	@JSONField(serialize = false)
	Integer id;

	@DatabaseField
	String vid;

	@DatabaseField
	@JSONField(name = "d")
	String date;
	@DatabaseField
	long dt;

	@DatabaseField
	String title;

	@DatabaseField
	@JSONField(name = "i")
	int status;

	@DatabaseField
	String src;

	@DatabaseField
	String cc;
	
	@JSONField(serialize = false)
	@DatabaseField
	String orgCc;

	@DatabaseField
	String url;

	public CcVideo() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public long getDt() {
		return dt;
	}

	public void setDt(long dt) {
		this.dt = dt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getOrgCc() {
		return orgCc;
	}

	public void setOrgCc(String orgCc) {
		this.orgCc = orgCc;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
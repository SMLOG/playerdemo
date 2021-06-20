package com.usbtv.demo.game;

import com.j256.ormlite.field.DatabaseField;



public class Subject {
	@DatabaseField(generatedId = true)
	private Integer id;
	@DatabaseField(unique = true)
	private String enText;
	@DatabaseField
	private String cnText;
	@DatabaseField
	private String imgUrl;
	@DatabaseField
	private String sound;

	@DatabaseField
	private Integer rTimes;

	private String cat;
	private Integer typeId;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getEnText() {
		return enText;
	}
	public void setEnText(String enText) {
		this.enText = enText;
	}
	public String getCnText() {
		return cnText;
	}
	public void setCnText(String cnText) {
		this.cnText = cnText;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public Integer getrTimes() {
		return rTimes;
	}

	public void setrTimes(Integer rTimes) {
		this.rTimes = rTimes;
	}

}

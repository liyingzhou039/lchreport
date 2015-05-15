package com.lch.report.dto;

import java.util.ArrayList;
import java.util.List;

public class Condition {
	private List<Button> buttons=new ArrayList<Button>();
	private List<Like> likes=new ArrayList<Like>();
	private List<Equal> equals=new ArrayList<Equal>();
	private String power;
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
	public List<Equal> getEquals() {
		return equals;
	}
	public void setEquals(List<Equal> equals) {
		this.equals = equals;
	}
	public List<Button> getButtons() {
		return buttons;
	}
	public void setButtons(List<Button> buttons) {
		this.buttons = buttons;
	}
	public List<Like> getLikes() {
		return likes;
	}
	public void setLikes(List<Like> likes) {
		this.likes = likes;
	}
}

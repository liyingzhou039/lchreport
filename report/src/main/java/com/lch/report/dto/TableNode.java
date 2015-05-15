package com.lch.report.dto;

import java.util.ArrayList;
import java.util.List;

public class TableNode {
	private String id;
	private String text;
	private TreeAttribute attributes;
	public TreeAttribute getAttributes() {
		return attributes;
	}
	public void setAttributes(TreeAttribute attributes) {
		this.attributes = attributes;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public List<TableNode> getChildren() {
		return children;
	}
	public void setChildren(List<TableNode> children) {
		this.children = children;
	}
	private String state="closed";
	private List<TableNode> children=new ArrayList<TableNode>();
}

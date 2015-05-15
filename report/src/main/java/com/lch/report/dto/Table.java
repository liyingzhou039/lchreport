package com.lch.report.dto;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String name;
	private String owner;
	private String comments;
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	private List<Column> cols=new ArrayList<Column>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public List<Column> getCols() {
		return cols;
	}
	public void setCols(List<Column> cols) {
		this.cols = cols;
	}
}

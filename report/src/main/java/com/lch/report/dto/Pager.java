package com.lch.report.dto;

import java.util.ArrayList;
import java.util.List;

public class Pager {
	private long total;
	List<String[]> data=new ArrayList<String[]>();
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<String[]> getData() {
		return data;
	}
	public void setData(List<String[]> data) {
		this.data = data;
	}
}

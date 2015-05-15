package com.lch.report.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lch.report.dto.Column;
import com.lch.report.dto.Condition;
import com.lch.report.dto.Equal;
import com.lch.report.dto.Like;
import com.lch.report.dto.Pager;
import com.lch.report.dto.Report;
import com.lch.report.dto.Table;
import com.lch.report.dto.TableNode;
import com.lch.report.dto.TreeAttribute;
import com.lch.report.util.ExcelUtil;
import com.lch.report.util.JsonUtil;
import com.lch.report.util.ReportUtil;

public class ReportServlet extends HttpServlet {

	private static final long serialVersionUID = 101010101010101010L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action=req.getParameter("action");
		if("dbtree".equals(action)){
			this.getDBTree(req, resp);
		}else if("showreport".equals(action)){
			this.showReport(req, resp);
		}else if("listreport".equals(action)){
			this.listReportData(req, resp);
		}else if("listcolumn".equals(action)){
			this.listColumn(req, resp);
		}else if("downreport".equals(action)){
			this.downReportData(req, resp);
		}else{
			resp.getWriter().println("unresolved action!");
		}
	}
	protected void showReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		String rStr=req.getParameter("report");
		Report report=JsonUtil.jsonToBean(rStr, Report.class);
		req.getSession().setAttribute("report", report);
		req.setAttribute("report", report);
		req.getRequestDispatcher("lchreport/show.jsp").forward(req, resp);
	}
	protected void listColumn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		String column=req.getParameter("column");
		String table=req.getParameter("table");
		String sql="select distinct \""+column+"\" from "+ table;
		this.reponseJson(ReportUtil.findList(sql), resp);
	}
	protected void listReportData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		Report report=(Report) req.getSession().getAttribute("report");
		//////////////////////////
		long pageSize=req.getParameter("pageSize")==null?15:Long.parseLong(req.getParameter("pageSize"));
		long pageNumber=req.getParameter("pageNumber")==null?0:Long.parseLong(req.getParameter("pageNumber"));
		
		long start=pageSize*pageNumber;
		long end=pageSize*(pageNumber+1);
		//////////////////////////
		//处理表
		String sql=ReportUtil.getSqlByReport(report, req, resp);
		
		
		List<String[]> tls=ReportUtil.findList("select count(*) from ("+sql+")");
		long total=Long.parseLong(tls.get(0)[0]);
		sql = "select ttt.* from ( select tt.*,rownum r from (" + sql
				+ " ) tt where rownum<=" + end + " ) ttt where ttt.r>" + start;
		
		
		List<String[]> ls=ReportUtil.findList(sql);
		
		Pager  pager=new Pager();
		pager.setData(ls);
		pager.setTotal(total);
		this.reponseJson(pager, resp);
	}
	protected void downReportData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		Report report=JsonUtil.jsonToBean(req.getParameter("report"), Report.class);
		//////////////////////////
		//处理表
		String sql=ReportUtil.getSqlByReport(report, req, resp);
		
		OutputStream os=null;
		String fileName="";
		try {
			fileName = new String((report.getName() + ".xls").getBytes("gb2312"),
					"iso8859-1");
			resp.reset();
			resp.setContentType("application/x-msdownload;charset=utf-8");
			resp.addHeader("Content-Disposition", "attachment; filename=\""
					+ fileName + "\"");
			os = resp.getOutputStream();
			
			List<String[]> datas=ReportUtil.findList(sql);
			ExcelUtil.exportExcel(resp.getOutputStream(), report.getTopTitle(), datas);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(os!=null){
				try{ os.close();}catch(Exception e2){}
			}
		}
	}
	protected void getDBTree(HttpServletRequest req, HttpServletResponse resp){
		String id=req.getParameter("id");
		String ownerName=req.getParameter("owner");
		String tableName=req.getParameter("tableName");
		String colName=req.getParameter("colName");
		if(id==null||id.trim().equals("")){
			List<String> owners=ReportUtil.getDbUserNames();
			List<TableNode> ownerNodes=new ArrayList<TableNode>();
			if(null!=owners){
				for(String owner:owners){
					TableNode ownerNode=new TableNode();
					TreeAttribute attr=new TreeAttribute();
					attr.setOwner(owner);
					ownerNode.setId(owner);
					ownerNode.setAttributes(attr);
					ownerNode.setText(owner);
					ownerNodes.add(ownerNode);
				}
			}
			this.reponseJson(ownerNodes, resp);
		}else if(tableName==null||tableName.trim().equals("")){
			List<Table> tables=ReportUtil.getAllTables(id);
			List<TableNode> tbNodes=new ArrayList<TableNode>();
			if(null!=tables){
				for(Table tb:tables){
					TableNode tbNode=new TableNode();
					TreeAttribute attr=new TreeAttribute();
					attr.setOwner(ownerName);
					attr.setTableName(tb.getName());
					tbNode.setAttributes(attr);
					tbNode.setId(tb.getName());
					tbNode.setText((tb.getComments()==null||tb.getComments().equals(""))?tb.getName():tb.getComments());
					tbNode.setState("closed");
					tbNodes.add(tbNode);
				}
			}
			this.reponseJson(tbNodes, resp);
		}else if(colName==null||colName.trim().equals("")){
			List<TableNode> colNodes=new ArrayList<TableNode>();
			Table table=ReportUtil.getTableInfo(tableName, ownerName);
			if(null!=table){
				List<Column> cols=table.getCols();
				if(null!=cols){
					for(Column col:cols){
						TableNode colNode=new TableNode();
						TreeAttribute attr=new TreeAttribute();
						attr.setOwner(table.getOwner());
						attr.setTableName(table.getName());
						attr.setColName(col.getColName());
						colNode.setId(col.getColName());
						colNode.setAttributes(attr);
						colNode.setText((col.getComments()==null||col.getComments().equals(""))?col.getColName():col.getComments());
						colNode.setState("open");
						colNodes.add(colNode);
					}
				}
			}
			this.reponseJson(colNodes, resp);
		}else{
			List<TableNode> colNodes=new ArrayList<TableNode>();
			this.reponseJson(colNodes, resp);
		}
	}
	protected  void reponseJson(Object object,HttpServletResponse response) {
		try {
		String text=JsonUtil.beanToJson(object);
		//设置编码及文件格式   
		response.setContentType("text/html;charset=UTF-8");
		//设置不使用缓存   
        response.setHeader("Cache-Control","no-cache"); 
			response.getWriter().write(text);
			response.getWriter().flush();   
	        response.getWriter().close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

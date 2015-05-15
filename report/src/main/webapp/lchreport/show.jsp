<%@page import="com.lch.report.dto.Equal"%>
<%@page import="com.lch.report.dto.Button"%>
<%@page import="com.lch.report.dto.Like"%>
<%@page import="java.util.List"%>
<%@page import="com.lch.report.dto.Condition"%>
<%@page import="com.lch.report.util.JsonUtil"%>
<%@page import="com.lch.report.dto.Report"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%
	String path=request.getContextPath();
	Report r=(Report) request.getAttribute("report");
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title></title>
	<link rel="stylesheet" type="text/css" href="<%=path %>/lchreport/css/jpagination.css" />
	<link rel="stylesheet" type="text/css" href="<%=path %>/lchreport/css/report.css">
	<script type="text/javascript" src="<%=path %>/lchreport/js/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="<%=path %>/lchreport/js/jpagination.js"></script>
	<style>
		body{
			margin:0;
			padding:0;
		}
	</style>
</head>
<body>
	<input type="hidden" id="ctx" value="<%=path %>"/>
	<div id="lchContent" style="position: absolute;width: 100%; height: 100%;margin:0;padding:0;">
		<div class="condition" >
			<form id="condition">
<%
	Condition cd=r.getCondition();
	if(cd!=null){
		//模糊查询
		List<Like> likes=cd.getLikes();
		for(Like like:likes){
%>
			<%=like.getDesc() %>:<input type="text" value='' name="<%=like.getColumn()%>"/>&nbsp;
<%			
		}
		//下拉选择
		List<Equal> equals=cd.getEquals();
		for(Equal equal:equals){
%>
			<%=equal.getDesc() %>:<select  value='' cdtype='select' name="<%=equal.getColumn()%>" table="<%=equal.getTableName()%>">
			</select>
<%			
		}
	}
%>


<%
	if(cd!=null){
		List<Button> btns=cd.getButtons();
		for(Button btn:btns){
%>
			<input type="button" value='<%=btn.getValue() %>' onclick="<%=btn.getMethod()%>"/>&nbsp;
<%			
		}
	}
%>
			</form>
		</div>
		<br/>
		<br/>
		<div class="cpBody">
			<div id="editTable" style="position: absolute; overflow: hidden;">
				<table width="100%" height="100%" cellspacing=0 cellpadding=0>
					<tr>
						<td  class="title" style="border-right: solid #e7d4b3 1px;border-bottom: solid #e7d4b3 1px;">
						</td>
						<td style="border-left: none;border-top:none;background-color:white;" class="title">
							<div id="topTool" style="overflow: hidden;border:none;"></div>
						</td>
					</tr>
					<tr>
						<td class="title" style="border-right: 0px; border-top: none;vertical-align: top;background-color:white;">
							<div id="leftTool" style="overflow: hidden;border:none;"></div>
						</td>
						<td class="title" style="border-top: 0px; border-left: 0px;vertical-align: top;background-color:white;">
							<div id="tableBody" style="overflow: auto;">
								<div class="selToolBox" flag=""
									style="position: absolute; float: left; display: none; overflow: visible;"></div>
							</div>
							<div class="page_count">
									<div class="page_count_left">
										<div id="pagination"></div>
									</div>
									<div class="page_count_left">
										共有 <span id="totalCount"></span> 条数据
									</div>
								</div>
						</td>
					</tr>
				</table>
			</div>
		</div>
	</div>
</body>
<script type="text/javascript">
	$(function(){
		//初始化下拉选择框
		var url=$("#ctx").val()+"/report?action=listcolumn";
		$("#condition").find("SELECT[cdtype='select']").each(function(){
			var colName=$(this).attr("name");
			var tableName=$(this).attr("table");
			var $this=$(this);
			$.ajax({
				type:"POST",
				dataType:'json',
				async:true,
				cache:false,
				data:{column:colName,table:tableName},
				url:url,
			   	success:function(data){
			   		if(data&&data.length>0){
			   			var h='<option value="" >全部</option>';
			   			for(var i=0;i<data.length;i++){
			   				h+='<option value="'+data[i][0]+'" >'+data[i][0]+'</option>';
			   			}
			   			$this.empty().append(h);
			   		}
			    }
			});
		});
	});
</script>

<script>
		//导出数据
		function downAll(){
			var url=$("#ctx").val()+"/report?action=downreport";
			var form = $("#condition");  
			form.attr('target','');  
			form.attr('method','post');  
			form.attr('action',url); 
			if(!form.find("INPUT[name='report']").length){
				var reportInput = $('<input>');
				reportInput.attr('type','hidden');  
				reportInput.attr('name','report');  
				reportInput.attr('value',JSON.stringify(report));
				form.append(reportInput);   
			}
			form.submit();  
		}
		function ajustHead($t){
			var $body=$t.find("#tableBody").find("TABLE");
			var $top=$t.find("#topTool").find("TABLE");
			var $left=$t.find("#leftTool").find("TABLE");
			//将body的第一行的单元格的得顶边框设置为none，第一列单元格的左边框设置为none
			$body.find("TD[y='0']").css({borderTop:'none'});
			$body.find("TD[x='0']").css({borderLeft:'none'});
			$top.find("TD[x='0']").css({borderLeft:'none'});
			$left.find("TD[y='0']").css({borderTop:'none'});
			///////////////////////////////
			$t.width("100%").height("100%");
			$top.width($body.width());
			$top.find("TD[colspan='1']").each(function(i){
				var cx=$body.find("TD[x='"+i+"'][colspan='1']:visible").width();
				$(this).width(cx);
			});
			
			$left.height($body.height());
			$left.find("TD[rowspan='1']").each(function(i){
				var cx=$body.find("TD[y='"+i+"'][rowspan='1']:visible").height();
				$(this).height(cx);
			});
			////////////////////////
			var bodyWidth=$body.width();
			var bodyHeight=$body.height();
			var winH=$(window).height();
			var winW=$(window).width();
			
			//alert(bodyWidth+","+bodyHeight+","+winW+","+winH);
			if(winH>bodyHeight){
				$t.height(bodyHeight+$top.height()+$(".page_count").height()+20);
				$t.find("#tableBody").css({'overflow-y':'hidden'});
			}
			if(winW>bodyWidth){
				$t.width(bodyWidth+$left.width()+20);
				$t.find("#tableBody").css({'overflow-x':'hidden'});
			}
			$t.find("#tableBody").width($t.width()-$left.width()).height($t.height()-$top.height()-$(".page_count").height());
			////////////////////////
		}
		function createTable(cx,cy){
			var h="<table>";
			for(var j=0;j<cy;j++){
				h+="<tr>";
				for(var i=0;i<cx;i++){
					h+="<td x="+i+" y="+j+" colspan=1 rowspan=1></td>";
				}
				h+="</tr>";
			}
			h+="</table>";
			return $(h);
		}
		//参数二维数组
		function createTableTitle(headData){
				/**
			     *合并相同标题的表头，采用横向优先法
				 */
				var h="<table style='position:relative;'>";
				var flag=[];
				for(var y=0;y<headData.length;y++){
					flag[y]=[];
					for(var x=0;x<headData[0].length;x++){
						flag[y][x]=0;
					}
				}
				for(var y=0;y<headData.length;y++){
					h+='<tr>';
					for(var x=0;x<headData[0].length;x++){
						if(flag[y][x]==1) continue;
						var dx=1,dy=1;
						var t=headData[y][x];
						for(var i=1;(x+i)<headData[0].length;i++){
							if(flag[y][x+i]==1) continue;
							var tnew=headData[y][x+i];
							if(t==tnew||tnew==''){
								dx++;
							}else{
								break;
							}
						}
						for(var j=1;(y+j)<headData.length;j++){
							var eq=true;
							for(var i=0;i<dx;i++){
								var tnew=headData[y+j][x+i];
								if(/*t!=tnew&&*/tnew!=''){
									eq=false;
									break;
								}
							}
							if(eq){
								dy++;
							}else{
								break;
							}
						}
						h+='<td x="'+x+'" y="'+y+'" class="title" colspan="'+dx+'" rowspan="'+dy+'" >'+headData[y][x]+'</td>';
						
						for(var j=0;j<dy;j++)
							for(var i=0;i<dx;i++)
								flag[y+j][x+i]=1;
					}
					h+='</tr>';
				}
				h+='</table>';
				return $(h);
		}
		function getPage(num){
			var url=$("#ctx").val()+"/report?action=listreport";
			var param=$("#condition").serialize();
			param+="&pageSize="+pageSize;
			param+="&pageNumber="+num;
			$.ajax({
				url:url,
				type:'POST',
				dataType:'json',
				data:param,
				async:true,
				success:function(r){
					$("#tableBody").find("TD").text("");
					if(num==0){
						initPagination(r.total);
					}
					if(r&&r.data.length){
						var d=r.data;
						for(var j=0;j<d.length;j++){
							for(var i=0;i<d[j].length;i++){
								$("#tableBody").find("TD[x='"+i+"'][y='"+j+"']").text(d[j][i]);
							}
						}
					}
					ajustHead($editTable);
				}
			});
		}
		//分页
		function initPagination(totalCount) {
			$("#totalCount").html(totalCount);
			$("#pagination").pagination(totalCount, {
				callback : getPage,
				items_per_page : pageSize,
				link_to : "###",
				prev_text : '上页', //上一页按钮里text  
				next_text : '下页', //下一页按钮里text  
				num_display_entries : 5,
				num_edge_entries : 2
			});
		}
		var $editTable=$("#editTable");
		$editTable.css({backgroundColor:'#c2c2c2'});
		
		/*var report={"topTitle":[["A","","","","","","","","","","","","","","","",""],["AA","","","","","","","","AAA","","","","","","","","AAAA"],["1------","2-------","3---------","4-------","5----","6-----","7","8","9","10","11","12","13","14","15","16",""]],"leftTitle":[["I","1---------------"],["","2------------"],["","3-------------"],["","4"],["","5"],["","6"],["II","1"],["","2"],["","3"],["","4"],["","5"],["","6"]
		,["","7"],["","8"],["","9"],["","10"],["","11"],["","12"],["","13"],["","14"],["","15"],["","16"],["","17"],["","18"]
		]};*/
		var report=<%=JsonUtil.beanToJson(r)%>;
		
		$topTitle=createTableTitle(report.topTitle);
		$("#topTool").append($topTitle);
		
		$leftTitle=createTableTitle(report.leftTitle);
		$("#leftTool").append($leftTitle);	
		
		var $tableBody=createTable(report.topTitle[0].length,report.leftTitle.length);
		$tableBody.css({backgroundColor:'#ffffff'});
		$tableBody.find("TD").css({minWidth:'80px',maxWidth:'350px'});
		$("#tableBody").prepend($tableBody);
		
		$("#tableBody").scroll(function(){
			var lf=$(this).scrollLeft();
			var tp=$(this).scrollTop();
			$("#topTool").find("table").css({left:'-'+lf+'px'});
			$("#leftTool").find("table").css({top:'-'+tp+'px'});
			//ajustHead($editTable);
		});
		
		
		//////////////////////////////////////////
		var pageSize =<%=r.getPageSize()%>;
		$(".page_count").width("100%");
		getPage(0);
	</script>
</html>


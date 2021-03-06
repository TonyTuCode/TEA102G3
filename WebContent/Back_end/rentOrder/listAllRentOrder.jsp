<%@ page contentType="text/html; charset=UTF-8" pageEncoding="Big5"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="java.util.*"%>
<%@ page import="com.rentOrder.model.*"%>
<%-- 此頁練習採用 EL 的寫法取值 --%>

<%
	RentOrderService rentOrderSvc = new RentOrderService();
    List<RentOrderVO> list = rentOrderSvc.getAll();
    pageContext.setAttribute("list",list);
%>


<html>
<head>
<title>所有員工資料 - listAllRentOrder.jsp</title>

<style>
  table#table-1 {
	background-color: #CCCCFF;
    border: 2px solid black;
    text-align: center;
  }
  table#table-1 h4 {
    color: red;
    display: block;
    margin-bottom: 1px;
  }
  h4 {
    color: blue;
    display: inline;
  }
</style>

<style>
  table {
	width: 800px;
	background-color: white;
	margin-top: 5px;
	margin-bottom: 5px;
  }
  table, th, td {
    border: 1px solid #CCCCFF;
  }
  th, td {
    padding: 5px;
    text-align: center;
  }
</style>

</head>
<body bgcolor='white'>
<h4>此頁練習採用 EL 的寫法取值:</h4>
<table id="table-1">
	<tr><td>
		 <h3>所有員工資料 - listAllRentOrder.jsp</h3>
		 <h4><a href="<%=request.getContextPath()%>/Back_end/rentOrder/select_page.jsp"><img src="<%=request.getContextPath()%>/Back_end/employee/images/1.png" width="100" height="32" border="0">回首頁</a></h4>
	</td></tr>
</table>

<%-- 錯誤表列 --%>
<c:if test="${not empty errorMsgs}">
	<font style="color:red">請修正以下錯誤:</font>
	<ul>
		<c:forEach var="message" items="${errorMsgs}">
			<li style="color:red">${message}</li>
		</c:forEach>
	</ul>
</c:if>

<table>
	<tr>
		<th>出租單編號</th>
		<th>訂單日期</th>
		<th>訂單狀態</th>
		<th>門市id</th>
		<th>出租時間</th>
		<th>歸還時間</th>
		<th>押金</th>
		<th>總金額</th>
		<th>合約簽名</th>
		<th>坤幣</th>
		<th>會員</th>
		<th>修改</th>
		<th>刪除</th>
	</tr>
	<%@ include file="page1.file" %> 
	<c:forEach var="rentOrderVO" items="${list}" begin="<%=pageIndex%>" end="<%=pageIndex+rowsPerPage-1%>">
		
		<tr>
			<td>${rentOrderVO.ro_id}</td>
			<td>${rentOrderVO.ro_date}</td>
			<td>${rentOrderVO.ro_status}</td>
			<td>${rentOrderVO.st_id}</td>
			<td>${rentOrderVO.ro_outdate}</td>
			<td>${rentOrderVO.ro_backdate}</td>
			<td>${rentOrderVO.ro_deposit}</td>
			<td>${rentOrderVO.ro_total}</td> 
			<td><img src="${rentOrderVO.ro_sign2}" width="100" height="100"></td>
			<td>${rentOrderVO.ro_pm}</td>
			<td>${rentOrderVO.m_id}</td>
			<td>
			  <FORM METHOD="post" ACTION="<%=request.getContextPath()%>/rentOrder/RentOrderServlet" style="margin-bottom: 0px;">
			     <input type="submit" value="修改">
			     <input type="hidden" name="ro_id"  value="${rentOrderVO.ro_id}">
			     <input type="hidden" name="action"	value="getOne_For_Update"></FORM>
			</td>
			<td>
			  <FORM METHOD="post" ACTION="/rentOrder/RentOrderServlet" style="margin-bottom: 0px;">
			     <input type="submit" value="刪除">
			     <input type="hidden" name="ro_id"  value="${rentOrderVO.ro_id}">
			     <input type="hidden" name="action" value="delete"></FORM>
			</td>
		</tr>
	</c:forEach>
</table>
<%@ include file="page2.file" %>

</body>
</html>
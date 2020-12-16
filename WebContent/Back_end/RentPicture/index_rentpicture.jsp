<%@ page contentType="text/html; charset=UTF-8" pageEncoding="Big5"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>IBM Rentpicture: Home</title>

<style>
  table#table-1 {
	width: 450px;
	background-color: #CCCCFF;
	margin-top: 5px;
	margin-bottom: 10px;
    border: 3px ridge Gray;
    height: 80px;
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

</head>
<body bgcolor='white'>

<table id="table-1">
   <tr><td><h3>IBM Rentpicture: Home</h3><h4>( MVC )</h4></td></tr>
</table>

<p>This is the Home page for IBM Rentpicture: Home</p>

<h3>��Ƭd��:</h3>
	
<%-- ���~���C --%>
<c:if test="${not empty errorMsgs}">
	<font style="color:red">�Эץ��H�U���~:</font>
	<ul>
	    <c:forEach var="message" items="${errorMsgs}">
			<li style="color:red">${message}</li>
		</c:forEach>
	</ul>
</c:if>

<ul>
  <li><a href='listAllRentPicture.jsp'>List</a> all Rentpicture.  <br><br></li>
  
  
  <li>
    <FORM METHOD="post" ACTION="<%=request.getContextPath() %>/RentPictureServlet" >
        <b>��J�X���~�Ϥ��s�� (�pRP00001):</b>
        <input type="text" name="rp_id">
        <input type="hidden" name="action" value="getOne_For_Display">
        <input type="submit" value="�e�X">
    </FORM>
  </li>

  <jsp:useBean id="rentpictureSvc" scope="page" class="com.rentpicture.model.RentPictureService" />
   
  <li>
     <FORM METHOD="post" ACTION="<%=request.getContextPath() %>/RentPictureServlet" >
       <b>��ܥX���~�Ϥ��s��:</b>
       <select size="1" name="rp_id">
         <c:forEach var="rentpictureVO" items="${rentpictureSvc.all}" > 
          <option value="${rentpictureVO.rp_id}">${rentpictureVO.rp_id}
         </c:forEach>   
       </select>
       <input type="hidden" name="action" value="getOne_For_Display">
       <input type="submit" value="�e�X">
    </FORM>
  </li>
  
<!--   <li> -->
<!--      <FORM METHOD="post" ACTION="rentpicture.do" > -->
<!--        <b>��ܭ��u�m�W:</b> -->
<!--        <select size="1" name="r_name"> -->
<%--          <c:forEach var="rentVO" items="${rentSvc.all}" >  --%>
<%--           <option value="${rentVO.r_name}">${rentVO.r_name} --%>
<%--          </c:forEach>    --%>
<!--        </select> -->
<!--        <input type="hidden" name="action" value="getOne_For_Display"> -->
<!--        <input type="submit" value="�e�X"> -->
<!--      </FORM> -->
<!--   </li> -->
</ul>


<h3>�X���~�Ϥ��޲z</h3>

<ul>
  <li><a href='addRentPicture.jsp'>Add</a> a new Rentpicture.</li>
</ul>

</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<c:if test="${not empty msg}">
		<div id="message" class="alert alert-success">${msg}</div>
	</c:if>
		<table>
			<c:forEach items="${comments}" var="comment">
			<tr>
			<td>时间:${comment.time}</td>
			<td>任务:${taskNames.get(comment.taskId)}</td>
			<td>审核意见:${comment.message}</td>
			<td>  意见人:${comment.userId}</td>
			<td>
			</td>
			</tr>
			</c:forEach>
		</table>
</body>
</html>
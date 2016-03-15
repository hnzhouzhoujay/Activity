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
			<c:forEach items="${entitys}" var="u">
			<tr><td>名字</td><td>年龄</td><td>出生日期</td><td>实例ID</td><td>流程定义ID</td><td>任务ID</td><td>操作</td></tr>
			<tr>
			<td>${u.name}</td>
			<td>${u.age}</td>
			<td>${u.birthday}</td>
			<td>${u.processInstanceId}</td>
			<td>${u.processDefineId}</td>
			<td>${u.taskId}</td>
			<td>
			<a href="${pageContext.servletContext.contextPath }/user/task/${u.taskId}/view.do">办理</a>
			<c:if test="${empty u.task.assignee}">
				<a href="${pageContext.servletContext.contextPath }/user/claim.do?userId=${userId}&taskId=${u.taskId}">签收</a>
			</c:if>
			<c:if test="${not empty u.task.assignee && u.task.assignee eq userId}">
				<a href="${pageContext.servletContext.contextPath }/user/unclaim.do?userId=${userId}&taskId=${u.taskId}">反签</a>
			</c:if>
			</td>
			</tr>
			</c:forEach>
		</table>
</body>
</html>
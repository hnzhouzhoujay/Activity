<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${task.name}</title>
</head>
<body>
	<form action="${pageContext.servletContext.contextPath }/user/task/${task.id}/complete.do" method="post" >
		<table>
		<input type="hidden"  name="id" value="${u.id}"/>
			<tr>
			<td>名字</td><td>${u.name}</td>
			</tr>
			<tr>
			<td>年龄</td><td>${u.age}</td>
			</tr>
			<tr>
			<td>出生日期</td><td>${u.birthday}</td>
			</tr>
			<tr>
			<td>审核意见<input type="text" name="comment" /></td><td>
			</tr>
			<tr>
			<td>是否同意
			<select name="p_B_hrApprove" id="hrApprove">
						<option value="true">同意</option>
						<option value="false">拒绝</option>
			</select>
			</td>
			</tr>
			<tr>
			<td>
			<c:if test="${not empty task.assignee && task.assignee eq userId}">
			<td><input type="submit"  value="提交" /></td>
			</c:if>
			<c:if test="${empty task.assignee }">
			<td><a href="${pageContext.servletContext.contextPath }/user/claim.do?taskId=${task.id}&userId=${userId}&nextdo=handle" >签收</a></td>
			</c:if>
			</tr>
		</table>
		</form>
</body>
</html>
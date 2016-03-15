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
			<td>出生日期</td><td>${u.birthday}</td>
			</tr>
			<tr>
			</tr>
			<tr>
			<td><td><input type="submit"  value="提交" /></td>
			</tr>
		</table>
		</form>
</body>
</html>
<head>
    <jsp:directive.include
            file="/WEB-INF/jsp/prelude/include-head-meta.jspf"/>
    <title>Title</title>
</head>
<body>
<h2>新用户注册</h2>
<form action=<%=request.getContextPath()+"/signInSuccess"%> method="post">
    <table align="center">
        <tr align="right">
            <td>请输入用户名:</td>
            <td><input type="text" name="name" autofocus="autofocus"></td>
        </tr>
        <tr align="right">
            <td>请输入密码:</td>
            <td><input type="text" name="password"></td>
        </tr>
        <tr align="right">
            <td>请输入邮箱:</td>
            <td><input type="text" name="email"></td>
        </tr>
        <tr align="right">
            <td>请输入手机号:</td>
            <td><input type="text" name="phoneNumber"></td>
        </tr>
    </table>
    <input align="center" type="submit" name="register" value="注册">
    <input align="center" type="reset" name="refill" value="重填">
</form>
</body>

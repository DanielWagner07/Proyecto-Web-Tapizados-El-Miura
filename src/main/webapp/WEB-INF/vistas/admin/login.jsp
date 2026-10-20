<%--
  CU01 - Iniciar sesión. La procesa LoginServlet (POST /admin/login).
  Atributos que puede recibir: error (mensaje), correo (para no reescribirlo).
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ingreso al panel - Tapizados El Miura</title>
    <link rel="icon" href="${ctx}/img/favicon.png" type="image/png">
    <link rel="stylesheet" href="${ctx}/admin/css/admin.css">
</head>
<body class="pagina-login">

<main class="tarjeta-login">
    <p class="marca"><img src="${ctx}/img/logo-miura.png" alt="Tapizados El Miura" width="210" height="97"></p>
    <h1>Panel administrador</h1>

    <c:if test="${param.salida == '1'}">
        <p class="aviso aviso-exito" role="status">Cerraste sesión correctamente.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="aviso aviso-error" role="alert"><c:out value="${error}" /></p>
    </c:if>

    <form action="${ctx}/admin/login" method="post" class="formulario" novalidate>
        <label for="correo">Correo</label>
        <input type="email" id="correo" name="correo" required autocomplete="username"
               value="<c:out value='${correo}' />" autofocus>

        <label for="contrasena">Contraseña</label>
        <input type="password" id="contrasena" name="contrasena" required autocomplete="current-password">

        <button type="submit" class="boton-principal">Iniciar sesión</button>
    </form>

    <a class="volver" href="${ctx}/index.html">&larr; Volver al sitio</a>
</main>

</body>
</html>

<%--
  CU07 - Listado del catálogo en el panel. Lo prepara ProductoServlet (GET /admin/productos).
  Atributos: productos (List<Producto>), busqueda (texto buscado), error (opcional).
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="seccion" value="catalogo" />
<fmt:setLocale value="es_CO" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestor de catálogo - Tapizados El Miura</title>
    <link rel="icon" href="${pageContext.request.contextPath}/img/favicon.png" type="image/png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/admin/css/admin.css">
</head>
<body>

<%@ include file="fragmentos/cabecera.jspf" %>

<main class="contenido-admin">
    <div class="encabezado-seccion">
        <div>
            <h1>Gestor de catálogo</h1>
            <p class="subtitulo">${fn:length(productos)} producto(s)
                <c:if test="${not empty busqueda}"> para &laquo;<c:out value="${busqueda}" />&raquo;</c:if>
            </p>
        </div>
        <a class="boton-principal" href="${ctx}/admin/productos?accion=nuevo">+ Nuevo producto</a>
    </div>

    <%-- Búsqueda: formulario GET, el texto queda en la URL (?q=...) --%>
    <form class="busqueda" action="${ctx}/admin/productos" method="get" role="search">
        <label for="q" class="solo-lector">Buscar producto</label>
        <input type="search" id="q" name="q" placeholder="Buscar por nombre o referencia"
               value="<c:out value='${busqueda}' />">
        <button type="submit" class="boton-secundario">Buscar</button>
        <c:if test="${not empty busqueda}">
            <a href="${ctx}/admin/productos" class="enlace-suave">Limpiar</a>
        </c:if>
    </form>

    <c:choose>
        <c:when test="${empty productos}">
            <p class="vacio">No hay productos para mostrar.</p>
        </c:when>
        <c:otherwise>
            <div class="tabla-contenedor">
                <table class="tabla-admin">
                    <thead>
                    <tr>
                        <th scope="col">Foto</th>
                        <th scope="col">Referencia</th>
                        <th scope="col">Nombre</th>
                        <th scope="col">Categoría</th>
                        <th scope="col" class="numero">Precio</th>
                        <th scope="col" class="numero">Stock</th>
                        <th scope="col">Estado</th>
                        <th scope="col">Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr class="${p.estado ? '' : 'fila-inactiva'}">
                            <td>
                                <img class="miniatura" alt="<c:out value='${p.nombre}' />"
                                     src="${ctx}/${empty p.imagenPrincipal ? 'img/sin-imagen.svg' : p.imagenPrincipal}">
                            </td>
                            <td><c:out value="${p.referencia}" /></td>
                            <td><c:out value="${p.nombre}" /></td>
                            <td><c:out value="${p.nombreCategoria}" /></td>
                            <td class="numero">
                                <fmt:formatNumber value="${p.precio}" type="currency" currencySymbol="$" maxFractionDigits="0" />
                            </td>
                            <td class="numero">
                                <c:choose>
                                    <c:when test="${p.agotado}"><span class="etiqueta etiqueta-alerta">Agotado</span></c:when>
                                    <c:otherwise>${p.stock}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span class="etiqueta ${p.estado ? 'etiqueta-ok' : 'etiqueta-gris'}">
                                    ${p.estado ? 'Visible' : 'Oculto'}
                                </span>
                                <%-- Los productos creados por la API nacen sin foto y no se pueden mostrar hasta tenerla --%>
                                <c:if test="${empty p.imagenPrincipal}">
                                    <span class="etiqueta etiqueta-alerta">Sin foto</span>
                                </c:if>
                            </td>
                            <td>
                                <div class="acciones">
                                    <a href="${ctx}/admin/productos?accion=editar&amp;id=${p.idProducto}">Editar</a>
                                    <%-- Cambiar estado modifica datos: va por POST, nunca por un enlace GET --%>
                                    <form action="${ctx}/admin/productos" method="post">
                                        <input type="hidden" name="accion" value="estado">
                                        <input type="hidden" name="id" value="${p.idProducto}">
                                        <input type="hidden" name="activo" value="${not p.estado}">
                                        <button type="submit" class="boton-enlace">${p.estado ? 'Ocultar' : 'Mostrar'}</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="fragmentos/pie.jsp" />

</body>
</html>

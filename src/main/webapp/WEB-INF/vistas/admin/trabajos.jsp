<%--
  CU08 - Listado de la galería de trabajos. Lo prepara TrabajoServlet (GET /admin/trabajos).
  Atributos: trabajos (List<Trabajo>), tipo (filtro actual: "", "restauracion" o "colaboracion").
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="seccion" value="galeria" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Galería de trabajos - Tapizados El Miura</title>
    <link rel="icon" href="${pageContext.request.contextPath}/img/favicon.png" type="image/png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/admin/css/admin.css">
</head>
<body>

<%@ include file="fragmentos/cabecera.jspf" %>

<main class="contenido-admin">
    <div class="encabezado-seccion">
        <div>
            <h1>Galería de trabajos</h1>
            <p class="subtitulo">Restauraciones con su antes y después, y colaboraciones con otros negocios.</p>
        </div>
        <a class="boton-principal" href="${ctx}/admin/trabajos?accion=nuevo${empty tipo ? '' : '&amp;tipo='.concat(tipo)}">+ Nuevo trabajo</a>
    </div>

    <%-- Filtro por tipo: enlaces GET (?tipo=...) --%>
    <nav class="pestanas" aria-label="Filtrar por tipo">
        <a href="${ctx}/admin/trabajos" class="${empty tipo ? 'activa' : ''}">Todos</a>
        <a href="${ctx}/admin/trabajos?tipo=restauracion" class="${tipo == 'restauracion' ? 'activa' : ''}">Restauraciones</a>
        <a href="${ctx}/admin/trabajos?tipo=colaboracion" class="${tipo == 'colaboracion' ? 'activa' : ''}">Colaboraciones</a>
    </nav>

    <c:choose>
        <c:when test="${empty trabajos}">
            <p class="vacio">No hay trabajos para mostrar.</p>
        </c:when>
        <c:otherwise>
            <div class="rejilla-trabajos">
                <c:forEach var="t" items="${trabajos}">
                    <article class="tarjeta-trabajo ${t.estado ? '' : 'tarjeta-inactiva'}">
                        <%-- Restauración: antes y después lado a lado. Colaboración: primera foto. --%>
                        <div class="fotos-tarjeta ${t.restauracion ? 'fotos-dobles' : ''}">
                            <c:choose>
                                <c:when test="${t.restauracion}">
                                    <figure>
                                        <img src="${ctx}/${t.imagenAntes}" alt="Antes: <c:out value='${t.titulo}' />">
                                        <figcaption>Antes</figcaption>
                                    </figure>
                                    <figure>
                                        <img src="${ctx}/${t.imagenDespues}" alt="Después: <c:out value='${t.titulo}' />">
                                        <figcaption>Después</figcaption>
                                    </figure>
                                </c:when>
                                <c:otherwise>
                                    <figure>
                                        <img src="${ctx}/${empty t.imagenPortada ? 'img/sin-imagen.svg' : t.imagenPortada}"
                                             alt="<c:out value='${t.titulo}' />">
                                    </figure>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="cuerpo-tarjeta">
                            <p class="meta-tarjeta">
                                <span class="etiqueta ${t.restauracion ? 'etiqueta-tipo-r' : 'etiqueta-tipo-c'}">${t.tipoLegible}</span>
                                <span class="etiqueta ${t.estado ? 'etiqueta-ok' : 'etiqueta-gris'}">${t.estado ? 'Visible' : 'Oculto'}</span>
                            </p>
                            <h2><c:out value="${t.titulo}" /></h2>
                            <p class="detalle-tarjeta">
                                <c:if test="${not empty t.cliente}"><c:out value="${t.cliente}" /> &middot; </c:if>
                                <c:out value="${empty t.nombreServicio ? 'Sin servicio' : t.nombreServicio}" />
                                &middot; ${t.totalFotos} foto(s) adicional(es)
                            </p>

                            <div class="acciones">
                                <a href="${ctx}/admin/trabajos?accion=editar&amp;id=${t.idTrabajo}">Editar</a>
                                <form action="${ctx}/admin/trabajos" method="post">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${t.idTrabajo}">
                                    <input type="hidden" name="activo" value="${not t.estado}">
                                    <button type="submit" class="boton-enlace">${t.estado ? 'Ocultar' : 'Mostrar'}</button>
                                </form>
                                <%-- Eliminar es definitivo: se pide confirmación en el navegador --%>
                                <form action="${ctx}/admin/trabajos" method="post"
                                      onsubmit="return confirm('¿Eliminar este trabajo y todas sus fotos? Esta acción no se puede deshacer.');">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${t.idTrabajo}">
                                    <button type="submit" class="boton-enlace enlace-peligro">Eliminar</button>
                                </form>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="fragmentos/pie.jsp" />

</body>
</html>

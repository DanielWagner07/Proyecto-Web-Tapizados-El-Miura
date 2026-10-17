<%--
  CU09 - Gestor de información del negocio. Lo prepara InformacionServlet (GET /admin/informacion).
  Atributos: grupos (Map nombre del grupo -> List<InformacionNegocio>), errores (Map clave -> mensaje).
  Cada campo se envía como valor_<clave>, por ejemplo valor_whatsapp.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="seccion" value="informacion" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Información del negocio - Tapizados El Miura</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/admin/css/admin.css">
</head>
<body>

<%@ include file="fragmentos/cabecera.jspf" %>

<main class="contenido-admin">
    <div class="encabezado-seccion">
        <div>
            <h1>Información del negocio</h1>
            <p class="subtitulo">Datos de contacto, horarios, cobertura y textos que se muestran en el sitio.</p>
        </div>
    </div>

    <c:if test="${not empty errores}">
        <p class="aviso aviso-error" role="alert">No se guardó ningún cambio. Revise los campos marcados en rojo.</p>
    </c:if>

    <form action="${ctx}/admin/informacion" method="post" class="formulario">
        <c:forEach var="grupo" items="${grupos}">
            <fieldset>
                <legend><c:out value="${grupo.key}" /></legend>
                <div class="rejilla rejilla-2">
                    <c:forEach var="dato" items="${grupo.value}">
                        <c:set var="errorCampo" value="${errores[dato.clave]}" />
                        <div class="campo ${dato.textoLargo ? 'campo-completo' : ''} ${not empty errorCampo ? 'con-error' : ''}">
                            <label for="valor_${dato.clave}"><c:out value="${dato.etiqueta}" /></label>
                            <c:choose>
                                <c:when test="${dato.textoLargo}">
                                    <textarea id="valor_${dato.clave}" name="valor_${dato.clave}" rows="4"
                                              maxlength="2000" required>${fn:escapeXml(dato.valor)}</textarea>
                                </c:when>
                                <c:otherwise>
                                    <%-- El tipo de input ayuda al navegador a validar y a mostrar el teclado adecuado --%>
                                    <c:set var="tipoInput" value="${dato.clave == 'correo' ? 'email'
                                        : (dato.grupo == 'redes' ? 'url'
                                        : (dato.clave == 'whatsapp' || dato.clave == 'telefono' ? 'tel' : 'text'))}" />
                                    <input type="${tipoInput}" id="valor_${dato.clave}" name="valor_${dato.clave}"
                                           maxlength="2000" required value="${fn:escapeXml(dato.valor)}">
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${dato.clave == 'whatsapp'}">
                                <small>Solo números con indicativo del país. Es el número que recibe las cotizaciones.</small>
                            </c:if>
                            <small class="error">${errorCampo}</small>
                        </div>
                    </c:forEach>
                </div>
            </fieldset>
        </c:forEach>

        <c:if test="${not empty grupos}">
            <div class="botones">
                <button type="submit" class="boton-principal">Guardar cambios</button>
            </div>
        </c:if>
    </form>
</main>

<jsp:include page="fragmentos/pie.jsp" />

</body>
</html>

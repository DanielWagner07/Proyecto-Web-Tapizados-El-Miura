<%--
  Pie de página del panel. Se incorpora con la acción <jsp:include> (inclusión
  dinámica): se ejecuta como una página independiente en cada petición.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<footer class="pie-admin">
    <%-- Expresión JSP: imprime el año actual calculado en el servidor --%>
    <p>&copy; <%= java.time.Year.now() %> Tapizados El Miura &middot; Panel administrador</p>
</footer>

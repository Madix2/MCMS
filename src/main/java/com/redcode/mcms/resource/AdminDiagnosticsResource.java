package com.redcode.mcms.resource;

import com.redcode.mcms.security.AuthContext;
import com.redcode.mcms.security.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/admin/diagnostics/health")
@Produces(MediaType.APPLICATION_JSON)
@Secured
public class AdminDiagnosticsResource {
    @Inject private AuthContext authContext;

    @GET
    public Response health() {
        authContext.requireRole(AuthContext.RolePermission.ADMIN);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("service", "mcms");
        try {
            DataSource dataSource = dataSource();
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(3);
                result.put("database", valid ? "UP" : "DOWN");
                result.put("pool", dataSource.getClass().getName());
                return Response.status(valid ? Response.Status.OK : Response.Status.SERVICE_UNAVAILABLE).entity(result).build();
            }
        } catch (Exception e) {
            result.put("database", "DOWN");
            result.put("error", "Database connection validation failed");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(result).build();
        }
    }

    private DataSource dataSource() throws NamingException {
        InitialContext context = new InitialContext();
        try {
            return (DataSource) context.lookup("java:/jdbc/mcms");
        } catch (NamingException wildFlyNameNotFound) {
            return (DataSource) context.lookup("jdbc/mcms");
        }
    }
}

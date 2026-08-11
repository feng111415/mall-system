package com.ruoyi.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SecurityConfigurationValidatorTest
{
    @Test
    void rejectsMissingSecretsAndDangerousSqlSetting()
    {
        List<String> violations = SecurityConfigurationValidator.validate(Map.of(
                "spring.datasource.druid.filter.wall.config.multi-statement-allow", "true"));

        assertTrue(violations.stream().anyMatch(item -> item.contains("token.secret")));
        assertTrue(violations.stream().anyMatch(item -> item.contains("master.password")));
        assertTrue(violations.stream().anyMatch(item -> item.contains("多语句")));
    }

    @Test
    void acceptsConfiguredNonProductionSettings()
    {
        assertTrue(SecurityConfigurationValidator.validate(baseValues()).isEmpty());
    }

    @Test
    void rejectsProductionDiagnostics()
    {
        Map<String, String> values = baseValues();
        values.put("spring.profiles.active", "prod,druid");
        values.put("springdoc.swagger-ui.enabled", "true");
        values.put("springdoc.api-docs.enabled", "true");
        values.put("spring.datasource.druid.statViewServlet.enabled", "true");
        values.put("spring.datasource.druid.statViewServlet.login-username", "ops");
        values.put("spring.datasource.druid.statViewServlet.login-password", "a-long-random-password");

        List<String> violations = SecurityConfigurationValidator.validate(values);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(item -> item.contains("Swagger")));
        assertTrue(violations.stream().anyMatch(item -> item.contains("Druid")));
    }

    @Test
    void rejectsDefaultDruidCredentials()
    {
        Map<String, String> values = baseValues();
        values.put("spring.datasource.druid.statViewServlet.enabled", "true");
        values.put("spring.datasource.druid.statViewServlet.login-username", "ruoyi");
        values.put("spring.datasource.druid.statViewServlet.login-password", "123456");

        assertTrue(SecurityConfigurationValidator.validate(values).stream()
                .anyMatch(item -> item.contains("默认凭据")));
    }

    private Map<String, String> baseValues()
    {
        Map<String, String> values = new HashMap<>();
        values.put("token.secret", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        values.put("spring.datasource.druid.master.url", "jdbc:mysql://127.0.0.1:3306/test");
        values.put("spring.datasource.druid.master.username", "test-user");
        values.put("spring.datasource.druid.master.password", "test-password");
        values.put("spring.datasource.druid.statViewServlet.enabled", "false");
        values.put("spring.datasource.druid.statViewServlet.login-username", "");
        values.put("spring.datasource.druid.statViewServlet.login-password", "");
        values.put("spring.datasource.druid.filter.wall.config.multi-statement-allow", "false");
        values.put("springdoc.swagger-ui.enabled", "false");
        values.put("springdoc.api-docs.enabled", "false");
        values.put("spring.profiles.active", "druid");
        return values;
    }
}

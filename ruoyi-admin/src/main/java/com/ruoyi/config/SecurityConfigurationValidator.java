package com.ruoyi.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/** Fails startup when security-sensitive production settings are unsafe. */
@Component
public class SecurityConfigurationValidator
{
    private static final int MIN_TOKEN_SECRET_LENGTH = 64;
    private final Environment environment;

    public SecurityConfigurationValidator(Environment environment)
    {
        this.environment = environment;
    }

    @PostConstruct
    public void validateAtStartup()
    {
        Map<String, String> values = Map.ofEntries(
                Map.entry("token.secret", environment.getProperty("token.secret", "")),
                Map.entry("spring.datasource.druid.master.url",
                        environment.getProperty("spring.datasource.druid.master.url", "")),
                Map.entry("spring.datasource.druid.master.username",
                        environment.getProperty("spring.datasource.druid.master.username", "")),
                Map.entry("spring.datasource.druid.master.password",
                        environment.getProperty("spring.datasource.druid.master.password", "")),
                Map.entry("spring.datasource.druid.statViewServlet.enabled",
                        environment.getProperty("spring.datasource.druid.statViewServlet.enabled", "false")),
                Map.entry("spring.datasource.druid.statViewServlet.login-username",
                        environment.getProperty("spring.datasource.druid.statViewServlet.login-username", "")),
                Map.entry("spring.datasource.druid.statViewServlet.login-password",
                        environment.getProperty("spring.datasource.druid.statViewServlet.login-password", "")),
                Map.entry("spring.datasource.druid.filter.wall.config.multi-statement-allow",
                        environment.getProperty("spring.datasource.druid.filter.wall.config.multi-statement-allow", "false")),
                Map.entry("springdoc.swagger-ui.enabled",
                        environment.getProperty("springdoc.swagger-ui.enabled", "false")),
                Map.entry("springdoc.api-docs.enabled",
                        environment.getProperty("springdoc.api-docs.enabled", "false")),
                Map.entry("spring.profiles.active", String.join(",", environment.getActiveProfiles())));

        List<String> violations = validate(values);
        if (!violations.isEmpty())
        {
            throw new IllegalStateException("安全配置校验失败: " + String.join("; ", violations));
        }
    }

    public static List<String> validate(Map<String, String> values)
    {
        List<String> violations = new ArrayList<>();
        require(values, "token.secret", violations);
        if (value(values, "token.secret").length() < MIN_TOKEN_SECRET_LENGTH)
        {
            violations.add("token.secret 必须至少包含 64 个字符");
        }
        require(values, "spring.datasource.druid.master.url", violations);
        require(values, "spring.datasource.druid.master.username", violations);
        require(values, "spring.datasource.druid.master.password", violations);

        if (Boolean.parseBoolean(value(values, "spring.datasource.druid.filter.wall.config.multi-statement-allow")))
        {
            violations.add("禁止开启 Druid 多语句执行");
        }

        boolean production = Arrays.stream(value(values, "spring.profiles.active").split(","))
                .map(String::trim)
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
        boolean swaggerEnabled = Boolean.parseBoolean(value(values, "springdoc.swagger-ui.enabled"));
        boolean apiDocsEnabled = Boolean.parseBoolean(value(values, "springdoc.api-docs.enabled"));
        boolean druidEnabled = Boolean.parseBoolean(value(values, "spring.datasource.druid.statViewServlet.enabled"));
        if (production && (swaggerEnabled || apiDocsEnabled))
        {
            violations.add("生产环境必须关闭 Swagger");
        }
        if (production && druidEnabled)
        {
            violations.add("生产环境必须关闭 Druid 监控入口");
        }
        if (druidEnabled)
        {
            require(values, "spring.datasource.druid.statViewServlet.login-username", violations);
            require(values, "spring.datasource.druid.statViewServlet.login-password", violations);
            if ("ruoyi".equalsIgnoreCase(value(values, "spring.datasource.druid.statViewServlet.login-username"))
                    || "123456".equals(value(values, "spring.datasource.druid.statViewServlet.login-password")))
            {
                violations.add("Druid 监控账号不得使用默认凭据");
            }
        }
        return violations;
    }

    private static void require(Map<String, String> values, String key, List<String> violations)
    {
        if (value(values, key).isBlank()) violations.add(key + " 未配置");
    }

    private static String value(Map<String, String> values, String key)
    {
        return values.getOrDefault(key, "").trim();
    }
}

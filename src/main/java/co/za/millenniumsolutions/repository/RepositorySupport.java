package co.za.millenniumsolutions.repository;

import org.springframework.jdbc.core.JdbcTemplate;

abstract class RepositorySupport {
    protected final JdbcTemplate jdbc;
    protected RepositorySupport(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    protected int update(String sql, Object... args) { return jdbc.update(sql, args); }
}

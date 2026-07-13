package com.sharedbasket.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DBCheckRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DBCheckRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM app_user", Integer.class);
        System.out.println("[dev-db-check] app_user rows: " + count);
    }
}
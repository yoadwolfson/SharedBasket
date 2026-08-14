package com.sharedbasket.service;

import com.sharedbasket.service.impl.CookieAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:dev-auth-profile-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false"
    }
)
@ActiveProfiles("dev")
class DevAuthenticationProfileIntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void devProfileUsesCookieAuthenticationService() {
        assertThat(authenticationService).isInstanceOf(CookieAuthenticationService.class);
    }
}
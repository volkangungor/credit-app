package com.volco.creditapp.api.rest;

import com.volco.creditapp.application.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.volco.creditapp.api.rest.Constants.ADMIN_PATH;
import static com.volco.creditapp.api.rest.Constants.BASE_TOKEN_PATH;
import static com.volco.creditapp.api.rest.Constants.CUSTOMER_PATH;

@RestController
@RequestMapping(BASE_TOKEN_PATH)
public class TokenGeneratorController{

    @SneakyThrows
    @Operation(
            tags = "Token Generators",
            summary = "Get Token for Admin"
    )
    @GetMapping(
            value = ADMIN_PATH
    )
    public ResponseEntity<String> getAdminToken() {
        return ResponseEntity.ok(
                JwtUtils.createJwt("admin1", List.of("ROLE_ADMIN"), null)
        );
    }

    @SneakyThrows
    @Operation(
            tags = "Token Generators",
            summary = "Get Token for Customer"
    )
    @GetMapping(
            value = CUSTOMER_PATH
    )
    public ResponseEntity<String> getCustomerToken(@PathVariable @Positive Long customerId) {
        return ResponseEntity.ok(
                JwtUtils.createJwt("customer1", List.of("ROLE_CUSTOMER"), customerId.toString())
        );
    }
}
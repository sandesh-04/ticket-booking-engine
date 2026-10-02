package com.example.ticketbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Ticket Booking Engine")
                        .description("Allocates seats across multiple ticket blocks, "
                                + "soonest-closing block first, with all-or-nothing booking.")
                        .version("1.0"));
    }
}

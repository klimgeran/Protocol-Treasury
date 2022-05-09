package com.github.klimgeran.symbol.discord.treasury;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;

@Configuration
public class BotConfiguration {

    @Value("${token}")
    private String token;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(token)
          .build()
          .login()
          .block();
    }
}
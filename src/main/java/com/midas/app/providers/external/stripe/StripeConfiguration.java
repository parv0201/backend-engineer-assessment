package com.midas.app.providers.external.stripe;

import com.stripe.StripeClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("stripe")
public class StripeConfiguration {
  @NonNull private String apiKey;

  @Bean
  public StripeClient stripeClient() {
    return StripeClient.builder().setApiKey(apiKey).build();
  }
}

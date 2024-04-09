package com.midas.app.providers.payment;

import com.midas.generated.model.ProviderTypeEnumDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

  private final PaymentProvider stripePaymentProvider;

  /**
   * create creates an object of PaymentProvider based on the payment provider type
   *
   * @param providerType payment provider type (ex stripe)
   * @return PaymentProvider
   */
  public PaymentProvider create(ProviderTypeEnumDto providerType) {
    switch (providerType) {
      case STRIPE:
      default:
        return stripePaymentProvider;
    }
  }
}

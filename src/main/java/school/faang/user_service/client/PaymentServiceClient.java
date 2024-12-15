package school.faang.user_service.client;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "payment-service", url = "${payment-service.host}:${payment-service.port}")
public interface PaymentServiceClient {

}

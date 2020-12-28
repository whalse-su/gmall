package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}

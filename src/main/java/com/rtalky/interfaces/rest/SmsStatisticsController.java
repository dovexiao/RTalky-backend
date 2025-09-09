package com.rtalky.interfaces.rest;

import com.rtalky.application.service.SmsStatisticsAppService;
import com.rtalky.domain.model.SmsStatistics;
import com.rtalky.interfaces.converter.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sms/statistics")
@RequiredArgsConstructor
@Tag(name = "短信统计", description = "短信发送统计相关接口")
public class SmsStatisticsController {

    private final SmsStatisticsAppService smsStatisticsAppService;
    private final ResponseBuilder responseBuilder;

    @GetMapping("/{phoneNumber}/{deviceFingerprint}")
    @Operation(summary = "获取手机号+设备的统计信息")
    public ResponseEntity<?> getStatistics(
            @Parameter(description = "手机号") @PathVariable String phoneNumber,
            @Parameter(description = "设备指纹") @PathVariable String deviceFingerprint) {
        
        SmsStatistics statistics = smsStatisticsAppService.getStatistics(phoneNumber, deviceFingerprint);
        return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "获取成功"));
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "获取手机号的所有设备统计")
    public ResponseEntity<?> getStatisticsByPhoneNumber(
            @Parameter(description = "手机号") @PathVariable String phoneNumber) {
        
        List<SmsStatistics> statistics = smsStatisticsAppService.getStatisticsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "获取成功"));
    }

    @GetMapping("/device/{deviceFingerprint}")
    @Operation(summary = "获取设备的所有手机号统计")
    public ResponseEntity<?> getStatisticsByDeviceFingerprint(
            @Parameter(description = "设备指纹") @PathVariable String deviceFingerprint) {
        
        List<SmsStatistics> statistics = smsStatisticsAppService.getStatisticsByDeviceFingerprint(deviceFingerprint);
        return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "获取成功"));
    }

    @GetMapping("/overall")
    @Operation(summary = "获取总体统计信息")
    public ResponseEntity<?> getOverallStatistics() {
        
        SmsStatistics statistics = smsStatisticsAppService.getOverallStatistics();
        return ResponseEntity.ok(responseBuilder.buildSimpleResponse(true, "获取成功"));
    }
}

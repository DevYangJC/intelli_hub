package com.intellihub.governance.controller;

import com.intellihub.common.ApiResponse;
import com.intellihub.governance.dto.CallLogDTO;
import com.intellihub.governance.service.CallLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 调用日志接收控制器
 * <p>
 * 接收Gateway上报的调用日志
 * </p>
 *
 * @author intellihub
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/governance/v1/logs")
@RequiredArgsConstructor
public class CallLogController {

    private final CallLogService callLogService;

    /**
     * 接收调用日志（Gateway调用）
     */
    @PostMapping("/report")
    public ApiResponse<Void> reportLog(@RequestBody CallLogDTO callLog) {
        callLogService.saveCallLog(callLog);
        return ApiResponse.success(null);
    }

    /**
     * 批量接收调用日志
     */
    @PostMapping("/report/batch")
    public ApiResponse<Void> reportLogs(@RequestBody java.util.List<CallLogDTO> callLogs) {
        for (CallLogDTO callLog : callLogs) {
            callLogService.saveCallLog(callLog);
        }
        return ApiResponse.success(null);
    }
}

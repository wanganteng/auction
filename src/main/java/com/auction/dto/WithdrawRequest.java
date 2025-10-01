package com.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 提现请求DTO
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "提现请求")
public class WithdrawRequest {

    @Schema(description = "提现金额（元）", example = "50.00")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0.01元")
    private Long amount;

    @Schema(description = "提现描述", example = "保证金提现")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}

package com.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

/**
 * 出价请求DTO
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "出价请求")
public class BidRequest {

    @Schema(description = "拍卖ID", example = "1")
    @NotNull(message = "拍卖ID不能为空")
    private Long auctionId;

    @Schema(description = "出价金额（元）", example = "1500.00")
    @NotNull(message = "出价金额不能为空")
    @DecimalMin(value = "0.01", message = "出价金额必须大于0.01元")
    private Long bidAmount;
}

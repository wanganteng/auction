package com.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 创建拍卖商品请求DTO
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建拍卖商品请求")
public class CreateAuctionItemRequest {

    @Schema(description = "商品名称", example = "明代青花瓷瓶")
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String itemName;

    @Schema(description = "分类ID", example = "1")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "商品描述", example = "明代青花瓷瓶，保存完好，具有很高的收藏价值")
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "商品图片（JSON数组）", example = "[\"image1.jpg\", \"image2.jpg\"]")
    private String images;

    @Schema(description = "起拍价（元）", example = "1000.00")
    @NotNull(message = "起拍价不能为空")
    @DecimalMin(value = "0.01", message = "起拍价必须大于0.01元")
    private Long startPrice;

    @Schema(description = "保留价（元）", example = "5000.00")
    @DecimalMin(value = "0.01", message = "保留价必须大于0.01元")
    private Long reservePrice;

    @Schema(description = "保证金金额（元）", example = "100.00")
    @NotNull(message = "保证金金额不能为空")
    @DecimalMin(value = "0.01", message = "保证金金额必须大于0.01元")
    private Long depositAmount;

    @Schema(description = "加价幅度（元）", example = "10.00")
    @DecimalMin(value = "0.01", message = "加价幅度必须大于0.01元")
    private Long incrementAmount;
}

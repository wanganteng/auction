package com.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建拍卖会请求DTO
 * 
 * @author auction-system
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建拍卖会请求")
public class CreateAuctionSessionRequest {

    @Schema(description = "拍卖会名称", example = "2024春季艺术品拍卖会")
    @NotBlank(message = "拍卖会名称不能为空")
    @Size(max = 200, message = "拍卖会名称长度不能超过200个字符")
    private String sessionName;

    @Schema(description = "拍卖会描述", example = "本次拍卖会汇集了众多珍贵艺术品，包括书画、陶瓷、玉器等")
    @Size(max = 2000, message = "拍卖会描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "开始时间", example = "2024-03-01T10:00:00")
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是未来时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-03-01T18:00:00")
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是未来时间")
    private LocalDateTime endTime;
}

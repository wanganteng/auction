package com.auction.controller;

import com.auction.common.Result;
import com.auction.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notices")
@Tag(name = "参拍须知")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/overview")
    @Operation(summary = "参拍须知聚合列表")
    public Result<List<Map<String, Object>>> overview() {
        try {
            return Result.success("ok", noticeService.getOverview());
        } catch (Exception e) {
            log.error("获取参拍须知失败: {}", e.getMessage(), e);
            return Result.success("ok", new ArrayList<>());
        }
    }
}



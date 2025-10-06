package com.auction.controller;

import com.auction.common.Result;
import com.auction.entity.NoticeCategory;
import com.auction.mapper.NoticeCategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/notices")
@Tag(name = "参拍须知管理")
public class AdminNoticeController {

    @Autowired
    private NoticeCategoryMapper categoryMapper;

    @GetMapping("/categories")
    @Operation(summary = "分类列表")
    public Result<List<NoticeCategory>> categories() {
        return Result.success("ok", categoryMapper.selectAll());
    }

    @PostMapping("/categories")
    @Operation(summary = "新增分类")
    public Result<Long> createCategory(@RequestBody NoticeCategory c) {
        if (c.getEnabled() == null) c.setEnabled(true);
        if (c.getSortOrder() == null) c.setSortOrder(0);
        categoryMapper.insert(c);
        return Result.success("ok", c.getId());
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "更新分类")
    public Result<String> updateCategory(@PathVariable Long id, @RequestBody NoticeCategory c) {
        c.setId(id);
        categoryMapper.update(c);
        return Result.success("ok");
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除分类")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success("ok");
    }
}



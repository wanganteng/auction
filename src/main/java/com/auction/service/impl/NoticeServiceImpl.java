package com.auction.service.impl;

import com.auction.entity.NoticeCategory;
import com.auction.entity.NoticeItem;
import com.auction.mapper.NoticeCategoryMapper;
import com.auction.mapper.NoticeItemMapper;
import com.auction.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;
    @Autowired
    private NoticeItemMapper noticeItemMapper;

    @Override
    public List<Map<String, Object>> getOverview() {
        List<NoticeCategory> categories = noticeCategoryMapper.selectAll();
        // 简化：直接使用分类的 content_html

        List<Map<String, Object>> result = new ArrayList<>();
        for (NoticeCategory c : categories) {
            if (Boolean.FALSE.equals(c.getEnabled())) continue;
            Map<String, Object> cat = new HashMap<>();
            cat.put("category", c.getName());
            cat.put("content", c.getContentHtml());
            result.add(cat);
        }
        return result;
    }
}



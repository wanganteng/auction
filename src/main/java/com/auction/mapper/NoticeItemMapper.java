package com.auction.mapper;

import com.auction.entity.NoticeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeItemMapper {
    int insert(NoticeItem item);
    int update(NoticeItem item);
    int deleteById(Long id);
    NoticeItem selectById(Long id);
    List<NoticeItem> selectByCategoryId(@Param("categoryId") Long categoryId);
    List<NoticeItem> selectEnabledAll();
}



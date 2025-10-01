package com.auction.mapper;

import com.auction.entity.NoticeCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeCategoryMapper {
    int insert(NoticeCategory category);
    int update(NoticeCategory category);
    int deleteById(Long id);
    NoticeCategory selectById(Long id);
    List<NoticeCategory> selectAll();
}



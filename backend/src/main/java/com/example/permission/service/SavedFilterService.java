package com.example.permission.service;

import com.example.permission.entity.SavedFilter;
import com.example.permission.mapper.SavedFilterMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.SavedFilterTableDef.SAVED_FILTER;

@Service
public class SavedFilterService {

    @Autowired
    private SavedFilterMapper savedFilterMapper;

    public List<SavedFilter> listByUserId(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .from(SavedFilter.class)
                .where(SAVED_FILTER.USER_ID.eq(userId))
                .orderBy(SAVED_FILTER.CREATE_TIME.desc());
        return savedFilterMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(SavedFilter filter) {
        filter.setCreateTime(LocalDateTime.now());
        filter.setUpdateTime(LocalDateTime.now());
        savedFilterMapper.insert(filter);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SavedFilter filter) {
        filter.setUpdateTime(LocalDateTime.now());
        savedFilterMapper.update(filter);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        savedFilterMapper.deleteById(id);
    }

    public SavedFilter getById(Long id) {
        return savedFilterMapper.selectOneById(id);
    }
}

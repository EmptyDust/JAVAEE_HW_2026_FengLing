package com.student.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.student.entity.Dict;
import com.student.student.mapper.DictMapper;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    public List<Dict> getByType(String dictType) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictType, dictType);
        wrapper.orderByAsc(Dict::getSort);
        return dictMapper.selectList(wrapper);
    }
}

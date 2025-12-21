package com.student.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.student.entity.Class;
import com.student.student.mapper.ClassMapper;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ClassService {

    @Autowired
    private ClassMapper classMapper;

    public Page<Class> list(int page, int size) {
        Page<Class> pageParam = new Page<>(page, size);
        return classMapper.selectPage(pageParam, null);
    }

    public void add(Class classEntity) {
        classMapper.insert(classEntity);
    }

    public void update(Class classEntity) {
        classMapper.updateById(classEntity);
    }

    public void delete(Long id) {
        classMapper.deleteById(id);
    }
}

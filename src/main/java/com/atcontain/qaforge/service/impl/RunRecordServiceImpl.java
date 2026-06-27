package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.RunRecord;
import com.atcontain.qaforge.mapper.RunRecordMapper;
import com.atcontain.qaforge.service.RunRecordService;
import com.atcontain.qaforge.vo.RunRecordDataVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RunRecordServiceImpl extends ServiceImpl<RunRecordMapper, RunRecord> implements RunRecordService {
    RunRecordMapper runRecordMapper;

    public RunRecordServiceImpl(RunRecordMapper runRecordMapper) {
        this.runRecordMapper = runRecordMapper;
    }

    @Override
    public RunRecordDataVO listData(Integer projectId, String status, String runType) {
        RunRecordDataVO runRecordDataVO = runRecordMapper.listData(projectId, status, runType);
        return runRecordDataVO;
    }
}

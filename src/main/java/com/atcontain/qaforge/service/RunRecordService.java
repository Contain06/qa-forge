package com.atcontain.qaforge.service;

import com.atcontain.qaforge.entity.RunRecord;
import com.atcontain.qaforge.vo.RunRecordDataVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RunRecordService extends IService<RunRecord> {
    RunRecordDataVO listData(Integer projectId, String status, String runType);
}

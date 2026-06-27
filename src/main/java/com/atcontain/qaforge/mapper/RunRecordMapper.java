package com.atcontain.qaforge.mapper;

import com.atcontain.qaforge.entity.RunRecord;
import com.atcontain.qaforge.vo.RunRecordDataVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RunRecordMapper extends BaseMapper<RunRecord> {
    RunRecordDataVO listData(Integer projectId, String status, String runType);

}

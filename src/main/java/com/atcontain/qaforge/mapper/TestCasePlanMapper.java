package com.atcontain.qaforge.mapper;

import com.atcontain.qaforge.entity.TestPlanCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestCasePlanMapper extends BaseMapper<TestPlanCase> {

    /**
     * 物理删除测试计划下的所有用例绑定（绕过 @TableLogic 逻辑删除）。
     * bind-cases 采用"先清空再重新绑定"模式，逻辑删除会导致唯一键冲突。
     */
    @Delete("DELETE FROM test_plan_case WHERE plan_id = #{planId}")
    int physicalDeleteByPlanId(@Param("planId") Integer planId);
}

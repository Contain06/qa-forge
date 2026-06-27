package com.atcontain.qaforge.service;

import com.atcontain.qaforge.vo.CaseExecuteResultVO;

public interface CaseExecuteService {

    CaseExecuteResultVO execute(Integer caseId, Integer envId, Integer userId);
}

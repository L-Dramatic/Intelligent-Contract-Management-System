package com.software.contract_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.entity.WfInstance;
import com.software.contract_system.entity.WfTask;
import com.software.contract_system.mapper.WfInstanceMapper;
import com.software.contract_system.mapper.WfTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private WfInstanceMapper instanceMapper;
    
    @Autowired
    private WfTaskMapper taskMapper;

    @GetMapping("/check-remark")
    public Map<String, Object> checkRemark() {
        Map<String, Object> result = new HashMap<>();
        try {
            jdbcTemplate.execute("SELECT remark FROM wf_instance LIMIT 1");
            result.put("status", "success");
            result.put("message", "Column 'remark' exists.");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/fix-remark")
    public Map<String, Object> fixRemark() {
        Map<String, Object> result = new HashMap<>();
        try {
            jdbcTemplate.execute("ALTER TABLE wf_instance ADD COLUMN remark VARCHAR(100) COMMENT '备注'");
            result.put("status", "success");
            result.put("message", "Column 'remark' added successfully.");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Failed to add column: " + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/check-updated-at")
    public Map<String, Object> checkUpdatedAt() {
        Map<String, Object> result = new HashMap<>();
        try {
            jdbcTemplate.execute("SELECT updated_at FROM t_contract LIMIT 1");
            result.put("status", "success");
            result.put("message", "Column 'updated_at' exists.");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/test-history/{contractId}")
    public Map<String, Object> testHistory(@PathVariable Long contractId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Step 1: 查询流程实例
            WfInstance instance = instanceMapper.selectOne(new LambdaQueryWrapper<WfInstance>()
                    .eq(WfInstance::getContractId, contractId)
                    .orderByDesc(WfInstance::getStartTime)
                    .last("LIMIT 1"));
            
            if (instance == null) {
                result.put("step1", "No instance found for contractId: " + contractId);
                result.put("status", "no_instance");
                return result;
            }
            result.put("step1", "Instance found: id=" + instance.getId() + ", scenarioId=" + instance.getScenarioId());
            
            // Step 2: 查询任务
            List<WfTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<WfTask>()
                    .eq(WfTask::getInstanceId, instance.getId())
                    .orderByAsc(WfTask::getCreateTime));
            
            result.put("step2", "Tasks found: " + tasks.size());
            result.put("tasks", tasks);
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}

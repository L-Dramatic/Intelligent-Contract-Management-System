package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.software.contract_system.dto.WorkflowDefinitionDTO;
import com.software.contract_system.entity.WfScenarioConfig;
import com.software.contract_system.mapper.WfScenarioConfigMapper;
import com.software.contract_system.service.WorkflowDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程定义服务实现
 * 基于场景配置表聚合生成流程模板视图
 */
@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {
    
    @Autowired
    private WfScenarioConfigMapper scenarioConfigMapper;
    
    // 合同大类定义
    private static final Map<String, String> CONTRACT_TYPE_NAMES = new LinkedHashMap<>();
    static {
        CONTRACT_TYPE_NAMES.put("A", "工程施工合同");
        CONTRACT_TYPE_NAMES.put("B", "代维服务合同");
        CONTRACT_TYPE_NAMES.put("C", "IT/DICT合同");
    }
    
    // 合同子类型到大类的映射
    private static final Map<String, String> SUB_TYPE_TO_MAIN = new HashMap<>();
    static {
        SUB_TYPE_TO_MAIN.put("A1", "A");
        SUB_TYPE_TO_MAIN.put("A2", "A");
        SUB_TYPE_TO_MAIN.put("A3", "A");
        SUB_TYPE_TO_MAIN.put("B1", "B");
        SUB_TYPE_TO_MAIN.put("B2", "B");
        SUB_TYPE_TO_MAIN.put("B3", "B");
        SUB_TYPE_TO_MAIN.put("B4", "B");
        SUB_TYPE_TO_MAIN.put("C1", "C");
        SUB_TYPE_TO_MAIN.put("C2", "C");
        SUB_TYPE_TO_MAIN.put("C3", "C");
    }
    
    // 子类型名称
    private static final Map<String, String> SUB_TYPE_NAMES = new HashMap<>();
    static {
        SUB_TYPE_NAMES.put("A1", "土建工程");
        SUB_TYPE_NAMES.put("A2", "装修工程");
        SUB_TYPE_NAMES.put("A3", "零星维修");
        SUB_TYPE_NAMES.put("B1", "光缆代维");
        SUB_TYPE_NAMES.put("B2", "基站代维");
        SUB_TYPE_NAMES.put("B3", "家宽代维");
        SUB_TYPE_NAMES.put("B4", "应急保障");
        SUB_TYPE_NAMES.put("C1", "定制开发");
        SUB_TYPE_NAMES.put("C2", "商用软件采购");
        SUB_TYPE_NAMES.put("C3", "DICT集成");
    }
    
    @Override
    public List<WorkflowDefinitionDTO> getAllDefinitions() {
        // 查询所有场景配置
        List<WfScenarioConfig> allScenarios = scenarioConfigMapper.selectList(
            new LambdaQueryWrapper<WfScenarioConfig>()
                .orderByAsc(WfScenarioConfig::getSubTypeCode)
                .orderByAsc(WfScenarioConfig::getAmountMin)
        );
        
        // 按合同大类聚合
        Map<String, List<WfScenarioConfig>> groupedByMainType = new HashMap<>();
        for (WfScenarioConfig scenario : allScenarios) {
            String subType = scenario.getSubTypeCode();
            String mainType = SUB_TYPE_TO_MAIN.getOrDefault(subType, "OTHER");
            groupedByMainType.computeIfAbsent(mainType, k -> new ArrayList<>()).add(scenario);
        }
        
        // 生成流程定义列表
        List<WorkflowDefinitionDTO> result = new ArrayList<>();
        long id = 1;
        
        for (Map.Entry<String, String> entry : CONTRACT_TYPE_NAMES.entrySet()) {
            String mainType = entry.getKey();
            String typeName = entry.getValue();
            List<WfScenarioConfig> scenarios = groupedByMainType.getOrDefault(mainType, Collections.emptyList());
            
            if (scenarios.isEmpty()) continue;
            
            WorkflowDefinitionDTO dto = new WorkflowDefinitionDTO();
            dto.setId(id++);
            dto.setName(typeName + "审批流程");
            dto.setDescription("适用于" + typeName + "的审批流程，包含" + scenarios.size() + "个审批场景");
            
            // 获取所有子类型
            List<String> subTypes = scenarios.stream()
                .map(WfScenarioConfig::getSubTypeCode)
                .distinct()
                .collect(Collectors.toList());
            dto.setApplicableContractTypes(subTypes);
            
            // 生成条件表达式
            String conditions = subTypes.stream()
                .map(st -> "subType == '" + st + "'")
                .collect(Collectors.joining(" || "));
            dto.setConditionExpression(conditions);
            
            dto.setVersion(1);
            dto.setEnabled(scenarios.stream().anyMatch(s -> s.getIsActive() == 1));
            dto.setScenarioCount(scenarios.size());
            
            // 取最新的更新时间
            LocalDateTime latestUpdate = scenarios.stream()
                .map(WfScenarioConfig::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
            dto.setUpdateTime(latestUpdate);
            dto.setCreateTime(latestUpdate.minusDays(30)); // 模拟创建时间
            
            result.add(dto);
        }
        
        // 添加兜底流程
        List<WfScenarioConfig> fallbackScenarios = allScenarios.stream()
            .filter(s -> s.getScenarioId() != null && s.getScenarioId().contains("FALLBACK"))
            .collect(Collectors.toList());
        
        if (!fallbackScenarios.isEmpty()) {
            WorkflowDefinitionDTO fallbackDto = new WorkflowDefinitionDTO();
            fallbackDto.setId(id);
            fallbackDto.setName("兜底审批流程");
            fallbackDto.setDescription("当没有匹配的专用流程时，使用此兜底流程");
            fallbackDto.setApplicableContractTypes(List.of("*"));
            fallbackDto.setConditionExpression("default");
            fallbackDto.setVersion(1);
            fallbackDto.setEnabled(true);
            fallbackDto.setScenarioCount(fallbackScenarios.size());
            fallbackDto.setCreateTime(LocalDateTime.now().minusDays(30));
            fallbackDto.setUpdateTime(LocalDateTime.now());
            result.add(fallbackDto);
        }
        
        return result;
    }
    
    @Override
    public WorkflowDefinitionDTO getDefinitionById(Long id) {
        List<WorkflowDefinitionDTO> all = getAllDefinitions();
        return all.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}


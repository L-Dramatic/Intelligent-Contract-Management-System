package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.software.contract_system.dto.ContractChangeDTO;
import com.software.contract_system.dto.ContractChangeVO;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.ContractChange;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.common.BusinessException;
import com.software.contract_system.mapper.ContractChangeMapper;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.ContractChangeService;
import com.software.contract_system.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 合同变更服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractChangeServiceImpl implements ContractChangeService {

    private final ContractChangeMapper changeMapper;
    private final ContractMapper contractMapper;
    private final SysUserMapper userMapper;
    private final WorkflowService workflowService;

    // 变更类型映射
    private static final Map<String, String> CHANGE_TYPE_MAP = Map.of(
            "AMOUNT", "金额变更",
            "TIME", "时间变更",
            "TECH", "技术方案变更",
            "ATTACHMENT", "附件补充",
            "CONTACT", "联系人变更",
            "OTHER", "其他"
    );

    // 变更原因映射
    private static final Map<String, String> REASON_TYPE_MAP = Map.of(
            "ACTIVE", "本方主动",
            "PASSIVE", "应乙方要求",
            "FORCE_MAJEURE", "不可抗力",
            "POLICY", "政策调整",
            "OTHER", "其他"
    );

    // 状态映射
    private static final Map<Integer, String> STATUS_MAP = Map.of(
            0, "草稿",
            1, "审批中",
            2, "已通过",
            3, "已驳回",
            4, "已撤销"
    );

    @Override
    @Transactional
    public ContractChange createChange(ContractChangeDTO dto, Long initiatorId) {
        log.info("创建变更申请: contractId={}, initiatorId={}", dto.getContractId(), initiatorId);

        // 1. 检查合同是否可以变更
        if (!canCreateChange(dto.getContractId())) {
            throw BusinessException.badRequest("该合同当前不可发起变更，可能已有变更在审批中或合同状态不允许变更");
        }

        // 2. 获取原合同
        Contract contract = contractMapper.selectById(dto.getContractId());
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }

        // 3. 生成变更单号和版本号
        String changeNo = generateChangeNo();
        String changeVersion = generateNextVersion(dto.getContractId());

        // 4. 计算变更对比数据
        Map<String, Object> diffData = calculateDiffData(contract, dto);

        // 5. 计算金额差额和是否重大变更
        BigDecimal amountDiff = BigDecimal.ZERO;
        boolean isMajorChange = false;

        if (dto.getNewAmount() != null && contract.getAmount() != null) {
            amountDiff = dto.getNewAmount().subtract(contract.getAmount());
            // 计算变更百分比
            if (contract.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal changePercent = amountDiff.abs()
                        .divide(contract.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                // 金额变更超过20%为重大变更
                if (changePercent.compareTo(BigDecimal.valueOf(20)) > 0) {
                    isMajorChange = true;
                }
            }
        }

        // 技术方案变更也算重大变更
        if ("TECH".equals(dto.getChangeType())) {
            isMajorChange = true;
        }

        // 6. 创建变更记录
        ContractChange change = new ContractChange();
        change.setContractId(dto.getContractId());
        change.setChangeNo(changeNo);
        change.setTitle(dto.getTitle());
        change.setChangeVersion(changeVersion);
        change.setChangeType(dto.getChangeType());
        change.setReasonType(dto.getReasonType());
        change.setDescription(dto.getDescription());
        change.setPartyBCommunication(dto.getPartyBCommunication());
        change.setDiffData(diffData);
        change.setIsMajorChange(isMajorChange);
        change.setAmountDiff(amountDiff);
        change.setStatus(0); // 草稿状态
        change.setInitiatorId(initiatorId);
        change.setAttachmentPath(dto.getAttachmentPath());

        changeMapper.insert(change);
        log.info("变更申请创建成功: changeId={}, changeNo={}", change.getId(), changeNo);

        return change;
    }

    @Override
    @Transactional
    public void submitChange(Long changeId, Long userId) {
        log.info("提交变更审批: changeId={}, userId={}", changeId, userId);

        ContractChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw BusinessException.notFound("变更申请不存在");
        }

        if (!change.getInitiatorId().equals(userId)) {
            throw BusinessException.forbidden("只有发起人可以提交审批");
        }

        if (change.getStatus() != 0) {
            throw BusinessException.badRequest("只有草稿状态的变更可以提交");
        }

        // 获取原合同信息用于匹配审批流程
        Contract contract = contractMapper.selectById(change.getContractId());

        // 创建审批流程实例
        // 如果是重大变更，需要增加法务会签
        Long instanceId = workflowService.startContractChangeWorkflow(
                changeId,
                contract.getType(),
                change.getIsMajorChange(),
                userId
        );

        // 更新变更状态
        change.setStatus(1); // 审批中
        change.setInstanceId(instanceId);
        changeMapper.updateById(change);

        log.info("变更审批提交成功: changeId={}, instanceId={}", changeId, instanceId);
    }

    @Override
    @Transactional
    public void cancelChange(Long changeId, Long userId) {
        log.info("撤销变更申请: changeId={}, userId={}", changeId, userId);

        ContractChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw BusinessException.notFound("变更申请不存在");
        }

        if (!change.getInitiatorId().equals(userId)) {
            throw BusinessException.forbidden("只有发起人可以撤销");
        }

        if (change.getStatus() != 0 && change.getStatus() != 1) {
            throw BusinessException.badRequest("只有草稿或审批中的变更可以撤销");
        }

        // 如果在审批中，需要终止流程实例
        if (change.getStatus() == 1 && change.getInstanceId() != null) {
            workflowService.terminateInstance(change.getInstanceId(), "发起人撤销变更申请");
        }

        change.setStatus(4); // 已撤销
        changeMapper.updateById(change);

        log.info("变更申请已撤销: changeId={}", changeId);
    }

    @Override
    @Transactional
    public void applyChange(Long changeId) {
        log.info("应用变更到合同: changeId={}", changeId);

        ContractChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw BusinessException.notFound("变更申请不存在");
        }

        if (change.getStatus() != 2) {
            throw BusinessException.badRequest("只有已通过的变更可以应用");
        }

        Contract contract = contractMapper.selectById(change.getContractId());
        if (contract == null) {
            throw BusinessException.notFound("原合同不存在");
        }

        // 从diffData中提取变更后的值并应用
        Map<String, Object> diffData = change.getDiffData();
        if (diffData != null) {
            Map<String, Object> afterContent = (Map<String, Object>) diffData.get("afterContent");
            if (afterContent != null) {
                if (afterContent.containsKey("name")) {
                    contract.setName((String) afterContent.get("name"));
                }
                if (afterContent.containsKey("amount")) {
                    Object amountObj = afterContent.get("amount");
                    if (amountObj instanceof Number) {
                        contract.setAmount(new BigDecimal(amountObj.toString()));
                    }
                }
                if (afterContent.containsKey("content")) {
                    contract.setContent((String) afterContent.get("content"));
                }
                if (afterContent.containsKey("partyB")) {
                    contract.setPartyB((String) afterContent.get("partyB"));
                }
                if (afterContent.containsKey("attributes")) {
                    contract.setAttributes((Map<String, Object>) afterContent.get("attributes"));
                }
            }
        }

        // 更新合同版本
        contract.setVersion(change.getChangeVersion());
        contractMapper.updateById(contract);

        // 更新变更生效时间
        change.setEffectiveAt(LocalDateTime.now());
        changeMapper.updateById(change);

        log.info("变更已应用到合同: contractId={}, newVersion={}", contract.getId(), contract.getVersion());
    }

    @Override
    public ContractChangeVO getChangeDetail(Long changeId) {
        ContractChange change = changeMapper.selectById(changeId);
        if (change == null) {
            return null;
        }
        return convertToVO(change);
    }

    @Override
    public List<ContractChangeVO> getChangeHistory(Long contractId) {
        List<ContractChange> changes = changeMapper.selectByContractId(contractId);
        return changes.stream().map(this::convertToVO).toList();
    }

    @Override
    public IPage<ContractChangeVO> pageChanges(Page<ContractChange> page, Long contractId, Integer status, Long initiatorId) {
        LambdaQueryWrapper<ContractChange> wrapper = new LambdaQueryWrapper<>();
        
        if (contractId != null) {
            wrapper.eq(ContractChange::getContractId, contractId);
        }
        if (status != null) {
            wrapper.eq(ContractChange::getStatus, status);
        }
        if (initiatorId != null) {
            wrapper.eq(ContractChange::getInitiatorId, initiatorId);
        }
        
        wrapper.orderByDesc(ContractChange::getCreatedAt);
        
        IPage<ContractChange> resultPage = changeMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ContractChangeVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream().map(this::convertToVO).toList());
        
        return voPage;
    }

    @Override
    public boolean canCreateChange(Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }

        // 只有已生效的合同可以变更
        if (contract.getStatus() != 2) {
            return false;
        }

        // 检查是否有正在审批中的变更
        int pendingCount = changeMapper.countPendingChanges(contractId);
        return pendingCount == 0;
    }

    @Override
    public boolean isMajorChange(ContractChangeDTO dto, Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }

        // 技术方案变更算重大变更
        if ("TECH".equals(dto.getChangeType())) {
            return true;
        }

        // 金额变更超过20%算重大变更
        if (dto.getNewAmount() != null && contract.getAmount() != null 
                && contract.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amountDiff = dto.getNewAmount().subtract(contract.getAmount()).abs();
            BigDecimal changePercent = amountDiff.divide(contract.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            return changePercent.compareTo(BigDecimal.valueOf(20)) > 0;
        }

        return false;
    }

    // ========== 私有方法 ==========

    /**
     * 生成变更单号
     */
    private String generateChangeNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "BG-" + dateStr + "-" + random;
    }

    /**
     * 生成下一个版本号
     */
    private String generateNextVersion(Long contractId) {
        String latestVersion = changeMapper.selectLatestVersion(contractId);
        if (latestVersion == null) {
            // 原合同是v1.0，第一次变更是v2.0
            return "v2.0";
        }
        
        // 解析版本号并递增
        try {
            String numStr = latestVersion.replace("v", "").split("\\.")[0];
            int num = Integer.parseInt(numStr);
            return "v" + (num + 1) + ".0";
        } catch (Exception e) {
            return "v2.0";
        }
    }

    /**
     * 计算变更对比数据
     */
    private Map<String, Object> calculateDiffData(Contract contract, ContractChangeDTO dto) {
        Map<String, Object> diffData = new HashMap<>();
        
        // 变更前内容
        Map<String, Object> beforeContent = new HashMap<>();
        beforeContent.put("name", contract.getName());
        beforeContent.put("amount", contract.getAmount());
        beforeContent.put("content", contract.getContent());
        beforeContent.put("partyB", contract.getPartyB());
        beforeContent.put("attributes", contract.getAttributes());
        
        // 变更后内容
        Map<String, Object> afterContent = new HashMap<>();
        afterContent.put("name", dto.getNewName() != null ? dto.getNewName() : contract.getName());
        afterContent.put("amount", dto.getNewAmount() != null ? dto.getNewAmount() : contract.getAmount());
        afterContent.put("content", dto.getNewContent() != null ? dto.getNewContent() : contract.getContent());
        afterContent.put("partyB", dto.getNewPartyB() != null ? dto.getNewPartyB() : contract.getPartyB());
        afterContent.put("attributes", dto.getNewAttributes() != null ? dto.getNewAttributes() : contract.getAttributes());
        
        diffData.put("beforeContent", beforeContent);
        diffData.put("afterContent", afterContent);
        
        return diffData;
    }

    /**
     * 转换为VO
     */
    private ContractChangeVO convertToVO(ContractChange change) {
        ContractChangeVO vo = new ContractChangeVO();
        vo.setId(change.getId());
        vo.setContractId(change.getContractId());
        vo.setChangeNo(change.getChangeNo());
        vo.setTitle(change.getTitle());
        vo.setChangeVersion(change.getChangeVersion());
        vo.setChangeType(change.getChangeType());
        vo.setChangeTypeName(CHANGE_TYPE_MAP.getOrDefault(change.getChangeType(), "未知"));
        vo.setReasonType(change.getReasonType());
        vo.setReasonTypeName(REASON_TYPE_MAP.getOrDefault(change.getReasonType(), "未知"));
        vo.setDescription(change.getDescription());
        vo.setPartyBCommunication(change.getPartyBCommunication());
        vo.setDiffData(change.getDiffData());
        vo.setIsMajorChange(change.getIsMajorChange());
        vo.setAmountDiff(change.getAmountDiff());
        vo.setStatus(change.getStatus());
        vo.setStatusName(STATUS_MAP.getOrDefault(change.getStatus(), "未知"));
        vo.setInstanceId(change.getInstanceId());
        vo.setInitiatorId(change.getInitiatorId());
        vo.setCreatedAt(change.getCreatedAt());
        vo.setApprovedAt(change.getApprovedAt());
        vo.setEffectiveAt(change.getEffectiveAt());
        vo.setAttachmentPath(change.getAttachmentPath());

        // 获取合同信息
        Contract contract = contractMapper.selectById(change.getContractId());
        if (contract != null) {
            vo.setContractNo(contract.getContractNo());
            vo.setContractName(contract.getName());
        }

        // 获取发起人信息
        SysUser user = userMapper.selectById(change.getInitiatorId());
        if (user != null) {
            vo.setInitiatorName(user.getRealName());
        }

        // 计算变更百分比
        if (change.getAmountDiff() != null && contract != null && contract.getAmount() != null 
                && contract.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            double percent = change.getAmountDiff().abs()
                    .divide(contract.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            vo.setChangePercent(percent);
        }

        // 格式化对比项
        vo.setDiffItems(formatDiffItems(change.getDiffData()));

        return vo;
    }

    /**
     * 格式化对比项
     */
    private List<ContractChangeVO.DiffItem> formatDiffItems(Map<String, Object> diffData) {
        List<ContractChangeVO.DiffItem> items = new ArrayList<>();
        if (diffData == null) {
            return items;
        }

        Map<String, Object> beforeContent = (Map<String, Object>) diffData.get("beforeContent");
        Map<String, Object> afterContent = (Map<String, Object>) diffData.get("afterContent");

        if (beforeContent == null || afterContent == null) {
            return items;
        }

        // 字段标签映射
        Map<String, String> fieldLabels = Map.of(
                "name", "合同名称",
                "amount", "合同金额",
                "content", "合同正文",
                "partyB", "乙方",
                "attributes", "扩展属性"
        );

        for (String field : fieldLabels.keySet()) {
            Object before = beforeContent.get(field);
            Object after = afterContent.get(field);

            // 只记录有变化的字段
            if (!Objects.equals(before, after)) {
                ContractChangeVO.DiffItem item = new ContractChangeVO.DiffItem();
                item.setFieldName(field);
                item.setFieldLabel(fieldLabels.get(field));
                item.setBeforeValue(before);
                item.setAfterValue(after);

                // 生成变更描述
                if ("amount".equals(field) && before instanceof Number && after instanceof Number) {
                    BigDecimal beforeAmount = new BigDecimal(before.toString());
                    BigDecimal afterAmount = new BigDecimal(after.toString());
                    BigDecimal diff = afterAmount.subtract(beforeAmount);
                    String sign = diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                    item.setChangeDesc(sign + diff.setScale(2, RoundingMode.HALF_UP) + "元");
                } else if ("content".equals(field)) {
                    item.setChangeDesc("正文已变更");
                } else {
                    item.setChangeDesc("已变更");
                }

                items.add(item);
            }
        }

        return items;
    }
    
    @Override
    public List<Contract> getChangeableContracts(Long userId) {
        // 1. 获取用户创建的已生效合同
        List<Contract> userContracts = contractMapper.selectList(
                new LambdaQueryWrapper<Contract>()
                        .eq(Contract::getCreatorId, userId)
                        .eq(Contract::getStatus, 2) // 已生效
        );
        
        // 2. 获取用户参与审批的合同（通过wf_task和wf_instance关联）
        // 这里简化实现：查询用户作为审批人处理过的任务对应的合同
        List<Contract> approvedContracts = contractMapper.selectChangeableContractsByApprover(userId);
        
        // 3. 合并去重
        Set<Long> contractIds = new HashSet<>();
        List<Contract> result = new ArrayList<>();
        
        for (Contract c : userContracts) {
            if (contractIds.add(c.getId())) {
                // 检查是否可以变更（没有进行中的变更）
                if (canCreateChange(c.getId())) {
                    result.add(c);
                }
            }
        }
        
        for (Contract c : approvedContracts) {
            if (contractIds.add(c.getId())) {
                // 检查是否可以变更（没有进行中的变更）
                if (canCreateChange(c.getId())) {
                    result.add(c);
                }
            }
        }
        
        return result;
    }
}


package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.common.BusinessException;
import com.software.contract_system.common.ContractStatus;
import com.software.contract_system.dto.ContractDTO;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.WfInstance;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.mapper.WfInstanceMapper;
import com.software.contract_system.service.ContractService;
import com.software.contract_system.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Autowired
    private SecurityUtils securityUtils;
    
    @Autowired
    private WfInstanceMapper wfInstanceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createContract(ContractDTO dto) {
        Contract contract = new Contract();
        
        // 1. 复制基本字段
        contract.setName(dto.getName());
        contract.setType(dto.getType());
        if (StringUtils.hasText(dto.getPartyA())) {
            contract.setPartyA(dto.getPartyA());
        } else {
            contract.setPartyA("中国电信XX省分公司"); // 默认甲方
        }
        contract.setPartyB(dto.getPartyB());
        contract.setAmount(dto.getAmount());
        contract.setContent(dto.getContent());
        contract.setAttributes(dto.getAttributes()); // 扩展字段 Map
        
        // 2. 生成合同编号: HT-类型-UUID (避免高并发重复)
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String contractNo = "HT-" + dto.getType() + "-" + uuid;
        contract.setContractNo(contractNo);

        // 3. 设置状态：统一创建为草稿状态
        // 提交审批必须通过 WorkflowController.submit() 进行，避免状态不一致
        contract.setStatus(ContractStatus.DRAFT);
        
        // 4. 获取当前登录用户 (创建人)
        Long currentUserId = securityUtils.getCurrentUserId();
        contract.setCreatorId(currentUserId);
        
        // 5. 默认设置
        contract.setIsAiGenerated(0); // 默认非AI，AI生成的会单独标记
        contract.setVersion("v1.0"); // 初始版本

        // 6. 保存
        this.save(contract);
        return contract.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContract(ContractDTO dto) {
        // 1. 检查是否存在
        Contract oldContract = this.getById(dto.getId());
        if (oldContract == null) {
            throw BusinessException.notFound("合同不存在");
        }

        // 2. 状态检查：只有草稿或已驳回状态才能修改
        if (!ContractStatus.canEdit(oldContract.getStatus())) {
            throw BusinessException.badRequest(
                "当前状态[" + ContractStatus.getStatusName(oldContract.getStatus()) + 
                "]不可修改合同，请先撤回或等待审批结束"
            );
        }

        // 3. 更新字段
        oldContract.setName(dto.getName());
        oldContract.setPartyB(dto.getPartyB());
        oldContract.setAmount(dto.getAmount());
        oldContract.setContent(dto.getContent());
        oldContract.setAttributes(dto.getAttributes());
        
        // 注意：更新合同后保持草稿状态，不自动提交审批
        // 提交审批必须通过 WorkflowController.submit() 进行

        return this.updateById(oldContract);
    }

    @Override
    public IPage<Contract> getContractPage(int pageNum, int pageSize, String name, String type) {
        Page<Contract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        
        // 动态拼接查询条件
        wrapper.like(StringUtils.hasText(name), Contract::getName, name); // 名字模糊查
        wrapper.eq(StringUtils.hasText(type), Contract::getType, type);   // 类型精确查
        
        // 按创建时间倒序
        wrapper.orderByDesc(Contract::getCreatedAt);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteContract(Long id) {
        // 1. 检查合同是否存在
        Contract contract = this.getById(id);
        if (contract == null) {
            throw BusinessException.notFound("合同不存在");
        }
        
        // 2. 检查状态：只能删除草稿或已驳回的合同
        if (!ContractStatus.canEdit(contract.getStatus())) {
            throw BusinessException.badRequest(
                "只能删除草稿或已驳回的合同，当前状态[" + 
                ContractStatus.getStatusName(contract.getStatus()) + "]不可删除"
            );
        }
        
        // 3. 级联检查：是否存在工作流实例
        Long instanceCount = wfInstanceMapper.selectCount(
            new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getContractId, id)
        );
        
        if (instanceCount > 0) {
            throw BusinessException.badRequest(
                "该合同存在 " + instanceCount + " 条审批记录，不允许删除。" +
                "如需删除，请先清理相关审批记录或联系管理员。"
            );
        }
        
        // 4. 执行删除
        return this.removeById(id);
    }

}
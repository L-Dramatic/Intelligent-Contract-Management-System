package com.software.contract_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.software.contract_system.dto.ContractDTO;
import com.software.contract_system.entity.Contract;
import com.software.contract_system.entity.SysUser;
import com.software.contract_system.mapper.ContractMapper;
import com.software.contract_system.mapper.SysUserMapper;
import com.software.contract_system.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
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
        
        // 2. 生成合同编号: HT-类型-时间戳 (例如: HT-BASE_STATION-20251212101010)
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String contractNo = "HT-" + dto.getType() + "-" + timeStr;
        contract.setContractNo(contractNo);

        // 3. 设置状态 (前端传 isDraft=true 则是草稿0，否则直接提交审批1)
        // 注意：目前我们还没接工作流，先都默认存为草稿(0)或审批中(1)
        contract.setStatus(Boolean.TRUE.equals(dto.getIsDraft()) ? 0 : 1);
        
        // 4. 获取当前登录用户 (创建人)
        Long currentUserId = getCurrentUserId();
        contract.setCreatorId(currentUserId);
        
        // 5. 默认设置
        contract.setIsAiGenerated(0); // 默认非AI，AI生成的会单独标记
        contract.setVersion("v1.0"); // 初始版本

        // 6. 保存
        this.save(contract);
        return contract.getId();
    }

    @Override
    public Boolean updateContract(ContractDTO dto) {
        // 1. 检查是否存在
        Contract oldContract = this.getById(dto.getId());
        if (oldContract == null) {
            throw new RuntimeException("合同不存在");
        }

        // 2. 权限/状态检查
        // 只有[草稿0]或[已驳回3]状态才能修改
        if (oldContract.getStatus() != 0 && oldContract.getStatus() != 3) {
            throw new RuntimeException("当前状态不可修改合同，请先撤回或等待审批结束");
        }

        // 3. 更新字段
        oldContract.setName(dto.getName());
        oldContract.setPartyB(dto.getPartyB());
        oldContract.setAmount(dto.getAmount());
        oldContract.setContent(dto.getContent());
        oldContract.setAttributes(dto.getAttributes());
        
        // 如果是从草稿提交为审批中
        if (Boolean.FALSE.equals(dto.getIsDraft())) {
            oldContract.setStatus(1); // 变更为审批中
        }

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

    /**
     * 辅助方法：从 Security 上下文中获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("未登录");
        }
        String username = (String) auth.getPrincipal(); // Filter里存的是 username
        // 根据用户名查ID (这里可以用缓存优化，现在先查库)
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户数据异常");
        }
        return user.getId();
    }
}
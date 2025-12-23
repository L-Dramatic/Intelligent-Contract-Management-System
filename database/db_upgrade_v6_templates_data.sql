/*
 Description: 合同模板初始数据
 Notes: 10个子类型的标准合同模板
*/

USE contract_system;

-- 清空旧数据
DELETE FROM t_contract_template;

-- =======================================================
-- TYPE_A 采购类模板
-- =======================================================

-- A1 土建工程合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_A', 'A1', '土建工程施工合同', '适用于基站建设、传输管线、机房土建等工程施工项目',
'# 土建工程施工合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方（发包方）**: {{partyA}}
**地址**: {{partyAAddress}}
**联系人**: {{partyAContact}}
**电话**: {{partyAPhone}}

**乙方（承包方）**: {{partyB}}
**地址**: {{partyBAddress}}
**联系人**: {{partyBContact}}
**电话**: {{partyBPhone}}

---

## 第一条 工程概况

1.1 **工程名称**: {{projectName}}

1.2 **工程地点**: {{projectLocation}}

1.3 **工程内容**: {{projectScope}}

1.4 **工程规模**: {{projectScale}}

---

## 第二条 合同价款

2.1 **合同总价**: 人民币 {{amount}} 元（大写：{{amountInWords}}）

2.2 **价款构成**:
- 直接工程费: {{directCost}} 元
- 措施项目费: {{measureCost}} 元
- 其他项目费: {{otherCost}} 元
- 规费: {{regulatoryCost}} 元
- 税金: {{tax}} 元

2.3 **计价方式**: {{pricingMethod}}

---

## 第三条 工期

3.1 **计划开工日期**: {{startDate}}

3.2 **计划竣工日期**: {{endDate}}

3.3 **总工期**: {{duration}} 天

---

## 第四条 质量标准

4.1 工程质量应符合国家现行《建筑工程施工质量验收统一标准》（GB 50300）及相关专业验收规范。

4.2 工程质量等级目标: {{qualityTarget}}

---

## 第五条 安全文明施工

5.1 乙方应严格遵守国家和地方有关安全生产、文明施工的法律法规。

5.2 **安全生产费**: 本工程安全生产费为合同价款的 {{safetyFeeRate}}%，计 {{safetyFee}} 元。

5.3 乙方应建立健全安全生产责任制，配备专职安全管理人员。

---

## 第六条 付款方式

6.1 **预付款**: 合同签订后 {{prepaymentDays}} 天内，甲方向乙方支付合同价款的 {{prepaymentRate}}%，计 {{prepaymentAmount}} 元。

6.2 **进度款**: 按月支付，每月 {{progressPaymentDay}} 日前支付上月完成工程量的 {{progressPaymentRate}}%。

6.3 **竣工结算款**: 工程竣工验收合格后 {{settlementDays}} 天内支付至合同价款的 {{settlementRate}}%。

6.4 **质保金**: 预留合同价款的 {{warrantyRate}}% 作为质保金，质保期满后 {{warrantyReleaseDays}} 天内无息返还。

---

## 第七条 违约责任

7.1 **甲方违约责任**:
- 甲方未按合同约定支付工程款的，每逾期一天，应向乙方支付逾期金额的 {{overdueRate}}‰ 的违约金。

7.2 **乙方违约责任**:
- 乙方未按合同工期完工的，每逾期一天，应向甲方支付合同价款的 {{delayRate}}‰ 的违约金。
- 因乙方原因导致工程质量不合格，乙方应无条件返工并承担由此产生的费用。

---

## 第八条 争议解决

8.1 本合同在履行过程中发生争议，双方应首先通过友好协商解决。

8.2 协商不成的，双方同意提交甲方所在地人民法院诉讼解决。

---

## 第九条 其他约定

{{otherTerms}}

---

## 第十条 合同生效

本合同自双方法定代表人或授权代表签字并加盖公章之日起生效。

**甲方（盖章）**:                    **乙方（盖章）**:

法定代表人/授权代表:                法定代表人/授权代表:

日期:                              日期:
',
'{"editable": ["partyB", "partyBAddress", "partyBContact", "partyBPhone", "projectName", "projectLocation", "projectScope", "projectScale", "amount", "startDate", "endDate", "duration", "qualityTarget", "safetyFeeRate", "prepaymentRate", "progressPaymentRate", "settlementRate", "warrantyRate", "otherTerms"], "required": ["partyB", "projectName", "amount", "startDate", "endDate"]}',
'{"locked": ["第四条", "第七条", "第八条"]}',
1, 1, 'v1.0');

-- A2 装修工程合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_A', 'A2', '装修工程施工合同', '适用于营业厅装修、办公场所装修等项目',
'# 装修工程施工合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方（发包方）**: {{partyA}}
**乙方（承包方）**: {{partyB}}

---

## 第一条 工程概况

1.1 **工程名称**: {{projectName}}
1.2 **工程地点**: {{projectLocation}}
1.3 **装修面积**: {{area}} 平方米
1.4 **装修内容**: {{decorationScope}}

---

## 第二条 合同价款

2.1 **合同总价**: 人民币 {{amount}} 元
2.2 **单价**: {{unitPrice}} 元/平方米

---

## 第三条 工期

3.1 **开工日期**: {{startDate}}
3.2 **竣工日期**: {{endDate}}
3.3 **总工期**: {{duration}} 天

---

## 第四条 质量标准

4.1 装修工程质量应符合《建筑装饰装修工程质量验收规范》（GB 50210）。
4.2 所用材料应符合国家环保标准，提供合格证明。

---

## 第五条 付款方式

5.1 **预付款**: {{prepaymentRate}}%
5.2 **进度款**: {{progressPaymentRate}}%
5.3 **验收款**: {{acceptanceRate}}%
5.4 **质保金**: {{warrantyRate}}%，质保期 {{warrantyPeriod}} 年

---

## 第六条 保修条款

6.1 保修期自竣工验收合格之日起计算。
6.2 保修期限：
- 防水工程：{{waterproofWarranty}} 年
- 电气管线、给排水管道：{{pipeWarranty}} 年
- 其他装修工程：{{otherWarranty}} 年

---

## 第七条 违约责任

（标准条款，不可修改）

7.1 甲方逾期付款的，每日按逾期金额的0.5‰支付违约金。
7.2 乙方逾期交工的，每日按合同总价的0.5‰支付违约金。

---

## 第八条 争议解决

本合同争议由甲方所在地人民法院管辖。

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "projectName", "projectLocation", "area", "decorationScope", "amount", "unitPrice", "startDate", "endDate", "duration", "prepaymentRate", "progressPaymentRate", "acceptanceRate", "warrantyRate", "warrantyPeriod"]}',
'{"locked": ["第七条", "第八条"]}',
1, 1, 'v1.0');

-- A3 零星维修合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_A', 'A3', '零星维修服务合同', '适用于小额维修、日常维护等项目',
'# 零星维修服务合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方**: {{partyA}}
**乙方**: {{partyB}}

---

## 第一条 服务内容

1.1 **服务范围**: {{serviceScope}}
1.2 **服务地点**: {{serviceLocation}}
1.3 **服务期限**: {{startDate}} 至 {{endDate}}

---

## 第二条 费用及支付

2.1 **合同总价**: 人民币 {{amount}} 元
2.2 **计费方式**: {{billingMethod}}
2.3 **付款方式**: {{paymentMethod}}

---

## 第三条 服务要求

3.1 乙方接到维修通知后，应在 {{responseTime}} 小时内到达现场。
3.2 维修完成后，乙方应清理现场并提交维修报告。

---

## 第四条 验收标准

维修完成后由甲方验收，验收合格后签署确认单。

---

## 第五条 违约责任

5.1 乙方未按时响应，每次扣减合同金额的 {{penaltyRate}}%。
5.2 维修质量不合格，乙方应免费返工。

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "serviceScope", "serviceLocation", "startDate", "endDate", "amount", "billingMethod", "paymentMethod", "responseTime"]}',
'{"locked": ["第五条"]}',
1, 1, 'v1.0');

-- =======================================================
-- TYPE_B 工程类模板
-- =======================================================

-- B1 光缆代维合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_B', 'B1', '光缆线路代维服务合同', '适用于光缆线路日常维护、故障抢修等服务',
'# 光缆线路代维服务合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方（委托方）**: {{partyA}}
**乙方（服务方）**: {{partyB}}

---

## 第一条 代维范围

1.1 **代维区域**: {{maintenanceArea}}
1.2 **光缆类型**: {{cableType}}
1.3 **光缆总长度**: {{cableLength}} 公里
1.4 **接头盒数量**: {{jointBoxCount}} 个

---

## 第二条 服务内容

2.1 日常巡检：每 {{inspectionInterval}} 天巡检一次
2.2 故障处理：接报后 {{faultResponseTime}} 小时内到达现场
2.3 应急抢修：重大故障 {{emergencyTime}} 小时内恢复
2.4 定期割接配合

---

## 第三条 合同价款

3.1 **年度代维费**: 人民币 {{amount}} 元
3.2 **单价**: {{unitPrice}} 元/公里/年

---

## 第四条 服务期限

4.1 服务期限：{{startDate}} 至 {{endDate}}
4.2 合同期限：{{contractTerm}} 年

---

## 第五条 考核指标

5.1 **故障率**: 不超过 {{faultRate}} 次/百公里/年
5.2 **故障修复及时率**: 不低于 {{repairRate}}%
5.3 **巡检覆盖率**: 不低于 {{inspectionRate}}%

---

## 第六条 付款方式

6.1 按季度支付，每季度末支付当季费用的 {{quarterlyRate}}%
6.2 年终考核合格后支付剩余款项

---

## 第七条 违约责任

7.1 故障响应超时，每次扣款 {{overtimePenalty}} 元
7.2 考核不达标，按比例扣减年度费用

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "maintenanceArea", "cableType", "cableLength", "jointBoxCount", "inspectionInterval", "faultResponseTime", "emergencyTime", "amount", "unitPrice", "startDate", "endDate", "contractTerm", "faultRate", "repairRate", "inspectionRate"]}',
'{"locked": ["第七条"]}',
1, 1, 'v1.0');

-- B2 基站代维合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_B', 'B2', '基站代维服务合同', '适用于基站设备日常维护、巡检等服务',
'# 基站代维服务合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方**: {{partyA}}
**乙方**: {{partyB}}

---

## 第一条 代维范围

1.1 **代维区域**: {{maintenanceArea}}
1.2 **基站数量**: {{stationCount}} 个
1.3 **基站类型**: {{stationType}}

---

## 第二条 服务内容

2.1 设备日常巡检
2.2 故障处理与恢复
2.3 配合网络优化
2.4 代维报表编制

---

## 第三条 合同价款

3.1 **年度代维费**: 人民币 {{amount}} 元
3.2 **单站单价**: {{unitPrice}} 元/站/年

---

## 第四条 考核指标（SLA）

4.1 **设备可用率**: 不低于 {{availabilityRate}}%
4.2 **故障修复时长**: 平均不超过 {{repairTime}} 小时
4.3 **巡检完成率**: 不低于 {{inspectionRate}}%

---

## 第五条 付款方式

按季度支付，年终考核结算。

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "maintenanceArea", "stationCount", "stationType", "amount", "unitPrice", "availabilityRate", "repairTime", "inspectionRate"]}',
'{"locked": []}',
1, 1, 'v1.0');

-- B3 家宽代维合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_B', 'B3', '家庭宽带代维服务合同', '适用于家庭宽带安装、维护等服务',
'# 家庭宽带代维服务合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方**: {{partyA}}
**乙方**: {{partyB}}

---

## 第一条 服务范围

1.1 **服务区域**: {{serviceArea}}
1.2 **服务用户数**: 约 {{userCount}} 户
1.3 **服务内容**: 新装、移机、故障修复、设备更换

---

## 第二条 合同价款

2.1 **合同总价**: 人民币 {{amount}} 元
2.2 **计费方式**: {{billingMethod}}

---

## 第三条 服务要求

3.1 新装/移机：{{installTime}} 小时内完成
3.2 故障修复：{{repairTime}} 小时内完成
3.3 用户满意度：不低于 {{satisfactionRate}}%

---

## 第四条 付款方式

{{paymentTerms}}

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "serviceArea", "userCount", "amount", "billingMethod", "installTime", "repairTime", "satisfactionRate", "paymentTerms"]}',
'{"locked": []}',
1, 1, 'v1.0');

-- B4 应急保障合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_B', 'B4', '通信保障服务合同', '适用于重大活动通信保障、应急抢修等服务',
'# 通信保障服务合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方**: {{partyA}}
**乙方**: {{partyB}}

---

## 第一条 保障任务

1.1 **保障活动**: {{eventName}}
1.2 **保障地点**: {{eventLocation}}
1.3 **保障时间**: {{startDate}} 至 {{endDate}}

---

## 第二条 服务内容

2.1 应急通信车部署
2.2 现场技术支撑
2.3 网络监控与调度
2.4 故障快速响应

---

## 第三条 合同价款

3.1 **保障费用**: 人民币 {{amount}} 元
3.2 **费用构成**: {{costBreakdown}}

---

## 第四条 保障要求

4.1 **人员配置**: {{staffing}}
4.2 **设备配置**: {{equipment}}
4.3 **响应时间**: {{responseTime}} 分钟内

---

## 第五条 验收标准

保障任务完成后，双方共同进行验收评估。

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "eventName", "eventLocation", "startDate", "endDate", "amount", "costBreakdown", "staffing", "equipment", "responseTime"]}',
'{"locked": []}',
1, 1, 'v1.0');

-- =======================================================
-- TYPE_C 服务类模板
-- =======================================================

-- C1 定制开发合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_C', 'C1', '软件定制开发合同', '适用于软件系统定制开发、二次开发等项目',
'# 软件定制开发合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方（委托方）**: {{partyA}}
**乙方（开发方）**: {{partyB}}

---

## 第一条 项目概述

1.1 **项目名称**: {{projectName}}
1.2 **项目背景**: {{projectBackground}}
1.3 **开发目标**: {{projectGoal}}

---

## 第二条 开发内容

2.1 **功能需求**: {{functionalRequirements}}
2.2 **技术要求**: {{technicalRequirements}}
2.3 **交付物清单**:
- 源代码
- 技术文档
- 用户手册
- 部署包

---

## 第三条 合同价款

3.1 **合同总价**: 人民币 {{amount}} 元
3.2 **付款方式**:
- 合同签订后支付 {{phase1Rate}}%
- 需求确认后支付 {{phase2Rate}}%
- 系统上线后支付 {{phase3Rate}}%
- 验收合格后支付 {{phase4Rate}}%

---

## 第四条 开发周期

4.1 **需求分析**: {{requirementDays}} 工作日
4.2 **设计阶段**: {{designDays}} 工作日
4.3 **开发阶段**: {{developmentDays}} 工作日
4.4 **测试阶段**: {{testingDays}} 工作日
4.5 **总工期**: {{totalDays}} 工作日

---

## 第五条 知识产权

5.1 本项目开发成果的知识产权归甲方所有。
5.2 乙方不得将项目成果用于其他用途或向第三方披露。

---

## 第六条 质量保证

6.1 **免费维护期**: {{maintenancePeriod}} 个月
6.2 维护期内免费修复程序缺陷
6.3 提供 {{supportHours}} 小时/工作日的技术支持

---

## 第七条 违约责任

7.1 乙方延期交付，每日按合同金额 {{delayRate}}‰ 支付违约金
7.2 甲方延期付款，每日按逾期金额 {{overdueRate}}‰ 支付违约金

---

## 第八条 保密条款

双方应对项目相关的技术资料、商业信息等保密，保密期限为合同终止后 {{confidentialityPeriod}} 年。

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "projectName", "projectBackground", "projectGoal", "functionalRequirements", "technicalRequirements", "amount", "phase1Rate", "phase2Rate", "phase3Rate", "phase4Rate", "requirementDays", "designDays", "developmentDays", "testingDays", "totalDays", "maintenancePeriod", "supportHours"]}',
'{"locked": ["第五条", "第八条"]}',
1, 1, 'v1.0');

-- C2 商用软件采购合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_C', 'C2', '商用软件采购合同', '适用于商用软件许可、SaaS服务等采购',
'# 商用软件采购合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方（采购方）**: {{partyA}}
**乙方（供应方）**: {{partyB}}

---

## 第一条 采购内容

1.1 **软件名称**: {{softwareName}}
1.2 **版本号**: {{version}}
1.3 **授权类型**: {{licenseType}}
1.4 **授权数量**: {{licenseCount}} 个用户/节点
1.5 **授权期限**: {{licensePeriod}}

---

## 第二条 合同价款

2.1 **合同总价**: 人民币 {{amount}} 元
2.2 **费用明细**:
- 软件许可费: {{licenseFee}} 元
- 技术服务费: {{serviceFee}} 元
- 培训费: {{trainingFee}} 元

---

## 第三条 交付与验收

3.1 **交付方式**: {{deliveryMethod}}
3.2 **交付时间**: 合同签订后 {{deliveryDays}} 个工作日内
3.3 **验收标准**: {{acceptanceCriteria}}

---

## 第四条 技术服务

4.1 **服务期限**: {{servicePeriod}}
4.2 **服务内容**: {{serviceContent}}
4.3 **响应时间**: {{responseTime}}

---

## 第五条 知识产权

5.1 软件著作权归乙方所有
5.2 甲方获得约定范围内的使用权
5.3 甲方不得复制、转让或反向工程

---

## 第六条 付款方式

{{paymentTerms}}

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "softwareName", "version", "licenseType", "licenseCount", "licensePeriod", "amount", "licenseFee", "serviceFee", "trainingFee", "deliveryMethod", "deliveryDays", "acceptanceCriteria", "servicePeriod", "serviceContent", "responseTime", "paymentTerms"]}',
'{"locked": ["第五条"]}',
1, 1, 'v1.0');

-- C3 DICT集成合同模板
INSERT INTO `t_contract_template` 
(`type`, `sub_type_code`, `name`, `description`, `content`, `fields_schema`, `locked_sections`, `is_default`, `is_active`, `version`) 
VALUES 
('TYPE_C', 'C3', 'DICT项目集成合同', '适用于数字化转型、ICT集成项目',
'# DICT项目集成合同

**合同编号**: {{contractNo}}

## 合同双方

**甲方**: {{partyA}}
**乙方**: {{partyB}}

---

## 第一条 项目概述

1.1 **项目名称**: {{projectName}}
1.2 **项目类型**: {{projectType}}
1.3 **最终客户**: {{endCustomer}}

---

## 第二条 项目内容

2.1 **解决方案**: {{solutionDescription}}
2.2 **硬件设备**: {{hardwareList}}
2.3 **软件系统**: {{softwareList}}
2.4 **集成服务**: {{integrationServices}}

---

## 第三条 合同价款

3.1 **合同总价**: 人民币 {{amount}} 元
3.2 **价款构成**:
- 设备费: {{equipmentCost}} 元
- 软件费: {{softwareCost}} 元
- 集成服务费: {{integrationCost}} 元
- 维保费: {{maintenanceCost}} 元

---

## 第四条 项目周期

4.1 **项目启动**: {{startDate}}
4.2 **设备交付**: {{deliveryDate}}
4.3 **系统上线**: {{goliveDate}}
4.4 **项目验收**: {{acceptanceDate}}

---

## 第五条 验收标准

5.1 按照《项目需求规格说明书》进行验收
5.2 系统可用性达到 {{availabilityTarget}}%
5.3 关键功能测试通过率达到 {{testPassRate}}%

---

## 第六条 质保服务

6.1 **质保期限**: {{warrantyPeriod}}
6.2 **服务响应**: {{responseLevel}}
6.3 **备件供应**: {{sparePartsPeriod}}

---

## 第七条 付款方式

7.1 合同签订: {{phase1Rate}}%
7.2 设备到货: {{phase2Rate}}%
7.3 系统上线: {{phase3Rate}}%
7.4 终验合格: {{phase4Rate}}%

---

**甲方（盖章）**:                    **乙方（盖章）**:

日期:                              日期:
',
'{"editable": ["partyB", "projectName", "projectType", "endCustomer", "solutionDescription", "hardwareList", "softwareList", "integrationServices", "amount", "equipmentCost", "softwareCost", "integrationCost", "maintenanceCost", "startDate", "deliveryDate", "goliveDate", "acceptanceDate", "availabilityTarget", "testPassRate", "warrantyPeriod", "responseLevel", "sparePartsPeriod", "phase1Rate", "phase2Rate", "phase3Rate", "phase4Rate"]}',
'{"locked": []}',
1, 1, 'v1.0');

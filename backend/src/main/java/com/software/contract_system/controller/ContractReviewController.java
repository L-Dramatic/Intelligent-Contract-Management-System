package com.software.contract_system.controller;

import com.software.contract_system.common.Result;
import com.software.contract_system.dto.ContractReviewDTO;
import com.software.contract_system.service.ContractReviewService;
import com.software.contract_system.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同审查控制器
 * 
 * 提供AI审查报告的查询和手动触发接口
 */
@Slf4j
@RestController
@RequestMapping("/api/contract-review")
@RequiredArgsConstructor
@Tag(name = "合同审查", description = "AI合同审查相关接口")
public class ContractReviewController {

    private final ContractReviewService reviewService;
    private final SecurityUtils securityUtils;

    /**
     * 获取合同的最新审查报告
     */
    @GetMapping("/latest/{contractId}")
    @Operation(summary = "获取最新审查报告", description = "获取指定合同的最新AI审查报告")
    public Result<ContractReviewDTO> getLatestReview(
            @Parameter(description = "合同ID") @PathVariable Long contractId) {
        log.info("获取最新审查报告: contractId={}", contractId);
        
        ContractReviewDTO review = reviewService.getLatestReview(contractId);
        if (review == null) {
            return Result.success(null, "该合同暂无审查报告");
        }
        return Result.success(review);
    }

    /**
     * 获取合同的审查历史
     */
    @GetMapping("/history/{contractId}")
    @Operation(summary = "获取审查历史", description = "获取指定合同的所有审查报告历史")
    public Result<List<ContractReviewDTO>> getReviewHistory(
            @Parameter(description = "合同ID") @PathVariable Long contractId) {
        log.info("获取审查历史: contractId={}", contractId);
        
        List<ContractReviewDTO> history = reviewService.getReviewHistory(contractId);
        return Result.success(history);
    }

    /**
     * 根据ID获取审查报告详情
     */
    @GetMapping("/{reviewId}")
    @Operation(summary = "获取审查报告详情", description = "根据审查报告ID获取详细信息")
    public Result<ContractReviewDTO> getReviewById(
            @Parameter(description = "审查报告ID") @PathVariable Long reviewId) {
        log.info("获取审查报告详情: reviewId={}", reviewId);
        
        ContractReviewDTO review = reviewService.getReviewById(reviewId);
        if (review == null) {
            return Result.error("审查报告不存在");
        }
        return Result.success(review);
    }

    /**
     * 手动触发AI审查
     */
    @PostMapping("/trigger/{contractId}")
    @Operation(summary = "手动触发审查", description = "手动触发对指定合同的AI审查")
    public Result<String> triggerReview(
            @Parameter(description = "合同ID") @PathVariable Long contractId) {
        log.info("手动触发审查: contractId={}", contractId);
        
        try {
            Long userId = securityUtils.getCurrentUserId();
            reviewService.triggerReviewAsync(contractId, userId, "MANUAL");
            return Result.success("AI审查已触发，请稍后查看结果");
        } catch (Exception e) {
            log.error("触发审查失败: contractId={}", contractId, e);
            return Result.error("触发审查失败: " + e.getMessage());
        }
    }

    /**
     * 检查审查状态
     */
    @GetMapping("/status/{contractId}")
    @Operation(summary = "检查审查状态", description = "检查指定合同的最新审查状态")
    public Result<ReviewStatusDTO> checkReviewStatus(
            @Parameter(description = "合同ID") @PathVariable Long contractId) {
        log.info("检查审查状态: contractId={}", contractId);
        
        ContractReviewDTO review = reviewService.getLatestReview(contractId);
        
        ReviewStatusDTO status = new ReviewStatusDTO();
        if (review == null) {
            status.setHasReview(false);
            status.setStatus("NONE");
            status.setMessage("暂无审查记录");
        } else {
            status.setHasReview(true);
            status.setReviewId(review.getId());
            status.setStatus(review.getStatus());
            status.setRiskLevel(review.getRiskLevel());
            status.setScore(review.getScore());
            
            switch (review.getStatus()) {
                case "PENDING":
                    status.setMessage("审查进行中，请稍候...");
                    break;
                case "COMPLETED":
                    status.setMessage("审查已完成");
                    break;
                case "FAILED":
                    status.setMessage("审查失败: " + review.getErrorMessage());
                    break;
                default:
                    status.setMessage("未知状态");
            }
        }
        
        return Result.success(status);
    }

    /**
     * 审查状态DTO
     */
    @lombok.Data
    public static class ReviewStatusDTO {
        private Boolean hasReview;
        private Long reviewId;
        private String status;
        private String riskLevel;
        private Integer score;
        private String message;
    }
}


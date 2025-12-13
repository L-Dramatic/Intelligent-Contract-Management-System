package com.software.contract_system.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String realName;
    // 角色我们暂时让前端传，或者默认给 USER，这里先允许传
    private String role; 
}
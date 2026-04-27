package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.serviceimpl.AdminServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    private AdminServiceImpl adminService;
    @Autowired
    public AdminController(AdminServiceImpl adminService){
        this.adminService = adminService;
    }

    @GetMapping("/admins")
    public ApiResult findAll(HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以查看管理员列表");
        }
        System.out.println("查询全部");
        return ApiResultHandler.success(adminService.findAll());
    }

    @GetMapping("/admin/{adminId}")
    public ApiResult findById(@PathVariable("adminId") Integer adminId, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以查看管理员信息");
        }
        System.out.println("根据ID查找");
        return ApiResultHandler.success(adminService.findById(adminId));
    }

    @DeleteMapping("/admin/{adminId}")
    public ApiResult deleteById(@PathVariable("adminId") Integer adminId, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以删除管理员");
        }
        adminService.deleteById(adminId);
        return ApiResultHandler.success();
    }

    @PutMapping("/admin/{adminId}")
    public ApiResult update(@PathVariable("adminId") Integer adminId, Admin admin, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以修改管理员");
        }
        return ApiResultHandler.success(adminService.update(admin));
    }

    @PostMapping("/admin")
    public ApiResult add(Admin admin, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以新增管理员");
        }
        return ApiResultHandler.success(adminService.add(admin));
    }

    @PostMapping("/admin/resetPsw")
    public ApiResult resetPsw(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        Object currentRole = request.getAttribute("currentRole");
        Object currentUserId = request.getAttribute("currentUserId");
        boolean selfReset = currentUserId != null && ("0".equals(currentRole) || "1".equals(currentRole));
        if (!selfReset) {
            return forbidden("只能修改自己的密码");
        }
        String oldPsw = payload.get("oldPsw");
        String newPsw = payload.get("newPsw");
        if (oldPsw == null || oldPsw.trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "旧密码不能为空", null);
        }
        if (newPsw == null || newPsw.trim().length() < 6) {
            return ApiResultHandler.buildApiResult(400, "新密码长度不能少于6位", null);
        }
        Integer adminId = Integer.parseInt(String.valueOf(currentUserId));
        return ApiResultHandler.success(adminService.resetPsw(adminId, newPsw, oldPsw));
    }

    private boolean isAdmin(HttpServletRequest request) {
        return request != null && "0".equals(request.getAttribute("currentRole"));
    }

    private ApiResult forbidden(String message) {
        return ApiResultHandler.buildApiResult(403, message, null);
    }
}

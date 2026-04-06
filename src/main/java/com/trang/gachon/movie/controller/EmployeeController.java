package com.trang.gachon.movie.controller;

import com.trang.gachon.movie.dto.EmployeeRequest;
import com.trang.gachon.movie.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("employees", employeeService.getActiveEmployees());
        return "admin/employee/list";
    }

    @GetMapping("/add")
    public String showAdd(Model model) {
        model.addAttribute("employeeRequest", new EmployeeRequest());
        return "admin/employee/add";
    }

    @PostMapping("/add")
    public String handleAdd(
            @Valid @ModelAttribute("employeeRequest") EmployeeRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) return "admin/employee/add";

        try {
            employeeService.addEmployee(req);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Thêm nhân viên thành công!");
            return "redirect:/admin/employee/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/employee/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable Long id, Model model) {
        var employee = employeeService.getById(id);
        var account = employee.getAccount();

        // Đổ sẵn dữ liệu hiện tại lên form để admin sửa trực tiếp.
        model.addAttribute("employee", employee);
        model.addAttribute("employeeRequest", new EmployeeRequest(
                account.getUserName(),
                "",
                account.getFullName(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getIdentityCard(),
                account.getGender(),
                account.getDateOfBirth(),
                account.getAddress()
        ));
        return "admin/employee/edit";
    }

    @PostMapping("/edit/{id}")
    public String handleEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("employeeRequest") EmployeeRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("employee", employeeService.getById(id));
            return "admin/employee/edit";
        }

        try {
            employeeService.updateEmployee(id, req);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Cập nhật nhân viên thành công!");
            return "redirect:/admin/employee/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("employee", employeeService.getById(id));
            return "admin/employee/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String handleDelete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Đã khóa tài khoản nhân viên.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/employee/list";
    }
}
 

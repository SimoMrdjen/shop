package easy.shop.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Password must be at least 8 characters and include upper, lower, number and special character"
    )
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;

//    public String getCurrentPassword() { return currentPassword; }
//    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
//
//    public String getNewPassword() { return newPassword; }
//    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
//
//    public String getConfirmNewPassword() { return confirmNewPassword; }
//    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
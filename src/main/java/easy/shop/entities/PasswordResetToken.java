// File: src/main/java/easy/shop/entities/PasswordResetToken.java
package easy.shop.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String token;

    @Column(nullable = false)
    private String userName; // UserAccount.userName (email)

    @Column(nullable = false)
    private LocalDateTime expiresAt;

//    public PasswordResetToken() {}
//
//    public PasswordResetToken(String token, String userName, LocalDateTime expiresAt) {
//        this.token = token;
//        this.userName = userName;
//        this.expiresAt = expiresAt;
//    }

    // getters / setters
//    public String getToken() { return token; }
//    public void setToken(String token) { this.token = token; }
//    public String getUserName() { return userName; }
//    public void setUserName(String userName) { this.userName = userName; }
//    public LocalDateTime getExpiresAt() { return expiresAt; }
//    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
package org.userservice.user_service.dto.response.wallet;

public class WalletResponseDTO {
    private Long walletId;
    private Long userId;
    private Double currentBalance;

    public WalletResponseDTO() {}

    public WalletResponseDTO(Long walletId, Long userId, Double currentBalance) {
        this.walletId = walletId;
        this.userId = userId;
        this.currentBalance = currentBalance;
    }

    // Getters & Setters
    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(Double currentBalance) { this.currentBalance = currentBalance; }
}

package com.uber.server.repository;

import com.uber.server.storage.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Repository for voucher database operations.
 */
public class VoucherRepository {
    private static final Logger logger = LoggerFactory.getLogger(VoucherRepository.class);
    private final DatabasePool databasePool;
    
    public VoucherRepository(DatabasePool databasePool) {
        this.databasePool = databasePool;
    }
    
    /**
     * Checks if a voucher code exists.
     * @param code Voucher code
     * @return True if voucher exists
     */
    public boolean isValidCode(String code) {
        String sql = "SELECT null FROM credit_vouchers WHERE code = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check voucher code: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets the value of a voucher.
     * @param code Voucher code
     * @return Voucher value, or 0 if not found
     */
    public int getVoucherValue(String code) {
        String sql = "SELECT value FROM credit_vouchers WHERE code = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("value");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get voucher value: {}", e.getMessage(), e);
        }
        
        return 0;
    }
    
    /**
     * Deletes a voucher after redemption.
     * @param code Voucher code
     * @return True if voucher was deleted successfully
     */
    public boolean deleteVoucher(String code) {
        String sql = "DELETE FROM credit_vouchers WHERE code = ? LIMIT 1";
        
        try (Connection conn = databasePool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete voucher: {}", e.getMessage(), e);
            return false;
        }
    }
}

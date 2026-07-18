package com.icerikfabrikasi.platform.channel.affiliate;

/**
 * Affiliate komisyon türü.
 */
public enum CommissionType {
    /** Tek seferlik komisyon */
    ONE_TIME,
    /** Yinelenen (recurring) komisyon */
    RECURRING,
    /** Aksiyon başına ödeme (Cost Per Action) */
    CPA,
    /** Gelir paylaşımı (Revenue Share) */
    REVENUE_SHARE
}

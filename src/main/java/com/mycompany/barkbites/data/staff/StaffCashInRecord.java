package com.mycompany.barkbites.data.staff;

/**
 * One Firestore cash-in record visible to staff.
 */
public record StaffCashInRecord(
	String id,
	String customerName,
	String studentId,
	long amountCents,
	long balanceBeforeCents,
	long balanceAfterCents,
	long createdAtMillis
) {
}
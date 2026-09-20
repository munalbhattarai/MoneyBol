package com.moneybol.app.core.util

import com.moneybol.app.core.model.PaymentDirection
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for DirectionDetector.
 */
class DirectionDetectorTest {

    // ── Credit detection ──

    @Test
    fun `detect credited as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Your account has been credited by Rs. 500")
        )
    }

    @Test
    fun `detect received as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Payment received Rs. 500")
        )
    }

    @Test
    fun `detect deposit as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Cash deposit of Rs. 1000")
        )
    }

    @Test
    fun `detect amount received as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Amount received Rs. 500 in your account")
        )
    }

    @Test
    fun `detect incoming transfer as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Incoming transfer of NPR 2,500 received from John Doe")
        )
    }

    @Test
    fun `detect Nepali jamma and praapt as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "तपाईंको खातामा रु. ५०० जम्मा भएको छ।")
        )
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "रकम रु. १,५०० प्राप्त भयो।")
        )
    }

    @Test
    fun `detect FP QR and merchant QR transaction as CREDIT`() {
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Dear Merchant, FP QR transaction from 984#077 of NPR 100.00 is successful. RRN: 44802649c2RB")
        )
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Rs. 10.00 received from 982****191 for RRN: 14240939JDsX")
        )
        assertEquals(
            PaymentDirection.CREDIT,
            DirectionDetector.detect(text = "Your account has been Credited by NPR 2,510.00")
        )
    }

    // ── Debit detection ──

    @Test
    fun `detect debited as DEBIT`() {
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "Your account has been debited by Rs. 500")
        )
    }

    @Test
    fun `detect sent as DEBIT`() {
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "Rs. 500 sent to John")
        )
    }

    @Test
    fun `detect withdrawn as DEBIT`() {
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "Rs. 500 withdrawn from ATM")
        )
    }

    @Test
    fun `detect paid to and deducted as DEBIT`() {
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "Paid to Merchant ABC Rs. 1,200")
        )
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "Amount of Rs. 300 deducted from your account")
        )
    }

    @Test
    fun `detect Nepali katauti and bhuktani as DEBIT`() {
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "खाताबाट रु. ५०० कटौती गरियो।")
        )
        assertEquals(
            PaymentDirection.DEBIT,
            DirectionDetector.detect(text = "भुक्तानी सफल भयो रु. १,०००")
        )
    }

    // ── OTP detection (safety critical) ──

    @Test
    fun `detect OTP notification`() {
        assertEquals(
            PaymentDirection.OTP,
            DirectionDetector.detect(text = "Your OTP is 123456. Do not share with anyone.")
        )
    }

    @Test
    fun `detect verification code as OTP`() {
        assertEquals(
            PaymentDirection.OTP,
            DirectionDetector.detect(text = "Your verification code is 5678")
        )
    }

    @Test
    fun `detect MPIN as OTP`() {
        assertEquals(
            PaymentDirection.OTP,
            DirectionDetector.detect(text = "Please enter your MPIN to continue")
        )
    }

    @Test
    fun `detect 2FA code as OTP`() {
        assertEquals(
            PaymentDirection.OTP,
            DirectionDetector.detect(text = "Your 2FA security code for transaction is 998877")
        )
    }

    // ── Failed and Reversal detection ──

    @Test
    fun `detect failed transaction`() {
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Transaction failed. Insufficient balance.")
        )
    }

    @Test
    fun `detect declined transaction`() {
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Your payment of Rs. 500 was declined")
        )
    }

    @Test
    fun `detect reversed transaction as FAILED`() {
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Transaction of Rs. 1,500 has been reversed.")
        )
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Reversal of Rs. 500 credited back due to network error.")
        )
    }

    @Test
    fun `detect cancelled and refunded as FAILED`() {
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Your order of Rs. 800 was cancelled.")
        )
        assertEquals(
            PaymentDirection.FAILED,
            DirectionDetector.detect(text = "Payment of Rs. 400 has been refunded.")
        )
    }

    // ── Pending detection ──

    @Test
    fun `detect pending transaction`() {
        assertEquals(
            PaymentDirection.PENDING,
            DirectionDetector.detect(text = "Your transaction of Rs. 500 is pending")
        )
    }

    @Test
    fun `detect processing and under process as PENDING`() {
        assertEquals(
            PaymentDirection.PENDING,
            DirectionDetector.detect(text = "Deposit of Rs. 2,000 is currently processing")
        )
        assertEquals(
            PaymentDirection.PENDING,
            DirectionDetector.detect(text = "Your payment request is under process")
        )
    }

    // ── Ambiguous and Unknown ──

    @Test
    fun `do not classify payment based on standalone word transaction or NPR`() {
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "Transaction of NPR 500")
        )
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "NPR 1,500")
        )
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "Notification regarding transaction 987654")
        )
    }

    @Test
    fun `return UNKNOWN for ambiguous text`() {
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "Rs. 500")
        )
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "")
        )
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = null)
        )
    }

    @Test
    fun `return UNKNOWN when credit and debit are equally present`() {
        // e.g. "Amount credited was later debited" -> 1 credit, 1 debit -> UNKNOWN
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "Amount credited was later debited from your account")
        )
    }

    // ── OTP takes priority over credit ──

    @Test
    fun `OTP in credit-like context returns OTP`() {
        assertEquals(
            PaymentDirection.OTP,
            DirectionDetector.detect(text = "OTP for your credited amount of Rs. 500 is 1234")
        )
    }
}

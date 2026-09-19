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

    // ── Failed detection ──

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

    // ── Pending detection ──

    @Test
    fun `detect pending transaction`() {
        assertEquals(
            PaymentDirection.PENDING,
            DirectionDetector.detect(text = "Your transaction of Rs. 500 is pending")
        )
    }

    // ── Unknown (ambiguous) ──

    @Test
    fun `return UNKNOWN for ambiguous text`() {
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "Rs. 500")
        )
    }

    @Test
    fun `return UNKNOWN for empty text`() {
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = "")
        )
    }

    @Test
    fun `return UNKNOWN for null text`() {
        assertEquals(
            PaymentDirection.UNKNOWN,
            DirectionDetector.detect(text = null)
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

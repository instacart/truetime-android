package com.instacart.truetime

import java.io.IOException
import java.util.Locale

internal class InvalidNtpServerResponseException : IOException {

  val property: String
  val expectedValue: Float
  val actualValue: Float

  internal constructor(detailMessage: String) : super(detailMessage) {

    this.property = "na"
    this.expectedValue = 0f
    this.actualValue = 0f
  }

  /**
   * @param message An informative message that api users can use to know what went wrong. should
   *   contain [this.property] [this.expectedValue] and [this.actualValue] as format specifiers (in
   *   that order)
   * @param property property that caused the invalid NTP response
   */
  internal constructor(
      message: String,
      property: String,
      actualValue: Float,
      expectedValue: Float
  ) : super(String.format(Locale.getDefault(), message, property, actualValue, expectedValue)) {

    this.property = property
    this.actualValue = actualValue
    this.expectedValue = expectedValue
  }
}

package com.windrr.boat.core.util

/** 천단위 콤마가 포함된 금액 문자열로 변환 (예: 10000 -> "10,000") */
fun Number.toPriceString(): String = "%,d".format(this.toLong())

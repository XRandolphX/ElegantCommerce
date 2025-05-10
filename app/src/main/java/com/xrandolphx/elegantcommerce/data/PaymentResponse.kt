package com.xrandolphx.elegantcommerce.data

import com.google.gson.annotations.SerializedName

data class PaymentResponse(
    @SerializedName("init_point") val initPoint: String?
)
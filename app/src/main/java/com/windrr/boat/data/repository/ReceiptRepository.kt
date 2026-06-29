package com.windrr.boat.data.repository

import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.model.ReceiptListData

class ReceiptRepository {

    private val api = ApiClient.receiptApiService

    suspend fun getReceipts(
        status: String = "all",
        sort: String = "recent",
        limit: Int = 20,
        cursor: String? = null,
        category: String? = null,
        q: String? = null,
    ): Result<ReceiptListData> = runCatching {
        api.getReceipts(
            status   = status,
            sort     = sort,
            limit    = limit,
            cursor   = cursor,
            category = category,
            q        = q,
        ).data
    }
}

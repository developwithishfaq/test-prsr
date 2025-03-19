package com.adm.url_parser.sdk.usecases

import com.adm.url_parser.interfaces.ApiLinkScrapper
import com.adm.url_parser.models.ParsedVideo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume


class ScrapperParallelUseCase : ScrappersUser {
    override suspend fun invoke(list: List<ApiLinkScrapper>, url: String): Result<ParsedVideo?> {
        return suspendCancellableCoroutine { cor ->
            val atomicInt = AtomicInteger(0)
            CoroutineScope(Dispatchers.IO).launch {
                list.forEach { scrapper ->
                    launch {
                        val response = scrapper.scrapeLink(url)
                        val count = atomicInt.incrementAndGet()
                        if (response.isSuccess) {
                            if (cor.isActive) {
                                cor.resume(response)
                                cor.cancel()
                            }
                        } else if (count == list.size) {
                            if (cor.isActive) {
                                cor.resume(Result.failure(response.exceptionOrNull() ?: Exception("No Data Found in ScrapperParallelUseCase")))
                                cor.cancel()
                            }
                        }
                    }
                }
            }
        }
    }
}
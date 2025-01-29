package com.down.adm_parser.interview.persistence

import kotlinx.coroutines.delay

class GetLocalStudentsImpl : GetLocalStudents {
    override suspend fun getStudents(): List<String> {
        delay(2000)
        return listOf(
            "Ishafq",
            "Ishafq1",
            "Ishafq2",
            "Ishafq3",
        )
    }
}
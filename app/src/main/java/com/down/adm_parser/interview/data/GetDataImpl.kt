package com.down.adm_parser.interview.data

import com.down.adm_parser.interview.domain.GetData
import com.down.adm_parser.interview.persistence.GetLocalStudents

class GetDataImpl(
    private val getLocalStudents: GetLocalStudents
) : GetData {
    override suspend fun getStudents(): List<String> {
        return getLocalStudents.getStudents()
    }
}
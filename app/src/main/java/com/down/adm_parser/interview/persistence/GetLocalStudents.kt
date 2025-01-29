package com.down.adm_parser.interview.persistence

interface GetLocalStudents {
    suspend fun getStudents(): List<String>
}
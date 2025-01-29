package com.down.adm_parser.interview.domain

interface GetData {
    suspend fun getStudents(): List<String>
}
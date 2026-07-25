package com.kodeelite.nooreislam

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
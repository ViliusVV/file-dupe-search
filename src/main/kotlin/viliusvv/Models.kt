package viliusvv

import calcFileHash

data class FileEntry(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val modified: Long,
    val hash: String = calcFileHash(filePath)
) {
    val hashShort get() = hash.take(8)
}
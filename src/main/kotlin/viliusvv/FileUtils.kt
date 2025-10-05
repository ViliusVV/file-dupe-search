import viliusvv.FileEntry
import java.io.File
import java.security.MessageDigest

fun calcFileHash(filePath: String): String {
    val digestInstance = MessageDigest.getInstance("MD5")
    val file = File(filePath)
    val bytes = file.readBytes()
    val hashBytes = digestInstance.digest(bytes)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun listSubdirAndFiles(rootDir: String): List<FileEntry> {
    val root = File(rootDir)
    if (!root.exists() || !root.isDirectory) {
        println("Directory does not exist: $rootDir")
        return emptyList()
    }

    val entries = mutableListOf<FileEntry>()
    root.walkTopDown().forEach { file ->
        if (file.isDirectory) {
            println("Dir: ${file.absolutePath}")
        } else {
//            println("  File: ${file.name} (${file.length()} bytes)")
            entries += FileEntry(
                file.name,
                file.absolutePath,
                file.length(),
                file.lastModified(),
            )
        }
    }

    return entries
}
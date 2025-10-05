import viliusvv.fileQueue
import viliusvv.infoModel
import java.io.File
import java.security.MessageDigest

fun calcFileHash(filePath: String): String {
    val digestInstance = MessageDigest.getInstance("MD5")
    val file = File(filePath)
    val bytes = file.readBytes()
    val hashBytes = digestInstance.digest(bytes)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun listSubdirAndFiles(dirs: List<File>) {
    infoModel.statusMessage = "Begins scanning directories..."
    for (root in dirs) {
        if (!root.exists() || !root.isDirectory) {
            println("Directory does not exist: $root")
            return
        }

        root.walkTopDown().forEach { file ->
            if (file.isDirectory) {
                println("Dir: ${file.absolutePath}")
                infoModel.statusMessage = "Scanning: ${file.absolutePath}"
            } else {
//            println("  File: ${file.name} (${file.length()} bytes)")
                fileQueue.add(file)
            }
        }

        infoModel.statusMessage = "Finished scanning root: $root"
    }

    infoModel.statusMessage = "Finished collecting files"
}
package igor.max.rafa_poc

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.time.LocalDateTime


class AndroidDependentPdfStrategy(
    private val resolver: ContentResolver,
) : MakerStrategy {

    override fun make(
        allPhotos: MutableList<Uri>,
        folder: String,
        result: (String) -> Unit,
    ) {

        CoroutineScope(Dispatchers.IO).launch {

            val filePath = "$folder/rafa-${LocalDateTime.now()}.pdf"
            if (allPhotos.isEmpty()) throw IllegalStateException("Missing photos uri's")

            val document = PdfDocument()
            var index = 0

            allPhotos.forEach { uri ->
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        resolver,
                        uri
                    )
                ).copy(Bitmap.Config.ARGB_8888, false)

                with(PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index).create()) {
                    document.startPage(this).apply {
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        document.finishPage(this)
                        index++
                    }
                }
            }


            withContext(Dispatchers.IO) {
                document.writeTo(FileOutputStream(filePath))
            }
            document.close()
            result.invoke(filePath)

        }


    }
}

// migrate  allPhotos from URI to something kotlin/java
interface Maker {
    fun createPdf(allPhotos: MutableList<Uri>, urlResultCallback: (String) -> Unit)
}

interface MakerStrategy {
    fun make(allPhotos: MutableList<Uri>, folder: String, result: (String) -> Unit)
}

class PdfMaker(
    private val fileFolder: String,
    private val strategy: MakerStrategy,
) : Maker {
    override fun createPdf(allPhotos: MutableList<Uri>, urlResultCallback: (String) -> Unit) {
            strategy.make(allPhotos, fileFolder) {
                urlResultCallback.invoke(it)
            }

    }
}

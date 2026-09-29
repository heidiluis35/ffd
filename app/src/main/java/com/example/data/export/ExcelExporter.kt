package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Genera un libro de trabajo oficial Microsoft Excel (.xlsx) OpenXML
 * con columnas: Nombre completo, Número de control, Carrera, Fecha, Hora de entrada.
 */
object ExcelExporter {

    suspend fun exportRegistrosToExcel(
        context: Context,
        registros: List<Registro>,
        titleSuffix: String = "Historico"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateStr = fileDateFormat.format(Date())
            val fileName = "Bibliotag_ITI_Asistencia_${titleSuffix}_$dateStr.xlsx"

            val xlsxBytes = buildXlsxBytes(registros)

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Bibliotag_ITI")
                }
                val resolver = context.contentResolver
                val contentUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IllegalStateException("No se pudo crear el archivo en Descargas")

                resolver.openOutputStream(contentUri)?.use { os ->
                    os.write(xlsxBytes)
                    os.flush()
                } ?: throw IllegalStateException("No se pudo abrir el stream de salida")
                contentUri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "Bibliotag_ITI").apply { if (!exists()) mkdirs() }
                val targetFile = File(appDir, fileName)
                FileOutputStream(targetFile).use { os ->
                    os.write(xlsxBytes)
                    os.flush()
                }
                Uri.fromFile(targetFile)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildXlsxBytes(registros: List<Registro>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zip ->

            // 1. [Content_Types].xml
            addZipEntry(
                zip,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
            )

            // 2. _rels/.rels
            addZipEntry(
                zip,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            )

            // 3. xl/_rels/workbook.xml.rels
            addZipEntry(
                zip,
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            )

            // 4. xl/styles.xml (Colors: Navy Blue #0D3B66 for headers, alternating light rows)
            addZipEntry(
                zip,
                "xl/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts count="3">
        <font><name val="Calibri"/><sz val="11"/><color rgb="000000"/></font>
        <font><b/><name val="Calibri"/><sz val="11"/><color rgb="FFFFFF"/></font>
        <font><b/><name val="Calibri"/><sz val="14"/><color rgb="0D3B66"/></font>
    </fonts>
    <fills count="3">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="0D3B66"/></patternFill></fill>
    </fills>
    <borders count="2">
        <border><left/><right/><top/><bottom/></border>
        <border>
            <left style="thin"><color rgb="CCCCCC"/></left>
            <right style="thin"><color rgb="CCCCCC"/></right>
            <top style="thin"><color rgb="CCCCCC"/></top>
            <bottom style="thin"><color rgb="CCCCCC"/></bottom>
        </border>
    </borders>
    <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
    <cellXfs count="4">
        <!-- 0: Regular -->
        <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1"/>
        <!-- 1: Header (Navy Blue fill, White bold text) -->
        <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
            <alignment horizontal="center" vertical="center"/>
        </xf>
        <!-- 2: Title Large -->
        <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
        <!-- 3: Centered Regular -->
        <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
            <alignment horizontal="center" vertical="center"/>
        </xf>
    </cellXfs>
</styleSheet>"""
            )

            // 5. xl/workbook.xml
            addZipEntry(
                zip,
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Entradas Biblioteca ITI" sheetId="1" r:id="rId1"/>
    </sheets>
</workbook>"""
            )

            // 6. xl/worksheets/sheet1.xml
            val sheetXml = buildSheetXml(registros)
            addZipEntry(zip, "xl/worksheets/sheet1.xml", sheetXml)
        }
        return bos.toByteArray()
    }

    private fun addZipEntry(zip: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zip.putNextEntry(entry)
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun buildSheetXml(registros: List<Registro>): String {
        val sb = StringBuilder()
        val onlyDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val onlyTimeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")

        // Column widths
        sb.append("""<cols>""")
        sb.append("""<col min="1" max="1" width="6" customWidth="1"/>""") // #
        sb.append("""<col min="2" max="2" width="32" customWidth="1"/>""") // Nombre completo
        sb.append("""<col min="3" max="3" width="16" customWidth="1"/>""") // Número de control
        sb.append("""<col min="4" max="4" width="34" customWidth="1"/>""") // Carrera
        sb.append("""<col min="5" max="5" width="16" customWidth="1"/>""") // Fecha
        sb.append("""<col min="6" max="6" width="18" customWidth="1"/>""") // Hora de entrada
        sb.append("""<col min="7" max="7" width="18" customWidth="1"/>""") // UID Tag NFC
        sb.append("""<col min="8" max="8" width="14" customWidth="1"/>""") // Distancia GPS
        sb.append("""</cols>""")

        sb.append("""<sheetData>""")

        // Title Rows
        sb.append("""<row r="1">""")
        sb.append("""<c r="A1" s="2" t="inlineStr"><is><t>INSTITUTO TECNOLÓGICO DEL ISTMO - BIBLIOTAG ITI</t></is></c>""")
        sb.append("""</row>""")

        sb.append("""<row r="2">""")
        sb.append("""<c r="A2" s="0" t="inlineStr"><is><t>Registro Oficial de Entradas a la Biblioteca Escolar</t></is></c>""")
        sb.append("""</row>""")

        sb.append("""<row r="3"/>""") // Empty row

        // Header Row (Row 4, style 1)
        sb.append("""<row r="4">""")
        sb.append("""<c r="A4" s="1" t="inlineStr"><is><t>#</t></is></c>""")
        sb.append("""<c r="B4" s="1" t="inlineStr"><is><t>Nombre Completo</t></is></c>""")
        sb.append("""<c r="C4" s="1" t="inlineStr"><is><t>Número de Control</t></is></c>""")
        sb.append("""<c r="D4" s="1" t="inlineStr"><is><t>Carrera</t></is></c>""")
        sb.append("""<c r="E4" s="1" t="inlineStr"><is><t>Fecha</t></is></c>""")
        sb.append("""<c r="F4" s="1" t="inlineStr"><is><t>Hora de Entrada</t></is></c>""")
        sb.append("""<c r="G4" s="1" t="inlineStr"><is><t>UID NFC</t></is></c>""")
        sb.append("""<c r="H4" s="1" t="inlineStr"><is><t>Distancia</t></is></c>""")
        sb.append("""</row>""")

        // Data Rows
        var rowIndex = 5
        for ((idx, reg) in registros.withIndex()) {
            val num = idx + 1
            val dateStr = onlyDateFormat.format(Date(reg.timestamp))
            val timeStr = onlyTimeFormat.format(Date(reg.timestamp))
            val distStr = if (reg.distanciaMetros > 0) "${reg.distanciaMetros.toInt()} m" else "Verificado"

            sb.append("""<row r="$rowIndex">""")
            sb.append("""<c r="A$rowIndex" s="3"><v>$num</v></c>""")
            sb.append("""<c r="B$rowIndex" s="0" t="inlineStr"><is><t>${escapeXml(reg.nombre)}</t></is></c>""")
            sb.append("""<c r="C$rowIndex" s="3" t="inlineStr"><is><t>${escapeXml(reg.numeroControl)}</t></is></c>""")
            sb.append("""<c r="D$rowIndex" s="0" t="inlineStr"><is><t>${escapeXml(reg.carrera)}</t></is></c>""")
            sb.append("""<c r="E$rowIndex" s="3" t="inlineStr"><is><t>$dateStr</t></is></c>""")
            sb.append("""<c r="F$rowIndex" s="3" t="inlineStr"><is><t>$timeStr</t></is></c>""")
            sb.append("""<c r="G$rowIndex" s="3" t="inlineStr"><is><t>${escapeXml(reg.alumnoUid)}</t></is></c>""")
            sb.append("""<c r="H$rowIndex" s="3" t="inlineStr"><is><t>$distStr</t></is></c>""")
            sb.append("""</row>""")
            rowIndex++
        }

        sb.append("""</sheetData>""")
        sb.append("""</worksheet>""")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

  private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)

  // A4 standard points: 595 x 842
  private const val PAGE_WIDTH = 595
  private const val PAGE_HEIGHT = 842

  private fun drawHeader(
    canvas: Canvas,
    title: String,
    subtitle: String,
    clientName: String,
    dateStr: String
  ) {
    val bgPaint = Paint().apply {
      color = Color.rgb(8, 12, 20) // Rush Dark Navy
      style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 95f, bgPaint)

    // Top purple accent stripe
    val accentPaint = Paint().apply {
      color = Color.rgb(139, 92, 246) // Rush Purple Accent
      style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 5f, accentPaint)

    // Title
    val titlePaint = Paint().apply {
      color = Color.WHITE
      textSize = 18f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText("THE RUSH WAY", 36f, 34f, titlePaint)

    val brandSubtitlePaint = Paint().apply {
      color = Color.rgb(167, 139, 250) // Purple Light
      textSize = 8.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      letterSpacing = 0.15f
      isAntiAlias = true
    }
    canvas.drawText("PROFESSIONAL COACHING & ATHLETIC NUTRITION", 36f, 48f, brandSubtitlePaint)

    val docTitlePaint = Paint().apply {
      color = Color.rgb(241, 245, 249)
      textSize = 14f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText(title, 36f, 74f, docTitlePaint)

    // Subtitle & Client metadata on right
    val metaLabelPaint = Paint().apply {
      color = Color.rgb(148, 163, 184)
      textSize = 9f
      textAlign = Paint.Align.RIGHT
      isAntiAlias = true
    }
    val metaValPaint = Paint().apply {
      color = Color.WHITE
      textSize = 9.5f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.RIGHT
      isAntiAlias = true
    }
    canvas.drawText("CLIENT:", (PAGE_WIDTH - 36).toFloat(), 34f, metaLabelPaint)
    canvas.drawText(clientName, (PAGE_WIDTH - 36).toFloat(), 48f, metaValPaint)
    canvas.drawText("DATE: $dateStr", (PAGE_WIDTH - 36).toFloat(), 66f, metaLabelPaint)
    canvas.drawText(subtitle, (PAGE_WIDTH - 36).toFloat(), 80f, metaValPaint)
  }

  private fun drawWatermark(canvas: Canvas) {
    val watermarkPaint = Paint().apply {
      color = Color.argb(12, 139, 92, 246) // Extremely subtle purple watermark
      textSize = 58f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    canvas.save()
    canvas.rotate(-35f, (PAGE_WIDTH / 2).toFloat(), (PAGE_HEIGHT / 2).toFloat())
    canvas.drawText("THE RUSH WAY", (PAGE_WIDTH / 2).toFloat(), (PAGE_HEIGHT / 2).toFloat(), watermarkPaint)
    canvas.restore()
  }

  private fun drawFooter(canvas: Canvas, pageNum: Int) {
    val linePaint = Paint().apply {
      color = Color.rgb(226, 232, 240)
      strokeWidth = 1f
    }
    canvas.drawLine(36f, (PAGE_HEIGHT - 35).toFloat(), (PAGE_WIDTH - 36).toFloat(), (PAGE_HEIGHT - 35).toFloat(), linePaint)

    val footerPaint = Paint().apply {
      color = Color.rgb(100, 116, 139)
      textSize = 8.5f
      isAntiAlias = true
    }
    canvas.drawText("THE RUSH WAY Operating System • Confidential Fitness & Nutrition Program", 36f, (PAGE_HEIGHT - 20).toFloat(), footerPaint)

    val pagePaint = Paint().apply {
      color = Color.rgb(100, 116, 139)
      textSize = 8.5f
      textAlign = Paint.Align.RIGHT
      isAntiAlias = true
    }
    canvas.drawText("Page $pageNum", (PAGE_WIDTH - 36).toFloat(), (PAGE_HEIGHT - 20).toFloat(), pagePaint)
  }

  // 1. Export Training Program PDF
  fun exportTrainingProgramPdf(
    context: Context,
    clientName: String,
    program: TrainingProgramEntity,
    days: List<Pair<TrainingDayEntity, List<TrainingDayExerciseEntity>>>
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    drawWatermark(canvas)
    drawHeader(
      canvas = canvas,
      title = "TRAINING PROGRAM SPECIFICATION",
      subtitle = program.name,
      clientName = clientName,
      dateStr = dateFormat.format(Date(program.createdAt))
    )

    var currentY = 115f

    // Summary Box
    val summaryBoxPaint = Paint().apply {
      color = Color.rgb(248, 250, 252)
      style = Paint.Style.FILL
    }
    val summaryBorderPaint = Paint().apply {
      color = Color.rgb(203, 213, 225)
      style = Paint.Style.STROKE
      strokeWidth = 1f
    }
    canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 44f), 6f, 6f, summaryBoxPaint)
    canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 44f), 6f, 6f, summaryBorderPaint)

    val summaryTextPaint = Paint().apply {
      color = Color.rgb(30, 41, 59)
      textSize = 10f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      isAntiAlias = true
    }
    val boldSummaryTextPaint = Paint().apply {
      color = Color.rgb(15, 23, 42)
      textSize = 10f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText("Program Frequency: ", 48f, currentY + 18f, boldSummaryTextPaint)
    canvas.drawText("${program.daysPerWeek} Days / Week", 160f, currentY + 18f, summaryTextPaint)
    canvas.drawText("Total Training Days: ", 270f, currentY + 18f, boldSummaryTextPaint)
    canvas.drawText("${days.size} Prescribed Days", 380f, currentY + 18f, summaryTextPaint)

    val descStr = if (program.description.isNotBlank()) program.description else "Custom tailored progressive overload regimen."
    canvas.drawText("Focus Notes: $descStr", 48f, currentY + 34f, summaryTextPaint)

    currentY += 60f

    // Days & Exercises
    for ((day, exercises) in days) {
      if (currentY > PAGE_HEIGHT - 120) break

      // Day Header
      val dayHeaderPaint = Paint().apply {
        color = Color.rgb(15, 23, 42)
        style = Paint.Style.FILL
      }
      canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 22f), 4f, 4f, dayHeaderPaint)

      val dayTitlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText(day.name.uppercase(), 46f, currentY + 15f, dayTitlePaint)

      val muscleTextPaint = Paint().apply {
        color = Color.rgb(167, 139, 250)
        textSize = 9f
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
      }
      if (day.targetMuscles.isNotBlank()) {
        canvas.drawText("Target: ${day.targetMuscles}", (PAGE_WIDTH - 46).toFloat(), currentY + 15f, muscleTextPaint)
      }

      currentY += 26f

      // Table Header
      val thBgPaint = Paint().apply {
        color = Color.rgb(241, 245, 249)
        style = Paint.Style.FILL
      }
      canvas.drawRect(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 18f, thBgPaint)

      val thTextPaint = Paint().apply {
        color = Color.rgb(71, 85, 105)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText("EXERCISE", 44f, currentY + 12f, thTextPaint)
      canvas.drawText("SETS", 210f, currentY + 12f, thTextPaint)
      canvas.drawText("REPS", 250f, currentY + 12f, thTextPaint)
      canvas.drawText("REST", 305f, currentY + 12f, thTextPaint)
      canvas.drawText("RIR/RPE", 365f, currentY + 12f, thTextPaint)
      canvas.drawText("METHOD & NOTES", 430f, currentY + 12f, thTextPaint)

      currentY += 20f

      // Table Rows
      val rowTextPaint = Paint().apply {
        color = Color.rgb(15, 23, 42)
        textSize = 9f
        isAntiAlias = true
      }
      val rowBorderPaint = Paint().apply {
        color = Color.rgb(241, 245, 249)
        strokeWidth = 0.8f
      }

      for (ex in exercises) {
        if (currentY > PAGE_HEIGHT - 60) break
        canvas.drawText(ex.exerciseName, 44f, currentY + 10f, rowTextPaint)
        canvas.drawText("${ex.sets}", 210f, currentY + 10f, rowTextPaint)
        canvas.drawText(ex.reps, 250f, currentY + 10f, rowTextPaint)
        canvas.drawText(ex.restTime, 305f, currentY + 10f, rowTextPaint)
        canvas.drawText(ex.rirRpe ?: "RIR 1-2", 365f, currentY + 10f, rowTextPaint)

        val methodInfo = if (ex.trainingMethod != "Straight sets") "[${ex.trainingMethod}] " else ""
        val fullNotes = methodInfo + (ex.notes ?: "")
        val truncatedNotes = if (fullNotes.length > 22) fullNotes.take(20) + "…" else fullNotes
        canvas.drawText(truncatedNotes, 430f, currentY + 10f, rowTextPaint)

        canvas.drawLine(36f, currentY + 16f, (PAGE_WIDTH - 36).toFloat(), currentY + 16f, rowBorderPaint)
        currentY += 18f
      }
      currentY += 12f
    }

    drawFooter(canvas, 1)
    document.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, "RushWay_Training_${System.currentTimeMillis()}.pdf")
    FileOutputStream(outFile).use { document.writeTo(it) }
    document.close()
    return outFile
  }

  // 2. Export Nutrition Plan PDF
  fun exportNutritionPlanPdf(
    context: Context,
    clientName: String,
    plan: NutritionPlanEntity,
    meals: List<Pair<MealEntity, List<MealFoodEntity>>>
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    drawWatermark(canvas)
    drawHeader(
      canvas = canvas,
      title = "NUTRITION & MACRONUTRIENT SPECIFICATION",
      subtitle = plan.name,
      clientName = clientName,
      dateStr = dateFormat.format(Date(plan.createdAt))
    )

    var currentY = 115f

    // Daily Macro Target Cards
    val cardPaint = Paint().apply {
      color = Color.rgb(15, 23, 42)
      style = Paint.Style.FILL
    }
    canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 54f), 6f, 6f, cardPaint)

    val macroTitlePaint = Paint().apply {
      color = Color.rgb(148, 163, 184)
      textSize = 8.5f
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    val macroValPaint = Paint().apply {
      color = Color.WHITE
      textSize = 14f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }

    // 4 Columns: Calories, Protein, Carbs, Fat
    val colW = (PAGE_WIDTH - 72) / 4f
    canvas.drawText("DAILY CALORIES", 36f + colW * 0.5f, currentY + 20f, macroTitlePaint)
    canvas.drawText("${plan.targetCalories} kcal", 36f + colW * 0.5f, currentY + 40f, macroValPaint)

    canvas.drawText("PROTEIN", 36f + colW * 1.5f, currentY + 20f, macroTitlePaint)
    canvas.drawText("${plan.targetProteinG}g", 36f + colW * 1.5f, currentY + 40f, macroValPaint)

    canvas.drawText("CARBOHYDRATES", 36f + colW * 2.5f, currentY + 20f, macroTitlePaint)
    canvas.drawText("${plan.targetCarbsG}g", 36f + colW * 2.5f, currentY + 40f, macroValPaint)

    canvas.drawText("HEALTHY FATS", 36f + colW * 3.5f, currentY + 20f, macroTitlePaint)
    canvas.drawText("${plan.targetFatG}g", 36f + colW * 3.5f, currentY + 40f, macroValPaint)

    currentY += 70f

    // Meals Table
    for ((meal, foods) in meals) {
      if (currentY > PAGE_HEIGHT - 120) break

      val mealHeaderPaint = Paint().apply {
        color = Color.rgb(241, 245, 249)
        style = Paint.Style.FILL
      }
      canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 20f), 4f, 4f, mealHeaderPaint)

      val mealNamePaint = Paint().apply {
        color = Color.rgb(15, 23, 42)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
      }
      val timingPaint = Paint().apply {
        color = Color.rgb(100, 116, 139)
        textSize = 8.5f
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
      }

      canvas.drawText(meal.mealName.uppercase(), 46f, currentY + 14f, mealNamePaint)
      if (meal.timing?.isNotBlank() == true) {
        canvas.drawText(meal.timing, (PAGE_WIDTH - 46).toFloat(), currentY + 14f, timingPaint)
      }

      currentY += 24f

      val thTextPaint = Paint().apply {
        color = Color.rgb(100, 116, 139)
        textSize = 8f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText("FOOD ITEM", 46f, currentY + 10f, thTextPaint)
      canvas.drawText("PORTION", 230f, currentY + 10f, thTextPaint)
      canvas.drawText("CALORIES", 310f, currentY + 10f, thTextPaint)
      canvas.drawText("PROT", 380f, currentY + 10f, thTextPaint)
      canvas.drawText("CARB", 435f, currentY + 10f, thTextPaint)
      canvas.drawText("FAT", 490f, currentY + 10f, thTextPaint)

      currentY += 16f

      val rowPaint = Paint().apply {
        color = Color.rgb(30, 41, 59)
        textSize = 9f
        isAntiAlias = true
      }
      val borderPaint = Paint().apply {
        color = Color.rgb(241, 245, 249)
        strokeWidth = 0.8f
      }

      for (item in foods) {
        if (currentY > PAGE_HEIGHT - 60) break
        canvas.drawText(item.foodName, 46f, currentY + 10f, rowPaint)
        canvas.drawText(item.quantity, 230f, currentY + 10f, rowPaint)
        canvas.drawText("${item.calories.roundToInt()} kcal", 310f, currentY + 10f, rowPaint)
        canvas.drawText("${item.proteinG.roundToInt()}g", 380f, currentY + 10f, rowPaint)
        canvas.drawText("${item.carbsG.roundToInt()}g", 435f, currentY + 10f, rowPaint)
        canvas.drawText("${item.fatG.roundToInt()}g", 490f, currentY + 10f, rowPaint)

        canvas.drawLine(36f, currentY + 16f, (PAGE_WIDTH - 36).toFloat(), currentY + 16f, borderPaint)
        currentY += 18f
      }
      currentY += 10f
    }

    drawFooter(canvas, 1)
    document.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, "RushWay_Nutrition_${System.currentTimeMillis()}.pdf")
    FileOutputStream(outFile).use { document.writeTo(it) }
    document.close()
    return outFile
  }

  // 3. Export Monthly Progress Report PDF
  fun exportMonthlyReportPdf(
    context: Context,
    clientName: String,
    report: MonthlyReportEntity
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    drawWatermark(canvas)
    drawHeader(
      canvas = canvas,
      title = "MONTHLY ATHLETIC PROGRESS REPORT",
      subtitle = report.monthYear,
      clientName = clientName,
      dateStr = dateFormat.format(Date(report.generatedDateMillis))
    )

    var currentY = 115f

    // Weight & Adherence Overview Cards
    val cardPaint = Paint().apply {
      color = Color.rgb(15, 23, 42)
      style = Paint.Style.FILL
    }
    canvas.drawRoundRect(RectF(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY + 60f), 6f, 6f, cardPaint)

    val colW = (PAGE_WIDTH - 72) / 4f
    val titleP = Paint().apply {
      color = Color.rgb(148, 163, 184)
      textSize = 8.5f
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }
    val valP = Paint().apply {
      color = Color.WHITE
      textSize = 14f
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      isAntiAlias = true
    }

    canvas.drawText("STARTING WEIGHT", 36f + colW * 0.5f, currentY + 22f, titleP)
    canvas.drawText("${report.startingWeightKg} kg", 36f + colW * 0.5f, currentY + 44f, valP)

    canvas.drawText("CURRENT WEIGHT", 36f + colW * 1.5f, currentY + 22f, titleP)
    canvas.drawText("${report.currentWeightKg} kg", 36f + colW * 1.5f, currentY + 44f, valP)

    val changeStr = (if (report.weightChangeKg >= 0) "+" else "") + "%.1f kg".format(report.weightChangeKg)
    canvas.drawText("NET DELTA", 36f + colW * 2.5f, currentY + 22f, titleP)
    canvas.drawText(changeStr, 36f + colW * 2.5f, currentY + 44f, valP)

    canvas.drawText("ADHERENCE AVG", 36f + colW * 3.5f, currentY + 22f, titleP)
    canvas.drawText("T:${report.trainingAdherenceAvg}% / N:${report.nutritionAdherenceAvg}%", 36f + colW * 3.5f, currentY + 44f, valP)

    currentY += 80f

    // Sections
    fun drawReportSection(title: String, content: String) {
      if (currentY > PAGE_HEIGHT - 80) return
      val sectionTitlePaint = Paint().apply {
        color = Color.rgb(139, 92, 246)
        textSize = 11f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText(title.uppercase(), 36f, currentY, sectionTitlePaint)
      currentY += 6f

      val lineP = Paint().apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 0.8f
      }
      canvas.drawLine(36f, currentY, (PAGE_WIDTH - 36).toFloat(), currentY, lineP)
      currentY += 16f

      val bodyP = Paint().apply {
        color = Color.rgb(30, 41, 59)
        textSize = 9.5f
        isAntiAlias = true
      }
      val textToDraw = if (content.isNotBlank()) content else "No observations noted."
      // Simple multi-line wrap
      val lines = textToDraw.chunked(95)
      for (line in lines) {
        canvas.drawText(line, 36f, currentY, bodyP)
        currentY += 15f
      }
      currentY += 12f
    }

    drawReportSection("Anthropometric & Measurement Changes", report.measurementChangesSummary)
    drawReportSection("Strength Progression & Progressive Overload", report.strengthProgressSummary)
    drawReportSection("Head Coach Clinical Observations", report.coachObservations)
    drawReportSection("Upcoming Cycle Goals", report.nextMonthGoals)
    drawReportSection("Updated Nutrition Target & Training Prescription", "${report.updatedCaloriesMacros}\n${report.updatedTrainingPlan}")

    drawFooter(canvas, 1)
    document.finishPage(page)

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val outFile = File(dir, "RushWay_Report_${System.currentTimeMillis()}.pdf")
    FileOutputStream(outFile).use { document.writeTo(it) }
    document.close()
    return outFile
  }

  // Helper Intent to view or share generated PDF
  fun createShareIntent(context: Context, pdfFile: File): Intent {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.provider",
      pdfFile
    )
    return Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, uri)
      putExtra(Intent.EXTRA_SUBJECT, pdfFile.name)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }

  private fun Double.roundToInt(): Int = Math.round(this).toInt()
}
